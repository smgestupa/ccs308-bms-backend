package com.bms.backend.models.book

import javax.persistence.*

@Entity
@Table(name="book")
class BookComplete constructor(
        cover: ByteArray?,
        title: String,
        author: String?,
        description: String?
) {

    @Id
    @Column(name="book_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val bookID: Int = 0;

    @Column
    val cover: ByteArray?;

    @Column(
            length=128,
            nullable=false
    )
    val title: String;

    @Column(length=64)
    val author: String?;

    @Column(length=1024)
    val description: String?;

    @OneToOne
    @JoinColumn(name="bookID")
    lateinit var bookMetadata: BookMetadata;

    init {
        this.cover = cover;
        this.title = title;
        this.author = author;
        this.description = description;
    }
}
