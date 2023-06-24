package com.bms.backend.models.role

import com.bms.backend.enums.EnumRole
import javax.persistence.*

@Entity
@Table
class Role {

    @Id
    @Column(name="role_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val roleID: Int = 0;

    @Enumerated(EnumType.STRING)
    @Column(
            length=16,
            nullable=false
    )
    lateinit var label: EnumRole;

}