//package com.bms.backend.controllers.v1
//
//import com.bms.backend.payloads.response.MessageResponse
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.*
//
//@CrossOrigin(origins=["*"], maxAge=3600)
//@RestController
//@RequestMapping("/api/v1/users")
//class UsersController {
//
//    @PostMapping(
//        value=["/login"],
//        consumes=["application/json"],
//        produces=["application/json"]
//    )
//    @ResponseBody
//    @Throws(Exception::class)
//    fun login(): ResponseEntity<Any> {
//        var status: HttpStatus = HttpStatus.NOT_FOUND;
//
//        return ResponseEntity(
//            MessageResponse("Username or password is invalid.", status.value()),
//            status
//        )
//    }
//
//    @PostMapping(
//        value=["/login"],
//        consumes=["application/json"],
//        produces=["application/json"]
//    )
//    @ResponseBody
//    @Throws(Exception::class)
//    fun register(): ResponseEntity<Any> {
//        var status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
//
//        return ResponseEntity(
//            MessageResponse("Something went wrong, please try again later.", status.value()),
//            status
//        )
//    }
//}