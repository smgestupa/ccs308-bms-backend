package com.bms.backend.controllers.v1

import com.bms.backend.models.book.Book
import com.bms.backend.models.book.BookMetadata
import com.bms.backend.payloads.response.MessageResponse
import com.bms.backend.repositories.BookRepository
import jep.Interpreter
import jep.NDArray
import jep.SharedInterpreter
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File

@CrossOrigin(origins=["*"], maxAge=3600)
@RestController
@RequestMapping("/api/v1/books")
class BooksController @Autowired constructor (
    private val bookRepository: BookRepository
) {

    @PostMapping(
            value=["/get/{id}"],
            consumes=["application/json"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    fun getBook(@PathVariable id: String): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.NOT_FOUND;

        return ResponseEntity(
                MessageResponse("No book found in the database", status.value()),
                status
        );
    }

    @GetMapping(
            value=["/get/random"],
            produces=["application/json"]
    )
    @Throws(Exception::class)
    fun getRandomBooks(): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.NOT_FOUND;

        val interp: Interpreter = SharedInterpreter();
        interp.use {
            val testValue: HashMap<String, String> = hashMapOf("awdaw" to "Awdaw", "wda" to "awdwa");
            it.runScript("F:/Programming/Python/recommend_books/main.py");
            it.set("value", testValue);
            println(it.getValue("value"));
            it.exec("value = pd.DataFrame(${testValue.map { v -> "${v.key}: ${v.value}" }.joinToString(", ") }");
            println(it.getValue("value"));
        }

        return ResponseEntity(
                MessageResponse("Could not find random books from the database", status.value()),
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