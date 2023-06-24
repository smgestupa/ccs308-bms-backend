package com.bms.backend.models

import javax.persistence.*

@Entity
@Table
class Genre {

    @Id
    @Column(name="genre_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val genreID: Int = 0;

    @Column(
            length=16,
            nullable=false
    )
    lateinit var type: String;
}