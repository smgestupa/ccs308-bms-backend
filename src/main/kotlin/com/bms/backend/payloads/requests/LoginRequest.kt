package com.bms.backend.payloads.requests

class LoginRequest {

    var userID: Int = 0;
    lateinit var userName: String;
    lateinit var password: String;
}