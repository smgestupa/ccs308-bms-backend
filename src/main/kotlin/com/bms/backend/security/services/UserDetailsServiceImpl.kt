package com.bms.backend.security.services

import com.bms.backend.models.user.User
import com.bms.backend.models.user.UserMetadata
import com.bms.backend.repositories.UserMetadataRepository
import com.bms.backend.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class UserDetailsServiceImpl : UserDetailsService {

    @Autowired
    private lateinit var userRepository: UserRepository;

    @Autowired
    private lateinit var userMetadataRepository: UserMetadataRepository;

    @Transactional
    @Throws( UsernameNotFoundException::class )
    override fun loadUserByUsername( username: String ): UserDetails {
        val userMetadata: UserMetadata = userMetadataRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException( "User doesn't exist with given username: $username" ) };

        val user: User = userRepository.findByUserID(userMetadata.userID)
            .orElseThrow { UsernameNotFoundException( "User doesn't exist with given ID: ${userMetadata.userID}" ) };

        return UserDetailsImpl.build( user );
    }
}