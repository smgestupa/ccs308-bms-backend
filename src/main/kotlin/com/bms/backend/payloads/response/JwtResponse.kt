package com.bms.backend.payloads.response

class JwtResponse constructor(
        token: String,
        userID: Int,
        roles: List<String>
) {

    val token: String;
    val type: String = "Bearer ";
    val userID: Int;
    val roles: List<String>;

    init {
        this.token = token;
        this.userID = userID;
        this.roles = roles;
    }
}