package com.bms.backend.repositories

import com.bms.backend.models.book.Book
import net.bytebuddy.build.Plugin.Engine.Summary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookRepository : JpaRepository<Book, Int> {

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