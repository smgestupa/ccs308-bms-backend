package com.bms.backend.models.book

import com.fasterxml.jackson.annotation.JsonManagedReference
import javax.persistence.*

@Entity
@Table(name="book_metadata")
class BookMetadata constructor(
    coverID: Int,
    genreID: Int,
    pages: Int,
    publisher: String,
    publishDate: String,
    views: Int = 0,
    isbn10: String? = null,
    isbn13: String? = null
) {

    @Id
    @Column(name="book_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val bookID: Int = 0;

    @Column(
            name="cover_id",
            nullable=false
    )
    val coverID: Int;

    @Column(
            name="genre_id",
            nullable=false
    )
    val genreID: Int;

    @Column(nullable=false)
    val pages: Int;

    @Column(
        length=128,
        nullable=false
    )
    val publisher: String;

    @Column(
        length=16,
        nullable=false
    )
    val publishDate: String;

    @Column(nullable=false)
    val views: Int;

    @Column(length=64)
    val isbn10: String?;

    @Column(length=64)
    val isbn13: String?;

    @OneToOne(
            cascade = [ CascadeType.ALL ],
            fetch = FetchType.LAZY,
            mappedBy = "bookMetadata"
    )
    lateinit var bookComplete: BookComplete;

    init {
        this.coverID = coverID;
        this.genreID = genreID;
        this.pages = pages;
        this.publisher = publisher;
        this.publishDate = publishDate;
        this.views = views;
        this.isbn10 = isbn10;
        this.isbn13 = isbn13;
    }
}
