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
            value="SELECT b.book_id, cover, title, author, description, published, b.created_at, b.updated_at FROM book b INNER JOIN favourite_book fb ON (b.book_id = fb.book_id) WHERE fb.user_id = :userID LIMIT 7",
            nativeQuery=true
    )
    fun listFavouriteBooks(@Param("userID") userID: Int): List<Book>;

    @Query(
            value="SELECT b.book_id, cover, title, author, description, published, b.created_at, b.updated_at FROM book b INNER JOIN favourite_book fb ON (b.book_id = fb.book_id) WHERE fb.user_id = :userID",
            nativeQuery=true
    )
    fun getFavouriteBooks(@Param("userID") userID: Int): List<Book>;

    @Query(
            value="SELECT b.book_id, cover, title, author, description, published, b.created_at, b.updated_at FROM book b INNER JOIN favourite_book fb ON (b.book_id = fb.book_id) WHERE fb.user_id = :userID AND b.title LIKE %:title%",
            nativeQuery=true
    )
    fun getFavouriteBooksByTitle(@Param("userID") userID: Int, @Param("title") title: String): List<Book>;

    @Query(
            value="SELECT b.book_id, cover, title, author, description, published, b.created_at, b.updated_at FROM book b INNER JOIN favourite_book fb ON (b.book_id = fb.book_id) INNER JOIN book_metadata bm  ON (b.book_id = bm.book_id) WHERE fb.user_id = :userID AND b.book_id = bm.book_id AND bm.isbn10 = :isbn10",
            nativeQuery=true
    )
    fun getFavouriteBooksByIsbn10(@Param("userID") userID: Int, @Param("isbn10") isbn10: String): List<Book>;

    @Query(
            value="SELECT b.book_id, cover, title, author, description, published, b.created_at, b.updated_at FROM book b INNER JOIN favourite_book fb ON (b.book_id = fb.book_id) INNER JOIN book_metadata bm  ON (b.book_id = bm.book_id) WHERE fb.user_id = :userID AND b.book_id = bm.book_id AND bm.isbn13 = :isbn13",
            nativeQuery=true
    )
    fun getFavouriteBooksByIsbn13(@Param("userID") userID: Int, @Param("isbn13") isbn13: String): List<Book>;

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


    @Query(
            value="SELECT b.book_id, cover, title, author, description, published, b.created_at, b.updated_at FROM book b INNER JOIN book_metadata bm ON (b.book_id = bm.book_id) ORDER BY bm.views DESC LIMIT 7",
            nativeQuery=true
    )
    fun listTrendingBooks(): List<Book>;

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