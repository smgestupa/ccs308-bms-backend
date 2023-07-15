package com.bms.backend.repositories

import com.bms.backend.models.user.User
import com.bms.backend.models.user.UserMetadata
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserMetadataRepository : JpaRepository<UserMetadata, Int> {

    fun existsByUserID(userID: Int): Boolean;
    fun existsByUsername(username: String): Boolean;
    fun findByUsername(username: String): Optional<UserMetadata>;
    fun existsByUserIDAndUsername(userID: Int, username: String): Boolean;
}