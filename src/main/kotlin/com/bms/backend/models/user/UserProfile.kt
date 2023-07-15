package com.bms.backend.models.user


data class UserProfile constructor(
        val userID: Int = 0,
        val photo: ByteArray? = null,
        val firstName: String,
        val lastName: String,
        val bio: String?
)
