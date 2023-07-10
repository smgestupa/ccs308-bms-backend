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
import org.springframework.security.access.prepost.PreAuthorize
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

    @PreAuthorize("hasRole('USER')")
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

    @PreAuthorize("hasRole('USER')")
    @GetMapping(
            value=["/favourite/{id}"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    fun favouriteBookRecord(@PathVariable id: String, @RequestHeader userID: String): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.OK;

        if (bookRepository.isFavouriteBook(Integer.parseInt(userID), Integer.parseInt(id)) == 1) {
            bookRepository.removeFavouriteBook(Integer.parseInt(userID), Integer.parseInt(id));

            return ResponseEntity(
                    MessageResponse("Book has been removed from your favourites", status.value()),
                    status
            );
        }

        bookRepository.addFavouriteBook(Integer.parseInt(userID), Integer.parseInt(id));

        return ResponseEntity(
                MessageResponse("Book has been added to your favourites", status.value()),
                status
        );
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(
            value=["/favourite/list"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    suspend fun listFavouriteBooks(@RequestHeader userID: String): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.OK;

        var books: List<Book> = bookRepository.listFavouriteBooks(Integer.parseInt(userID));

        return ResponseEntity(
                DataResponse(books, status.value()),
                status
        );
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(
            value=["/favourite/search"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    fun searchFavouriteBooks(@RequestParam(required = false) query: String?, @RequestHeader userID: String): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.OK;

        var books: List<Book>;
        if (query.isNullOrEmpty())
            books = bookRepository.getFavouriteBooks(Integer.parseInt(userID));
        else {
            val formattedQuery: String = query.trim();

            books = if (formattedQuery.matches(Regex("^[\\d]{10}$")))
                bookRepository.getFavouriteBooksByIsbn10(Integer.parseInt(userID), formattedQuery);
            else if (formattedQuery.matches(Regex("^[\\d]{13}$")))
                bookRepository.getFavouriteBooksByIsbn13(Integer.parseInt(userID), formattedQuery);
            else
                bookRepository.getFavouriteBooksByTitle(Integer.parseInt(userID), formattedQuery);
        }

        return ResponseEntity(
                DataResponse(books, status.value()),
                status
        );
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping(
            value=["/trending/list"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    fun listTrendingBooks(): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.OK;

        var books: List<Book> = bookRepository.listTrendingBooks();

        return ResponseEntity(
                DataResponse(books, status.value()),
                status
        );
    }

    @PreAuthorize("hasRole('USER')")
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
            books = bookRepository.findByTitleContainsAndPublishedEquals(formattedQuery, true);

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

    @PreAuthorize("hasRole('EDITOR')")
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

    @PreAuthorize("hasRole('USER')")
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
            historyBooksInformation = bookInformationRepository.getHistoryBooksInformationWithLimit(userID=Integer.parseInt(userID), 10);
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
    @PreAuthorize("hasRole('USER')")
    @PostMapping(
            value=["/extract"],
            consumes=["multipart/form-data"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    @ResponseBody
    fun extractISBNCodes(@RequestBody uploadedImage: MultipartFile): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.NOT_FOUND;
        var isbn: String = "";

        runBlocking {
            SharedInterpreter().use {
                it.runScript("F:/Programming/Python/extract_isbn_codes/main.py");
                it.set("img", NDArray(uploadedImage.bytes));
                it.exec("isbnCode = extract_isbn_codes(img)");

                val isbnCode: Any? = it.getValue("isbnCode");
                if (isbnCode != null && isbnCode.toString().isNotEmpty())
                    isbn = isbnCode.toString();
            }
        }

        if (isbn.isNotEmpty()) {
            status = HttpStatus.OK;

            return ResponseEntity(
                    DataResponse(isbn, status.value()),
                    status
            );
        }

        return ResponseEntity(
                MessageResponse("No ISBN was detected from the image", status.value()),
                status
        );
    }
}