package com.bms.backend.payloads.requests

data class UserProfileRequest constructor(
        val userID: String,
        val firstName: String,
        val lastName: String,
        val bio: String
)
