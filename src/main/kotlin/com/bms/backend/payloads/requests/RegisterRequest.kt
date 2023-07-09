package com.bms.backend.payloads.requests

class RegisterRequest {

    lateinit var username: String;
    lateinit var password: String;
    lateinit var roles: Set<String>;
    lateinit var firstName: String;
    lateinit var lastName: String;
    lateinit var bio: String;
    lateinit var genres: Set<String>;
}