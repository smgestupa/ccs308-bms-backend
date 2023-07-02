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

    @Modifying
    @Transactional
    @Query(
        value="INSERT IGNORE INTO book_genre VALUES (:bookID, :type)",
        nativeQuery=true
    )
    fun addBookGenre(@Param("bookID") bookID: Int, @Param("type") type: String);

    @Query(
            value="SELECT * FROM favourite_book f JOIN books b USING (book_id) WHERE user_code = :userID",
            nativeQuery=true
    )
    fun getFavouriteBooks(@Param("userID") userID: Int): List<Summary>;

    @Query(
            value="SELECT EXISTS(SELECT * FROM favourite_book WHERE user_id = :userID AND book_id = :bookID",
            nativeQuery=true
    )
    fun isFavouriteBook(@Param("userID") userID: Int, @Param("bookID") bookID: Int): Int;

    @Query(
            value="INSERT IGNORE INTO favourite_book VALUES (:userID, :bookID)",
            nativeQuery=true
    )
    fun addFavouriteBook(@Param("userID") userID: Int, @Param("bookID") bookID: Int);

    @Query(
            value="DELETE IGNORE FROM favourite_book WHERE user_id = :userID AND book_id = :bookID)",
            nativeQuery=true
    )
    fun removeFavouriteBook(@Param("userID") userID: Int, @Param("bookID") bookID: Int): List<Book>;
}