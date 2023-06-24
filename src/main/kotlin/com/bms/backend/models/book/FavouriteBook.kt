package com.bms.backend.models.book

import javax.persistence.*

@Entity
@Table(name="favourite_book")
class FavouriteBook {

    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val userID: Int = 0;

    @Column(name="book_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val bookID: Int = 0;

    @OneToOne
    @JoinColumn(name="bookID")
    lateinit var book: Book;
}
