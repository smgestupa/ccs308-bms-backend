package com.bms.backend.models.user

import javax.persistence.*

@Entity
@Table(name="user_metadata")
data class UserMetadata(
    @Id
    @Column(name="user_id")
    val userID: Int = 0,

    @Column(
        length=64,
        nullable=false
    )
    val username: String,

    @Column(
        length=64,
        nullable=false
    )
    val password: String
)