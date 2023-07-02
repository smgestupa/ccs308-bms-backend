package com.bms.backend.repositories

import com.bms.backend.models.book.BookMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BookMetadataRepository : JpaRepository<BookMetadata, Int> {
}