package com.bms.backend.controllers.v1

import com.bms.backend.enums.EnumRole
import com.bms.backend.models.role.Role
import com.bms.backend.models.user.User
import com.bms.backend.models.user.UserMetadata
import com.bms.backend.payloads.requests.LoginRequest
import com.bms.backend.payloads.requests.RegisterRequest
import com.bms.backend.payloads.response.JwtResponse
import com.bms.backend.payloads.response.MessageResponse
import com.bms.backend.repositories.BookRepository
import com.bms.backend.repositories.RoleRepository
import com.bms.backend.repositories.UserMetadataRepository
import com.bms.backend.repositories.UserRepository
import com.bms.backend.security.jwt.JwtUtils
import com.bms.backend.security.services.UserDetailsImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

@CrossOrigin(origins=["*"], maxAge=3600)
@RestController
@RequestMapping("/api/v1/users")
class UsersController @Autowired constructor(
        private val authenticationManager: AuthenticationManager,
        private val passwordEncoder: PasswordEncoder,
        private val jwtUtils: JwtUtils,
        private val bookRepository: BookRepository,
        private val userRepository: UserRepository,
        private val userMetadataRepository: UserMetadataRepository,
        private val roleRepository: RoleRepository
) {

    @PostMapping(
        value=["/login"],
        consumes=["application/json"],
        produces=["application/json"]
    )
    @ResponseBody
    @Throws(Exception::class)
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.OK;

        if (!userMetadataRepository.existsByUsername(loginRequest.username)) {
            status = HttpStatus.NOT_FOUND;
            return ResponseEntity(
                MessageResponse("Username or password is invalid", status.value()),
                status
            );
        }

        val user: User;
        userMetadataRepository.findByUsername(loginRequest.username)
            .get()
            .let {
                user = userRepository.findByUserID(it.userID).get();
            };

        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        );

        SecurityContextHolder.getContext().authentication = authentication;
        val jwt: String = jwtUtils.generateJwtToken(authentication);

        val userDetailsImpl: UserDetailsImpl = authentication.principal as UserDetailsImpl;
        val roles: List<String> = userDetailsImpl.authorities.stream()
            .map { item -> item.authority }
            .collect(Collectors.toList());

        return ResponseEntity(
            JwtResponse(
                jwt,
                user.userID,
                roles
            ), status
        );
    }

    @PostMapping(
        value=["/register"],
        consumes=["application/json"],
        produces=["application/json"]
    )
    @ResponseBody
    @Throws(Exception::class)
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<Any> {
        var status: HttpStatus = HttpStatus.OK;

        if (userMetadataRepository.existsByUsername(registerRequest.username)) {
            status = HttpStatus.CONFLICT;
            return ResponseEntity(
                MessageResponse("Username already been registered", status.value()),
                status
            );
        }

        val strGenres: Set<String> = registerRequest.genres;

        val strRoles: Set<String> = registerRequest.roles;
        val roles: MutableSet<Role> = mutableSetOf();

        strRoles.forEach {role -> run{
            when (role) {
                "editor" -> {
                    val editorRole: Role = roleRepository.findByType(EnumRole.ROLE_EDITOR)
                        .orElseThrow { RuntimeException("Editor Role could not be retrieved") };
                    roles.add(editorRole);
                }
                "admin" -> {
                    val adminRole: Role = roleRepository.findByType(EnumRole.ROLE_ADMIN)
                        .orElseThrow { RuntimeException("Admin Role could not be retrieved") };
                    roles.add(adminRole);
                }
                else -> {
                    val userRole: Role = roleRepository.findByType(EnumRole.ROLE_USER)
                        .orElseThrow { RuntimeException("User Role could not be retrieved") };
                    roles.add(userRole);
                }
            }
        }};

        val savedUser = userRepository.save(
            User(
                null,
                registerRequest.firstName,
                registerRequest.lastName,
                registerRequest.bio,
                roles.toSet()
            )
        );

        val userMetadata = UserMetadata(
            savedUser.userID,
            registerRequest.username,
            passwordEncoder.encode(registerRequest.password)
        );

        GlobalScope.launch {
            strGenres.forEach {genre -> run{
                when (genre) {
                    "fantasy" -> {
                        bookRepository.addUserBookHistory(
                                savedUser.userID,
                                1,
                                "view"
                        );
                    }
                    "horror" -> {
                        bookRepository.addUserBookHistory(
                                savedUser.userID,
                                2,
                                "view"
                        );
                    }
                    "adventure" -> {
                        bookRepository.addUserBookHistory(
                                savedUser.userID,
                                3,
                                "view"
                        );
                    }
                    "romance" -> {
                        bookRepository.addUserBookHistory(
                                savedUser.userID,
                                4,
                                "view"
                        );
                    }
                    "mystery" -> {
                        bookRepository.addUserBookHistory(
                                savedUser.userID,
                                5,
                                "view"
                        );
                    }
                }
            }};
        }

        userMetadataRepository.save(userMetadata);
        return ResponseEntity(
            MessageResponse("Successfully registered", status.value()),
            status
        )
    }
}