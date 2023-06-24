package com.bms.backend.models.user

import javax.persistence.*

@Entity
@Table(name="user")
class UserProfile constructor(
    photo: ByteArray?,
    firstName: String,
    lastName: String,
    bio: String?,
) {

    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val userID: Int = 0;

    @Column
    val photo: ByteArray?;

    @Column(
            name="first_name",
            length=64,
            nullable=false
    )
    val firstName: String;

    @Column(
            name="last_name",
            length=64,
            nullable=false
    )
    val lastName: String;

    @Column(length=64)
    val bio: String?;

    init {
        this.photo = photo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
    }
}