package com.bms.backend.repositories

import com.bms.backend.enums.EnumRole
import com.bms.backend.models.role.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<Role, Int> {

    fun findByType(label: EnumRole): Optional<Role>;
}