package com.bms.backend.models.book

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name="book_metadata")
data class BookInformation(
    @Id
    @Column(name="book_id")
    val bookID: Int = 0,

    @Column(
        length=128,
        nullable=false
    )
    val title: String,

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

    @Column
    val views: Int = 0,

    @Column(nullable=false)
    val isbn10: String? = null,

    @Column(length=64)
    val isbn13: String? = null,

    @Column
    val published: Int = 0,

    @Column
    val aesthetic: Int = 0,

    @Column
    val dark: Int = 0,

    @Column
    val nature: Int = 0,

    @Column
    val plain: Int = 0,

    @Column
    val fantasy: Int = 0,

    @Column
    val horror: Int = 0,

    @Column
    val adventure: Int = 0,

    @Column
    val romance: Int = 0,

    @Column
    val mystery: Int = 0
)
