package com.bms.backend.controllers.v1

import com.bms.backend.models.book.Book
import com.bms.backend.models.book.BookInformation
import com.bms.backend.payloads.response.DataResponse
import com.bms.backend.payloads.response.MessageResponse
import com.bms.backend.repositories.BookInformationRepository
import com.bms.backend.repositories.BookMetadataRepository
import com.bms.backend.repositories.BookRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jep.NDArray
import jep.SharedInterpreter
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList


@CrossOrigin(origins=["*"], maxAge=3600)
@RestController
@RequestMapping("/api/v1/books")
class BooksController @Autowired constructor (
    private val bookRepository: BookRepository,
    private val bookMetadataRepository: BookMetadataRepository,
    private val bookInformationRepository: BookInformationRepository
) {

    @GetMapping(
            value=["/get/{id}"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    fun getBookRecord(@PathVariable id: String, @RequestHeader userID: String): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.NOT_FOUND;

        val book: Optional<Book> = bookRepository.findById(Integer.parseInt(id));

        if (book.isPresent && book.get().published) {
            status = HttpStatus.OK;

            GlobalScope.launch {
                bookRepository.addUserBookHistory(
                    Integer.parseInt(userID),
                    Integer.parseInt(id),
                    "view"
                );
            }

            val genres: List<String> = bookRepository.getBookGenre(book.get().bookID);

            return ResponseEntity(
                DataResponse(listOf(book.get(), genres), status.value()),
                status
            )
        }

        return ResponseEntity(
                MessageResponse("No book found in the database", status.value()),
                status
        );
    }
    @GetMapping(
            value=["/search"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    suspend fun searchBookRecords(@RequestParam query: String): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.NOT_FOUND;

        val formattedQuery = query.trim();

        var books: List<Book> = emptyList();
        if (formattedQuery.matches(Regex("^[\\d]{10}$")))
            books = bookRepository.findByBookMetadataIsbn10(formattedQuery);
        else if (formattedQuery.matches(Regex("^[\\d]{13}$")))
            books = bookRepository.findByBookMetadataIsbn13(formattedQuery);
        else
            books = bookRepository.findByTitleContains(formattedQuery);

        if (books.isNotEmpty()) {
            status = HttpStatus.OK;

            return ResponseEntity(
                    DataResponse(books, status.value()),
                    status
            );
        }

        return ResponseEntity(
                MessageResponse("No books found for recommendation", status.value()),
                status
        );
    }

    @PostMapping(
            value=["/add"],
            consumes=["application/json"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    @ResponseBody
    fun insertBookRecord(@RequestBody book: Book): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity(
                MessageResponse("Something went wrong in inserting the book to the database", status.value()),
                status
        );
    }

    @GetMapping(
            value=["/recommend"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    suspend fun recommendBooks(@RequestHeader userID: String, @RequestParam(required=false) genre: String?): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.NOT_FOUND;

        var historyBooksInformation: List<BookInformation> = emptyList();
        var booksInformation: List<BookInformation> = emptyList();


        val retrieveHistoryBooksInformation: Job = CoroutineScope(Dispatchers.Default).launch {
            historyBooksInformation = bookInformationRepository.getHistoryBooksInformationWithLimit(userID=Integer.parseInt(userID));
        };

        val retrieveBooksInformation: Job = CoroutineScope(Dispatchers.Default).launch {
            booksInformation = bookInformationRepository.getBooksInformationWithLimit();
        };

        retrieveHistoryBooksInformation.join();
        retrieveBooksInformation.join();

        if (historyBooksInformation.isEmpty() || booksInformation.isEmpty()) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity(
                MessageResponse("Something went wrong with retrieving book information", status.value()),
                status
            );
        }

        var recommended: List<Int>;
        runBlocking {
            SharedInterpreter().use {
                val gson: Gson = Gson();

                it.runScript("F:/Programming/Python/recommend_books/main.py");
                it.set("user", gson.toJsonTree(historyBooksInformation).asJsonArray.toString());
                it.set("books", gson.toJsonTree(booksInformation).asJsonArray.toString());

                it.exec("recommended = recommend_books(user, books)");

                recommended = gson.fromJson(it.getValue("recommended") as String, ArrayList<Double>()::class.java)
                    .stream()
                    .map { value -> value.toInt() }
                    .collect(Collectors.toList());
            }
        }

        val recommendedBooks: List<Book> = bookRepository.findAllById(recommended);

        if (recommendedBooks.isNotEmpty()) {
            status = HttpStatus.OK;

            return ResponseEntity(
                DataResponse(recommendedBooks, status.value()),
                status
            );
        }

        return ResponseEntity(
                MessageResponse("No books found for recommendation", status.value()),
                status
        );
    }

//    @GetMapping(
//        value=["/shawn"],
//        produces=["application/json"]
//    )
//    fun shawn(): ResponseEntity<Any> {
//        var status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
//
//        val booksFirst = arrayOf(
//            arrayOf("Fantasy Genre", null, null, false),
//            arrayOf("Horror Genre", null, null, false),
//            arrayOf("Adventure Genre", null, null, false),
//            arrayOf("Romance Genre", null, null, false),
//            arrayOf("Mystery Genre", null, null, false)
//        )
//
//        val books = arrayOf(
//            arrayOf("Harry Potter and the Chamber of Secrets", "J.K. Rowling", "Harry Potter's summer has included the worst birthday ever, doomy warnings from a house-elf called Dobby, and rescue from the Dursleys by his friend Ron Weasley in a magical flying car! Back at Hogwarts School of Witchcraft and Wizardry for his second year, Harry hears strange whispers echo through empty corridors - and then the attacks start. Students are found as though turned to stone... Dobby's sinister predictions seem to be coming true."),
//
//            arrayOf("The Binding", "Bridget Collins", "One of 2019’s standout debuts, The Binding became an instant favourite with our booksellers from the moment it landed in their hands in January. Heart-racingly exciting, chilling and with a startlingly original premise, it is a supernaturally tinged tale of forbidden love, buried secrets and unspeakable betrayal that is simply unforgettable."),
//
//            arrayOf("The Wrath and the Dawn", "Renée Ahdieh", "Every dawn brings horror to a different family in a land ruled by a killer. Khalid, the eighteen-year-old Caliph of Khorasan, takes a new bride each night only to have her executed at sunrise. So it is a suspicious surprise when sixteen-year-old Shahrzad volunteers to marry Khalid. But she does so with a clever plan to stay alive and exact revenge on the Caliph for the murder of her best friend and countless other girls. Shazi’s wit and will, indeed, get her through to the dawn that no others have seen, but with a catch . . . she’s falling in love with the very boy who killed her dearest friend."),
//
//            arrayOf("The Bone Witch", "Rin Chupeco", "A story of scorned witches, sinister curses, and resurrection, The Bone Witch is the start of a dark fantasy trilogy, perfect for readers of Serpent & Dove and The Cruel Prince."),
//
//            arrayOf("Nocturna", "Maya Motayne", "The first in a sweeping and epic debut fantasy trilogy set in a stunning Latinx-inspired world about a face-changing thief and a risk-taking prince who must team up to defeat a powerful evil they accidentally unleashed. Perfect for fans of Tomi Adeyemi and Sabaa Tahir."),
//
//            arrayOf("Spin the Dawn", "Elizabeth Lim", "Project Runway meets Mulan in this sweeping fantasy about a teenage girl who poses as a boy to compete for the role of imperial tailor and embarks on an impossible journey to sew three magic dresses, from the sun, the moon, and the stars. And don’t miss Elizabeth Lim’s new novel, the instant New York Times bestseller, Six Crimson Cranes!"),
//
//            arrayOf("Carve the Mark (Carve the Mark, Book 1)", "Veronica Roth", "Cyra Noavek and Akos Kereseth have grown up in enemy countries locked in a long-standing fight for dominance over their shared planet. When Akos and his brother are kidnapped by the ruling Noavek family, Akos is forced to serve Cyra, the sister of a dictator who governs with violence and fear. Cyra is known for her deadly power of transferring extraordinary pain unto others with simple touch, and her tyrant brother uses her as a weapon against those who challenge him. But as Akos fights for his own survival, he recognizes that Cyra is also fighting for hers, and that her true gift—resilience—might be what saves them both."),
//
//            arrayOf("A Court of Frost and Starlight", "Maas, Sarah J.", "Feyre, Rhysand, and their close-knit circle of friends are still busy rebuilding the Night Court and the vastly-changed world beyond. But Winter Solstice is finally near, and with it, a hard-earned reprieve. Yet even the festive atmosphere can't keep the shadows of the past from looming. As Feyre navigates her first Winter Solstice as High Lady, she finds that those dearest to her have more wounds than she anticipated -- scars that will have a far-reaching impact on the future of their Court."),
//
//            arrayOf("The Darkest Part of the Forest", "Holly Black", "Faeries. Knights. Princes. True love. Think you know how the story goes? Think again...Near the little town of Fairfold, in the darkest part of the forest, lies a glass casket. Inside the casket lies a sleeping faerie prince that none can rouse. He's the most fascinating thing Hazel and her brother Ben have ever seen. They dream of waking him - but what happens when dreams come true? In the darkest part of the forest, you must be careful what you wish for...NEW YORK TIMES bestselling author Holly Black spins a dark, dangerous and utterly beautiful faerie tale, guaranteed to steal your heart. Holly Black is a NEW YORK TIMES bestselling author, co-creator of The Spiderwick Chronicles and author of YA novels including the widely acclaimed THE COLDEST GIRL IN COLDTOWN"),
//
//            arrayOf("Ghosts of the Shadow Market", "Cassandra Clare", "The Shadow Market is a meeting point for faeries, werewolves, warlocks, and vampires. There, the Downworlders buy and sell magical objects, make dark bargains, and whisper secrets they do not want the Shadowhunters to know. Through two centuries, however, there has been a frequent visitor to the Shadow Market from the very heart of the Shadowhunters’ world. Jem Carstairs is searching through the Shadow Market, in many different cities over long years, for a relic from his past."),
//
//            arrayOf("Six of Crows", " Leigh Bardugo", "Ketterdam: a bustling hub of international trade where anything can be had for the right price–and no one knows that better than criminal prodigy Kaz Brekker. Kaz is offered a chance at a deadly heist that could make him rich beyond his wildest dreams. But he can’t pull it off alone…"),
//
//            arrayOf("American Psycho", "Bret Easton Ellis", "Patrick Bateman is twenty-six and he works on Wall Street, he is handsome, sophisticated, charming and intelligent. He is also a psychopath. Taking us to head-on collision with America's greatest dream—and its worst nightmare—American Psycho is bleak, bitter, black comedy about a world we all recognise but do not wish to confront."),
//
//            arrayOf("Dread Nation", "Justina Ireland ", "At once provocative, terrifying, and darkly subversive, Dread Nation is Justina Ireland's stunning vision of an America both foreign and familiar—a country on the brink, at the explosive crossroads where race, humanity, and survival meet.\n\nJane McKeene was born two days before the dead began to walk the battlefields of Gettysburg, Pennsylvania—derailing the War Between the States and changing the nation forever.\n\nIn this new America, safety for all depends on the work of a few, and laws like the Native and Negro Education Act require certain children attend combat schools to learn to put down the dead.\n\nBut there are also opportunities—and Jane is studying to become an Attendant, trained in both weaponry and etiquette to protect the well-to-do. It's a chance for a better life for Negro girls like Jane. After all, not even being the daughter of a wealthy white Southern woman could save her from society’s expectations."),
//
//            arrayOf("Library of Souls", "Ransom Riggs", "Like its predecessors, Library of Souls blends thrilling fantasy with never-before-published vintage photography to create a one-of-a-kind reading experience.\n\nA boy with extraordinary powers. An army of deadly monsters. An epic battle for the future of peculiardom.\n\nThe adventure that began with Miss Peregrine’s Home for Peculiar Children and continued in Hollow City comes to a thrilling conclusion with Library of Souls. As the story opens, sixteen-year-old Jacob discovers a powerful new ability, and soon he’s diving through history to rescue his peculiar companions from a heavily guarded fortress. Accompanying Jacob on his journey are Emma Bloom, a girl with fire at her fingertips, and Addison MacHenry, a dog with a nose for sniffing out lost children.\n\nThey’ll travel from modern-day London to the labyrinthine alleys of Devil’s Acre, the most wretched slum in all of Victorian England. It’s a place where the fate of peculiar children everywhere will be decided once and for all."),
//
//            arrayOf("Welcome to Night Vale: A Novel", "Joseph Fink", "From the creators of the #1 international hit podcast Welcome to Night Vale comes an imaginative mystery of appearances and disappearances that is also a poignant look at the ways in which we all struggle to find ourselves . . . no matter where we live.\n\nWelcome to Night Vale . . . a friendly desert community somewhere in the American Southwest. In this ordinary little town where ghosts, angels, aliens, and government conspiracies are commonplace parts of everyday life, the lives of two women, with two mysteries, are about to converge.\n\nPawnshop proprietor Jackie Fierro abides by routine. But a crack appears in the standard order of her perpetually nineteen-year-old life when a mysterious man in a tan jacket gives her a slip of paper marked by two pencil-smudged words: KING CITY. Everything about the man unsettles her, especially the paper that she cannot remove from her hand. Yet when Jackie puts her life on hold to search for the man, no one who meets him can seem to remember anything about him."),
//
//            arrayOf("The Wasp Factory", "Iain Banks", "The polarizing literary debut by Scottish author Ian Banks, The Wasp Factory is the bizarre, imaginative, disturbing, and darkly comic look into the mind of a child psychopath.\n\nMeet Frank Cauldhame. Just sixteen, and unconventional to say the least:\n\nTwo years after I killed Blyth I murdered my young brother Paul, for quite different and more fundamental reasons than I'd disposed of Blyth, and then a year after that I did for my young cousin Esmerelda, more or less on a whim.\n\nThat's my score to date. Three. I haven't killed anybody for years, and don't intend to ever again.\n\nIt was just a stage I was going through."),
//
//            arrayOf("The Outsider", "Stephen King", "Evil has many faces…maybe even yours in this #1 New York Times bestseller from master storyteller Stephen King.\n\nAn eleven-year-old boy’s violated corpse is discovered in a town park. Eyewitnesses and fingerprints point unmistakably to one of Flint City’s most popular citizens—Terry Maitland, Little League coach, English teacher, husband, and father of two girls. Detective Ralph Anderson, whose son Maitland once coached, orders a quick and very public arrest. Maitland has an alibi, but Anderson and the district attorney soon have DNA evidence to go with the fingerprints and witnesses. Their case seems ironclad.\n\nAs the investigation expands and horrifying details begin to emerge, King’s story kicks into high gear, generating strong tension and almost unbearable suspense."),
//
//            arrayOf("Perfume", "Patrick Suskind", "In the slums of eighteenth-century France, the infant Jean-Baptiste Grenouille is born with one sublime gift—an absolute sense of smell. As a boy, he lives to decipher the odors of Paris, and apprentices himself to a prominent perfumer who teaches him the ancient art of mixing precious oils and herbs. But Grenouille’s genius is such that he is not satisfied to stop there, and he becomes obsessed with capturing the smells of objects such as brass doorknobs and fresh-cut wood. Then one day he catches a hint of a scent that will drive him on an ever-more-terrifying quest to create the “ultimate perfume”—the scent of a beautiful young virgin. Told with dazzling narrative brilliance, Perfume is a hauntingly powerful tale of murder and sensual depravity."),
//
//            arrayOf("IT", "Stephen King", "Welcome to Derry, Maine. It’s a small city, a place as hauntingly familiar as your own hometown. Only in Derry the haunting is real.\n\nThey were seven teenagers when they first stumbled upon the horror. Now they are grown-up men and women who have gone out into the big world to gain success and happiness. But the promise they made twenty-eight years ago calls them reunite in the same place where, as teenagers, they battled an evil creature that preyed on the city’s children. Now, children are being murdered again and their repressed memories of that terrifying summer return as they prepare to once again battle the monster lurking in Derry’s sewers."),
//
//            arrayOf("The Library at Mount Char", "Scott Hawkins", "A missing God.\n\nA library with the secrets to the universe.\n\nA woman too busy to notice her heart slipping away.\n\nCarolyn's not so different from the other people around her. She likes guacamole and cigarettes and steak. She knows how to use a phone. Clothes are a bit tricky, but everyone says nice things about her outfit with the Christmas sweater over the gold bicycle shorts.  \n\nAfter all, she was a normal American herself once.   \n\nThat was a long time ago, of course. Before her parents died. Before she and the others were taken in by the man they called Father. \n\nIn the years since then, Carolyn hasn't had a chance to get out much. Instead, she and her adopted siblings have been raised according to Father's ancient customs. They've studied the books in his Library and learned some of the secrets of his power. And sometimes, they've wondered if their cruel tutor might secretly be God."),
//
//            arrayOf("The Night Watch", "Sarah Waters ", "Moving back through the 1940s, through air raids, blacked-out streets, illicit partying, and sexual adventure, to end with its beginning in 1941, The Night Watch tells the story of four Londoners—three women and a young man with a past—whose lives, and those of their friends and lovers, connect in tragedy, stunning surprise and exquisite turns, only to change irreversibly in the shadow of a grand historical event."),
//
//            arrayOf("The Night Tiger", "Yangsze Choo", "1930s colonial Malaysia. Ji Lin is stuck as an apprentice dressmaker, moonlighting as a dancehall girl to help pay off her mother's Mahjong debts. Then one of her dance partners accidentally leaves behind a gruesome souvenir. Houseboy Ren is racing to fulfill his former master's dying wish: find the man's finger, lost years ago in an accident, and bury it with his body. Ren has 49 days to do so, or his master's soul will wander the earth forever. As a series of unexplained deaths racks the district, along with whispers of men who turn into tigers. Ji Lin and Ren's paths criss-cross through lush plantations, hospital storage rooms, and ghostly dreamscapes."),
//
//            arrayOf("The Road to Little Dribbling", "Bill Bryson", "A loving and hilarious--if occasionally spiky--valentine to Bill Bryson's adopted country, Great Britain. Prepare for total joy and multiple episodes of unseemly laughter. Twenty years ago, Bill Bryson went on a trip around Britain to discover and celebrate that green and pleasant land. The result was Notes from a Small Island, a true classic and one of the bestselling travel books ever written. Now he has traveled about Britain again, by bus and train and rental car and on foot, to see what has changed--and what hasn't.Following (but not too closely) a route he dubs the Bryson Line, from Bognor Regis in the south to Cape Wrath in the north, by way of places few travelers ever get to at all, Bryson rediscovers the wondrously beautiful, magnificently eccentric, endearingly singular country that he both celebrates and, when called for, twits."),
//
//            arrayOf("Bill Bryson's African Diary", "Bill Bryson", "A travel writer with little background knowledge of Africa recounts his journey to Kenya at the invitation of CARE International, where he visited slums, historic sites, natural wonders, refugee camps, and relief projects."),
//
//            arrayOf("I’m as Stranger Here Myself", "Bill Bryson", "Bryson recalls his return to the United States after twenty years in England."),
//
//            arrayOf("Notes From A Small Island", "Bill Bryson", "In 1993, before leaving his much-loved home in North Yorkshire to move back to the States for a few years with his family, Bill Bryson insisted on taking one last trip around Britain, a sort of valedictory tour of the green and kindly island that had so long been his home. His aim was to take stock of the nation's public face and private parts (as it were), and to analyse what precisely it was he loved so much about a country that had produced Marmite; a military hero whose dying wish was to be kissed by a fellow named Hardy; place names like Farleigh Wallop, Titsey and Shellow Bowells; people who said 'Mustn't grumble', and 'Ooh lovely' at the sight of a cup of tea and a plate of biscuits; and Gardeners' Question Time. Notes from a Small Island was a huge number-one bestseller when it was first published, and has become the nation's most loved book about Britain, going on to sell over two million copies."),
//
//            arrayOf("Neither Here Nor There: Travels in Europe", "Bill Bryson", "Bill Bryson's first travel book, The Lost Continent, was unanimously acclaimed as one of the funniest books in years. In Neither Here nor There he brings his unique brand of humour to bear on Europe as he shoulders his backpack, keeps a tight hold on his wallet, and journeys from Hammerfest, the northernmost town on the continent, to Istanbul on the cusp of Asia. Fluent in, oh, at least one language, he retraces his travels as a student twenty years before."),
//
//            arrayOf("The Little Book of Hygge: The Danish Way to Live Well", "Meik Wiking", "Hygge has been described as everything from \"cosines of the soul\" to \"the pursuit of everyday pleasures\". The Little Book of Hygge is the book we all need right now, and is guaranteed to bring warmth and comfort to you and your loved ones this winter."),
//
//            arrayOf("The Lost Continent: Travels in Small-Town America", "Bill Bryson", "The Lost Continent: Travels in Small-Town America is a book by travel writer Bill Bryson, chronicling his 13,978-mile trip around the United States in the autumn of 1987 and spring 1988."),
//
//            arrayOf("The Summer Isles: A Voyage of the Imagination", "Philip Marsden", "From the acclaimed author of Rising Ground, this is the story of a sea voyage to the Summer Isles, an enticing, periodically inhabited archipelago off the Scottish Highlands. To reach them, Marsden must head north, sailing a course along the exposed and exhilarating western coasts of Ireland and Scotland. It is a course that has been followed for centuries by explorers and adventurers, fishermen and monks, all drawn to the western seas and their distant horizons. But as much as the journey of men, this book is about the journey of ideas: of nostalgia and a very particular kind of geographical yearning; of a culture and language that has been shaped by its dramatic topography; of the local legend and lore that live on to this day."),
//
//            arrayOf("Treasure Island", "Robert Louis Stevenson", "For sheer storytelling delight and pure adventure, Treasure Island has never been surpassed. From the moment young Jim Hawkins first encounters the sinister Blind Pew at the Admiral Benbow Inn until the climactic battle for treasure on a tropic isle, the novel creates scenes and characters that have fired the imaginations of generations of readers. Written by a superb prose stylist, a master of both action and atmosphere, the story centers upon the conflict between good and evil - but in this case a particularly engaging form of evil. It is the villainy of that most ambiguous rogue Long John Silver that sets the tempo of this tale of treachery, greed, and daring."),
//
//            arrayOf("The Ragged Edge of Night", "Olivia Hawker", "For fans of All the Light We Cannot See, Beneath a Scarlet Sky, and The Nightingale comes an emotionally gripping, beautifully written historical novel about extraordinary hope, redemption, and one man’s search for light during the darkest times of World War II."),
//
//            arrayOf("The Time Traveler's Wife", "Audrey Niffenegger", "This is the extraordinary love story of Clare and Henry, who met when Clare was six and Henry was thirty-six, and were married when Clare was twenty-two and Henry was thirty. Impossible but true, because Henry suffers from a rare condition where his genetic clock periodically resets and he finds himself pulled suddenly into his past or future. In the face of this force they can neither prevent nor control, Henry and Clare's struggle to lead normal lives is both intensely moving and entirely unforgettable."),
//
//            arrayOf("A Very Large Expanse of Sea", "Tahereh Mafi", "Shirin is never surprised by how horrible people can be. She’s tired of the rude stares, the degrading comments - even the physical violence - she endures as a result of her race, her religion, and the hijab she wears every day. So she’s built up protective walls and refuses to let anyone close enough to hurt her. Instead, she drowns her frustrations in music and spends her afternoons break-dancing with her brother."),
//
//            arrayOf("The Winner's Curse", "Marie Rutkoski", "As a general's daughter in a vast empire that revels in war and enslaves those it conquers, Kestrel has two choices: she can join the military or get married. Kestrel has other ideas.One day, she is startled to find a kindred spirit in Arin, a young slave up for auction. Following her instinct, Kestrel buys him - and for a sensational price that sets the society gossips talking. It's not long before she has to hide her growing love for Arin. But he, too, has a secret and Kestrel quickly learns that the price she paid for him is much higher than she ever could have imagined. The first novel in a stunning new trilogy, The Winner's Curse is a story of romance, rumours and rebellion, where dirty secrets and careless alliances can be deadly - and everything is at stake."),
//
//            arrayOf("Sherwood", "Meagan Spooner", "Fans of Danielle Paige, Marissa Meyer, and Alex Flinn will devour New York Times bestselling author Meagan Spooner’s next fierce fairy tale-inspired story, which Illuminae author Amie Kaufman calls “a kick-ass, gender-flipped feminist retelling.”"),
//
//            arrayOf("Roomies", "Christina Lauren", "From subway to Broadway to happily ever after. Modern love in all its thrill, hilarity, and uncertainty has never been so compulsively readable as in New York Times bestselling author Christina Lauren’s romantic novel."),
//
//            arrayOf("The Kiss Quotient", "Helen Hoang", "A heartwarming and refreshing debut novel that proves one thing: there's not enough data in the world to predict what will make your heart tick."),
//
//            arrayOf("Say You're Sorry", "Melinda Leigh", "After the devastating loss of her husband in Iraq, Morgan Dane returns to Scarlet Falls, seeking the comfort of her hometown. Now, surrounded by family, she’s finally found peace and a promising career opportunity—until her babysitter is killed and her neighbor asks her to defend his son, Nick, who stands accused of the murder."),
//
//            arrayOf("Heartless", "Marissa Meyer", "Long before she was the terror of Wonderland—the infamous Queen of Hearts—she was just a girl who wanted to fall in love."),
//
//            arrayOf("The Proposal", "Jasmine Guillory", "The author of The Wedding Date serves up a novel about what happens when a public proposal doesn't turn into a happy ending, thanks to a woman who knows exactly how to make one on her own...When someone asks you to spend your life with him, it shouldn't come as a surprise--or happen in front of 45,000 people."),
//
//            arrayOf("The Postman Always Rings Twice", "Cain, James M.", "An amoral young tramp. A beautiful, sullen woman with an inconvenient husband. A problem that has only one grisly solution—a solution that only creates other problems that no one can ever solve. First published in 1934, The Postman Always Rings Twice is a classic of the roman noir. It established James M. Cain as a major novelist with an unsparing vision of America's bleak underside and was acknowledged by Albert Camus as the model for The Stranger."),
//
//            arrayOf("Someone Knows", "Lisa Scottoline", "Twenty years ago, in an upscale suburb of Philadelphia, four teenagers spent a summer as closest friends: drinking, sharing secrets, testing boundaries. When a new boy looked to join them, they decided to pull a prank on him, convincing him to play Russian roulette as an initiation into their group. They secretly planned to leave the gun unloaded—but what happened next would change each of them forever.\n\nNow three of the four reunite for the first time since that horrible summer. The guilt—and the lingering question about who loaded the gun—drove them apart. But after one of the group apparently commits suicide with a gun, their old secrets come roaring back. One of them is going to figure out if the new suicide is what it seems, and if it connects to the events of that long-ago summer. Someone knows exactly what happened—but who? And how far will they go to keep their secrets buried?"),
//
//            arrayOf("Redemption", "David Baldacci", "Detective Amos Decker discovers that a mistake he made as a rookie detective may have led to deadly consequences in the latest Memory Man thriller in David Baldacci's #1 New York Times bestselling series.\n\nAmos Decker and his FBI partner Alex Jamison are visiting his hometown of Burlington, Ohio, when he's approached by an unfamiliar man. But he instantly recognizes the man's name: Meryl Hawkins. He's the first person Decker ever arrested for murder back when he was a young detective. Though a dozen years in prison have left Hawkins unrecognizably aged and terminally ill, one thing hasn't changed: He maintains he never committed the murders. Could it be possible that Decker made a mistake all those years ago? As he starts digging into the old case, Decker finds a startling connection to a new crime that he may be able to prevent, if only he can put the pieces together quickly enough..."),
//
//            arrayOf("Tinker, Tailor, Soldier, Spy", "John le Carré", "A modern classic in which John le Carré expertly creates a total vision of a secret world, Tinker, Tailor, Soldier, Spy begins George Smiley's chess match of wills and wits with Karla, his Soviet counterpart.\n\nIt is now beyond a doubt that a mole, implanted decades ago by Moscow Centre, has burrowed his way into the highest echelons of British Intelligence. His treachery has already blown some of its most vital operations and its best networks. It is clear that the double agent is one of its own kind. But which one? George Smiley is assigned to identify him. And once identified, the traitor must be destroyed."),
//
//            arrayOf("The Good Daughter", "Karin Slaughter", "Two girls are forced into the woods at gunpoint. One runs for her life. One is left behind.\n\nTwenty-eight years ago, Charlotte and Samantha Quinn's happy small-town family life was torn apart by a terrifying attack on their family home. It left their mother dead. It left their father—Pikeville's notorious defense attorney—devastated. And it left the family fractured beyond repair, consumed by secrets from that terrible night.\n\nTwenty-eight years later, Charlotte has followed in her father's footsteps to become a lawyer herself—the ideal good daughter. But when violence comes to Pikeville again, and a shocking tragedy leaves the whole town traumatized, Charlotte is plunged into a nightmare."),
//
//            arrayOf("The Dry", "Jane Harper", "A small town hides big secrets in this atmospheric, page-turning debut mystery by award-winning author Jane Harper.\n\nIn the grip of the worst drought in a century, the farming community of Kiewarra is facing life and death choices daily when three members of a local family are found brutally slain.\n\nFederal Police investigator Aaron Falk reluctantly returns to his hometown for the funeral of his childhood friend, loath to face the townsfolk who turned their backs on him twenty years earlier.\n\nBut as questions mount, Falk is forced to probe deeper into the deaths of the Hadler family. Because Falk and Luke Hadler shared a secret. A secret Falk thought was long buried. A secret Luke's death now threatens to bring to the surface in this small Australian town, as old wounds bleed into new ones."),
//
//            arrayOf("End of Watch", "Stephen Edwin King", "The spectacular finale to the New York Times bestselling trilogy that began with Mr. Mercedes (winner of the Edgar Award) and Finders Keepers—In End of Watch, the diabolical “Mercedes Killer” drives his enemies to suicide, and if Bill Hodges and Holly Gibney don’t figure out a way to stop him, they’ll be victims themselves.\n\nIn Room 217 of the Lakes Region Traumatic Brain Injury Clinic, something has awakened. Something evil. Brady Hartsfield, perpetrator of the Mercedes Massacre, where eight people were killed and many more were badly injured, has been in the clinic for five years, in a vegetative state. According to his doctors, anything approaching a complete recovery is unlikely. But behind the drool and stare, Brady is awake, and in possession of deadly new powers that allow him to wreak unimaginable havoc without ever leaving his hospital room."),
//
//            arrayOf("The Alienist", "Caleb Carr", "The year is 1896, the place, New York City. On a cold March night New York Times reporter John Schuyler Moore is summoned to the East River by his friend and former Harvard classmate Dr. Laszlo Kreizler, a psychologist, or \"alienist.\" On the unfinished Williamsburg Bridge, they view the horribly mutilated body of an adolescent boy, a prostitute from one of Manhattan's infamous brothels."),
//
//            arrayOf("The 17th Suspect", "James Patterson", "In this #1 NYT bestseller, Sergeant Lindsay Boxer puts her life on the line to protect San Francisco from a shrewd and unpredictable killer.\n\nWhen a series of shootings exposes San Francisco to a mysterious killer, a reluctant woman decides to put her trust in Sergeant Lindsay Boxer. The confidential informant's tip leads Lindsay to a disturbing conclusion: something has gone horribly wrong inside the police department."),
//
//            arrayOf("The Fix An Amos Decker Novel 3", "David Baldacci", "Amos Decker witnesses a murder just outside FBI headquarters. A man shoots a woman execution-style on a crowded sidewalk, then turns the gun on himself.\n\nEven with Decker's extraordinary powers of observation and deduction, the killing is baffling. Decker and his team can find absolutely no connection between the shooter--a family man with a successful consulting business--and his victim, a schoolteacher. Nor is there a hint of any possible motive for the attack.\n\nEnter Harper Brown. An agent of the Defense Intelligence Agency, she orders Decker to back off the case. The murder is part of an open DIA investigation, one so classified that Decker and his team aren't cleared for it.")
//        );
//
//        val booksMetadataFirst = arrayOf(
//                arrayOf("none", 0, "System ", "N/A", null, null),
//                arrayOf("none", 0, "System ", "N/A", null, null),
//                arrayOf("none", 0, "System ", "N/A", null, null),
//                arrayOf("none", 0, "System ", "N/A", null, null),
//                arrayOf("none", 0, "System ", "N/A", null, null)
//        );
//
//        val booksMetadata = arrayOf(
//            arrayOf("dark", 341, "Pottermore ", "2015", "0439064864 ", "9780439064866"),
//            arrayOf("nature", 448, "HarperCollins ", "2020", "n/a", "9780008272142"),
//            arrayOf("dark", 416, "Penguin", "2015", "1473657938", "9781473657939"),
//            arrayOf("dark", 400, "Hardcover ", "2017", "1492635839", "9781492635833"),
//            arrayOf("aesthetic", 480, "Hodder & Stoughton", "2019", "0062842730", "9780062842732"),
//            arrayOf("aesthetic", 432, "Random House Children's Books", "2020", "0525647015", "9780593126028"),
//            arrayOf("plain", 480, "Katherine Tegen Books", "2017", "0008157820", "9780008157821"),
//            arrayOf("aesthetic", 466, "Bloomsbury", "2018", "1408890321", "9781408890325"),
//            arrayOf("dark", 368, "Orion Children's Books", "2015", "1780621744", "9781780621746"),
//            arrayOf("aesthetic", 617, "Margaret K. McElderry Books", "2020", "1406385387", "9781406385380"),
//            arrayOf("dark", 465, "Henry Holt & Company", "2015", "1522609733", "9781522609735"),
//            arrayOf("aesthetic", 399, "Vintage", "1991", "0330319922", "9780330319928"),
//            arrayOf("dark", 464, "Balzer + Bray", "2018", "0062570609", "9780062570604"),
//            arrayOf("dark", 464, "Quirk Books", "2017", "1594749310", "9781594749315"),
//            arrayOf("nature", 416, "Harper Parennial", "2017", "0062351435", "9780062351432"),
//            arrayOf("aesthetic", 184, "Simon & Schuster", "1998", "0684853159", "9780684853154"),
//            arrayOf("dark", 576, "Scribner", "2018", "1501180983", "9781501180989"),
//            arrayOf("aesthetic", 255, "Vintage", "2001", "0375725849", "9780375725845"),
//            arrayOf("plain", 1168, "Scribner", "2016", "1501142976", "9781501142970"),
//            arrayOf("dark", 400, "Crown", "2016", "0553418629", "9780553418620"),
//            arrayOf("nature", 560, "Flatiron Books", "2019", "1250175445", "9781250175441"),
//            arrayOf("dark", 372, "Flatiron Books", "2019", "1250175445", "9781250175441"),
//            arrayOf("nature", 400, "Doubleday, an Imprint of Penguin Random House", "2016", "0385539290", "9780385539296"),
//            arrayOf("dark", 48, "Broadway Books", "2002", "0307418847", "9780307418845"),
//            arrayOf("aesthetic", 288, "Broadway Books", "1999", "0767931181", "9780767931182"),
//            arrayOf("nature", 384, "Transworld Publishers Ltd", "2015", "1784161195", "9781784161194"),
//            arrayOf("aesthetic", 254, "William Morrow Paperbacks", "1993", "0380713802", "9780380713806"),
//            arrayOf("aesthetic", 289, "Penguin UK", "2016", "0241283914", "9780241283912"),
//            arrayOf("nature", 299, "William Morrow Paperbacks", "1990", "0060920084", "9780060920081"),
//            arrayOf("nature", 352, "Granta Books", "2019", "1783782994", "9781783782994"),
//            arrayOf("aesthetic", 311, "Kingfisher", "2001", "0753453800", "9780753453803"),
//            arrayOf("nature", 340, "Lake Union Publishing", "2018", "1503900908", "9781503900905"),
//            arrayOf("dark", 537, "Zola Books", "2013", "0224071912", "9781939126016"),
//            arrayOf("plain", 310, "HarperTeen", "2018", "0062866567", "9780062866561"),
//            arrayOf("dark", 355, "Farrar Straus Giroux", "2014", "0374384681", "9780374384685"),
//            arrayOf("aesthetic", 496, "HarperTeen", "2019", "0062422332", "9780062422330"),
//            arrayOf("nature", 368, "Gallery Books", "2017", "1501165836", "9781501165832"),
//            arrayOf("plain", 323, "Berkley", "2018", "0451490800", "9780451490803"),
//            arrayOf("plain", 330, "Montlake Romance", "2017", "1503948706", "9781503948709"),
//            arrayOf("dark", 464, "Feiwel & Friends", "2016", "1250044650", "9781250044655"),
//            arrayOf("plain", 325, "Berkley", "2018", "0399587683", "9780399587689"),
//            arrayOf("dark", 116, "Vintage", "1989", "0679723250", "9781250044655"),
//            arrayOf("nature", 400, "G.P. Putnam's Sons", "2019", "0525539646", "9780525539643"),
//            arrayOf("nature", 417, "Grand Central Publishing", "2019", "1538761416", "9781538761410"),
//            arrayOf("dark", 281, "Penguin Books", "2011", "0143119788", "9780143119784"),
//            arrayOf("dark", 656, "William Morrow", "2017", "0062430262", "9780062430267"),
//            arrayOf("dark", 336, "Macmillan Australia", "2016", "1250196760", "9781250196767"),
//            arrayOf("aesthetic", 432, "Scribner", "2016", "1501129740", "9781501129742"),
//            arrayOf("dark", 498, "Random House", "2006", "0812976142", "9780812976144"),
//            arrayOf("aesthetic", 353, "Little, Brown And Company", "2018", "1780895216", "9781780895215"),
//            arrayOf("aesthetic", 434, "Grand Central Publishing", "2017", "1455586560", "9781455586561"),
//        );
//
//        val genreFirst = arrayOf(
//            arrayOf("fantasy"),
//            arrayOf("horror"),
//            arrayOf("adventure"),
//            arrayOf("romance"),
//            arrayOf("mystery")
//        );
//
//        val genre = arrayOf(
//            arrayOf("fantasy"),
//            arrayOf("romance", "fantasy"),
//            arrayOf("fantasy"),
//            arrayOf("fantasy"),
//            arrayOf("adventure", "fantasy"),
//            arrayOf("fantasy"),
//            arrayOf("fantasy"),
//            arrayOf("adventure", "fantasy"),
//            arrayOf("fantasy"),
//            arrayOf("fantasy"),
//            arrayOf("adventure", "fantasy"),
//            arrayOf("horror"),
//            arrayOf("horror"),
//            arrayOf("horror", "fantasy"),
//            arrayOf("fantasy"),
//            arrayOf("horror"),
//            arrayOf("horror"),
//            arrayOf("horror"),
//            arrayOf("horror"),
//            arrayOf("horror"),
//            arrayOf("horror"),
//            arrayOf("adventure", "fantasy"),
//            arrayOf("adventure"),
//            arrayOf("adventure"),
//            arrayOf("adventure"),
//            arrayOf("adventure"),
//            arrayOf("adventure"),
//            arrayOf("romance"),
//            arrayOf("romance", "adventure"),
//            arrayOf("romance", "adventure"),
//            arrayOf("romance", "adventure"),
//            arrayOf("romance"),
//            arrayOf("romance", "fantasy"),
//            arrayOf("romance"),
//            arrayOf("romance", "fantasy"),
//            arrayOf("romance", "fantasy"),
//            arrayOf("romance"),
//            arrayOf("romance", "mystery"),
//            arrayOf("romance", "fantasy"),
//            arrayOf("romance"),
//            arrayOf("mystery"),
//            arrayOf("mystery"),
//            arrayOf("mystery"),
//            arrayOf("mystery"),
//            arrayOf("mystery"),
//            arrayOf("mystery"),
//            arrayOf("mystery", "horror"),
//            arrayOf("mystery"),
//            arrayOf("mystery"),
//            arrayOf("mystery"),
//        );
//
//        val bookList: MutableList<Book> = mutableListOf();
//
//        for (index in booksFirst.indices) {
//            val selectedBook = booksFirst[index]
//            var book: Book = Book(
//                null,
//                selectedBook[0].toString(),
//                selectedBook[1].toString(),
//                selectedBook[2].toString()
//            );
//
//            if (selectedBook.size == 4)
//                book.published = selectedBook[3] as Boolean
//
//            val savedBook = bookRepository.save(book);
//
//            var metadata = booksMetadataFirst[index];
//            val bookMetadata = BookMetadata(
//                savedBook.bookID,
//                metadata[0].toString(),
//                Integer.parseInt(metadata[1].toString()),
//                metadata[2].toString(),
//                metadata[3].toString(),
//                0,
//                metadata[4].toString(),
//                metadata[5].toString()
//            );
//
//            bookMetadataRepository.save(bookMetadata);
//
//            for (gIndex in 0 until genreFirst[index].size) {
//                bookRepository.addBookGenre(savedBook.bookID, genreFirst[index][gIndex]);
//            }
//        }
//
//        for (index in books.indices) {
//            val selectedBook = books[index]
//            var book: Book = Book(
//                File("C:/Users//cool laptop ni shawn/Downloads/BookCoverTinyJPG-20230629T091313Z-001/BookCoverTinyJPG/cover_$index.jpg").readBytes(),
//                selectedBook[0].toString(),
//                selectedBook[1].toString(),
//                selectedBook[2].toString()
//            );
//
//            if (selectedBook.size == 4)
//                book.published = selectedBook[3] as Boolean
//
//            val savedBook = bookRepository.save(book);
//
//            var metadata = booksMetadata[index];
//            val bookMetadata = BookMetadata(
//                savedBook.bookID,
//                metadata[0].toString(),
//                Integer.parseInt(metadata[1].toString()),
//                metadata[2].toString(),
//                metadata[3].toString(),
//                0,
//                metadata[4].toString(),
//                metadata[5].toString()
//            );
//
//            bookMetadataRepository.save(bookMetadata);
//
//            for (gIndex in 0 until genre[index].size) {
//                bookRepository.addBookGenre(savedBook.bookID, genre[index][gIndex]);
//            }
//        }
//
//
//        return ResponseEntity(
//            MessageResponse("Something went wrong in classifying the book cover", status.value()),
//            status
//        );
//    }

    @PostMapping(
            value=["/cover/classify"],
            consumes=["multipart/form-data"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    @ResponseBody
    fun classifyBookCover(@RequestBody bookCover: MultipartFile): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        var coverType: String = "";

        runBlocking {
            SharedInterpreter().use {
                it.runScript("F:/Programming/Python/classify_book_cover/main.py");
                it.set("img", NDArray(bookCover.bytes));
                it.exec("coverID = classifyBookCover(img)");

                val coverID: Any = it.getValue("coverID");
                if (coverID.toString().isNotEmpty())
                    coverType = coverID.toString();
            }
        }

        if (coverType.isNotEmpty()) {
            status = HttpStatus.OK;

            return ResponseEntity(
                    MessageResponse(coverType, status.value()),
                    status
            );
        }

        return ResponseEntity(
                MessageResponse("Something went wrong in classifying the book cover", status.value()),
                status
        );
    }
}