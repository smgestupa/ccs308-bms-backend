package com.bms.backend.models.book

import javax.persistence.*

@Entity
@Table(name="book_metadata")
data class BookMetadata(
    @Id
    @Column(name="book_id")
    val bookID: Int = 0,

    @Column(
        name="cover_type",
        length=16,
        nullable=false
    )
    val coverType: String,

    @Column(nullable=false)
    val pages: Int = 0,

    @Column(
        length=128,
        nullable=false
    )
    val publisher: String,

    @Column(
        length=16,
        nullable=false
    )
    val publishDate: String,


    val views: Int = 0,

    @Column(nullable=false)
    val isbn10: String? = null,

    @Column(length=64)
    val isbn13: String? = null,
)
