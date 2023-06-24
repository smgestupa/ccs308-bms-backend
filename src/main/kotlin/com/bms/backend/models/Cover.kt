package com.bms.backend.models

import javax.persistence.*

@Entity
@Table
class Cover {

    @Id
    @Column(name="cover_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val coverID: Int = 0;

    @Column(
            length=16,
            nullable=false
    )
    lateinit var type: String;
}