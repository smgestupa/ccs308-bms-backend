package com.bms.backend.models.user

import javax.persistence.*

@Entity
@Table(name="user_metadata")
class UserMetadata constructor(
        username: String,
        password: String
) {

    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val userID: Int = 0;


    @Column(
            length=64,
            nullable=false
    )
    val username: String;

    @Column(
            length=64,
            nullable=false
    )
    val password: String;

    init {
        this.username = username;
        this.password = password;
    }
}