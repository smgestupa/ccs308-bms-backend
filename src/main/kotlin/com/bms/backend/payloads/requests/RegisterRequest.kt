package com.bms.backend.payloads.requests

class RegisterRequest {

    var userID: Int = 0;
    lateinit var firstName: String;
    lateinit var lastName: String;
    lateinit var username: String;
    lateinit var password: String;
    lateinit var roles: Set<String>;
}