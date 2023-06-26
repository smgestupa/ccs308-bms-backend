package com.bms.backend.repositories

import com.bms.backend.models.user.User
import com.bms.backend.models.user.UserMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Int> {

    fun existsByUserID(userID: Int): Boolean;
    fun findByUserID(userID: Int): Optional<User>;
}