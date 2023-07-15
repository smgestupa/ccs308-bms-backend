package com.bms.backend.repositories

import com.bms.backend.models.book.BookInformation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface BookInformationRepository : JpaRepository<BookInformation, Int> {

    @Query(
        value="SELECT uh.book_id, b.title, bm.cover_type, bm.pages, bm.publisher, bm.publish_date, bm.views, bm.isbn10, bm.isbn13, b.published,\n" +
                "\tMAX(CASE WHEN TRIM(bm.cover_type) = 'Aesthetic' THEN 1 ELSE 0 END) aesthetic,\n" +
                "\tMAX(CASE WHEN TRIM(bm.cover_type) = 'Dark' THEN 1 ELSE 0 END) dark,\n" +
                "\tMAX(CASE WHEN TRIM(bm.cover_type) = 'Nature' THEN 1 ELSE 0 END) nature,\n" +
                "\tMAX(CASE WHEN TRIM(bm.cover_type) = 'Plain' THEN 1 ELSE 0 END) plain,\n" +
                "\tMAX(CASE WHEN g.type = 'Fantasy' THEN 1 ELSE 0 END) fantasy,\n" +
                "\tMAX(CASE WHEN g.type = 'Horror' THEN 1 ELSE 0 END) horror,\n" +
                "\tMAX(CASE WHEN g.type = 'Adventure' THEN 1 ELSE 0 END) adventure,\n" +
                "\tMAX(CASE WHEN g.type = 'Romance' THEN 1 ELSE 0 END) romance,\n" +
                "\tMAX(CASE WHEN g.type = 'Mystery' THEN 1 ELSE 0 END) mystery\n" +
                "FROM user_history uh\n" +
                "INNER JOIN book_metadata bm\n" +
                "\tON (uh.book_id = bm.book_id)\n" +
                "INNER JOIN book b\n" +
                "\tON (uh.book_id = b.book_id)\n" +
                "INNER JOIN book_genre g\n" +
                "\tON (uh.book_id = g.book_id)\n" +
                "WHERE uh.user_id = :userID\n" +
                "GROUP BY bm.book_id, b.title, bm.cover_type, bm.pages, bm.publisher, bm.publish_date, bm.views, bm.isbn10, bm.isbn13, b.published, uh.created_at\n" +
                "ORDER BY uh.created_at DESC\n" +
                "LIMIT :limit",
        nativeQuery=true
    )
    fun getHistoryBooksInformationWithLimit(@Param("userID") userID: Int, @Param("limit") limit: Int = 100): List<BookInformation>;

    @Query(
        value="SELECT bm.book_id, b.title, bm.cover_type, bm.pages, bm.publisher, bm.publish_date, bm.views, bm.isbn10, bm.isbn13, b.published,\n" +
                "\tMAX(CASE WHEN TRIM(bm.cover_type) = 'Aesthetic' THEN 1 ELSE 0 END) aesthetic,\n" +
                "\tMAX(CASE WHEN TRIM(bm.cover_type) = 'Dark' THEN 1 ELSE 0 END) dark,\n" +
                "\tMAX(CASE WHEN TRIM(bm.cover_type) = 'Nature' THEN 1 ELSE 0 END) nature,\n" +
                "\tMAX(CASE WHEN TRIM(bm.cover_type) = 'Plain' THEN 1 ELSE 0 END) plain,\n" +
                "\tMAX(CASE WHEN g.type = 'Fantasy' THEN 1 ELSE 0 END) fantasy,\n" +
                "\tMAX(CASE WHEN g.type = 'Horror' THEN 1 ELSE 0 END) horror,\n" +
                "\tMAX(CASE WHEN g.type = 'Adventure' THEN 1 ELSE 0 END) adventure,\n" +
                "\tMAX(CASE WHEN g.type = 'Romance' THEN 1 ELSE 0 END) romance,\n" +
                "\tMAX(CASE WHEN g.type = 'Mystery' THEN 1 ELSE 0 END) mystery\n" +
                "FROM book_metadata bm\n" +
                "INNER JOIN book b\n" +
                "\tON (bm.book_id = b.book_id)\n" +
                "INNER JOIN book_genre g\n" +
                "\tON (bm.book_id = g.book_id)\n" +
                "GROUP BY bm.book_id, b.title, bm.cover_type, bm.pages, bm.publisher, bm.publish_date, bm.views, bm.isbn10, bm.isbn13, b.published\n" +
                "LIMIT :limit",
        nativeQuery=true
    )
    fun getBooksInformationWithLimit(@Param("limit") limit: Int = 100): List<BookInformation>;
}