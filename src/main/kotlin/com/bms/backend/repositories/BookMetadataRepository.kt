package com.bms.backend.repositories

import com.bms.backend.models.book.BookMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BookMetadataRepository : JpaRepository<BookMetadata, Int> {

    @Query(
            value="SELECT bm.book_id, b.title, bm.cover_type, bm.pages, bm.publisher, bm.publish_date, bm.views, bm.isbn10, bm.isbn13, b.published,\n" +
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
    fun getBooksMetadataWithLimit(@Param("limit") limit: Int = 100): List<BookMetadata>;
}