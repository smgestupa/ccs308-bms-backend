package com.bms.backend.repositories

import com.bms.backend.models.book.Book
import com.bms.backend.models.book.BookMetadata
import net.bytebuddy.build.Plugin.Engine.Summary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface BookRepository : JpaRepository<Book, Int> {

    @Query(
        value = "SELECT type FROM book_genre WHERE book_id = :bookID",
        nativeQuery = true
    )
    fun getBookGenre(@Param("bookID") bookID: Int): List<String>;

    @Query(
            value="SELECT * FROM favourite_book f JOIN books b USING (book_id) WHERE user_code = :userID",
            nativeQuery=true
    )
    fun getFavouriteBooks(@Param("userID") userID: Int): List<Summary>;

    @Query(
            value="SELECT EXISTS(SELECT * FROM favourite_book WHERE user_id = :userID AND book_id = :bookID)",
            nativeQuery=true
    )
    fun isFavouriteBook(@Param("userID") userID: Int, @Param("bookID") bookID: Int): Int;

    @Modifying
    @Transactional
    @Query(
            value="INSERT IGNORE INTO favourite_book (user_id, book_id) VALUES (:userID, :bookID)",
            nativeQuery=true
    )
    fun addFavouriteBook(@Param("userID") userID: Int, @Param("bookID") bookID: Int);

    @Modifying
    @Transactional
    @Query(
            value="DELETE IGNORE FROM favourite_book WHERE user_id = :userID AND book_id = :bookID",
            nativeQuery=true
    )
    fun removeFavouriteBook(@Param("userID") userID: Int, @Param("bookID") bookID: Int);

    @Modifying
    @Transactional
    @Query(
        value="INSERT IGNORE INTO user_history (user_id, book_id, action) VALUES (:userID, :bookID, :action)",
        nativeQuery=true
    )
    fun addUserBookHistory(@Param("userID") userID: Int, @Param("bookID") bookID: Int, @Param("action") action: String);


    @Modifying
    @Transactional
    @Query(
        value="INSERT IGNORE INTO book_genre VALUES (:bookID, :type)",
        nativeQuery=true
    )
    fun addBookGenre(@Param("bookID") bookID: Int, @Param("type") type: String);

    fun findByTitleContains(title: String): List<Book>;
    fun findByBookMetadataIsbn10(isbn10: String): List<Book>;
    fun findByBookMetadataIsbn13(isbn13: String): List<Book>;

}