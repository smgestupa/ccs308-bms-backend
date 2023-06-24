package com.bms.backend.models.user

import com.bms.backend.models.role.Role
import javax.persistence.*

@Entity
@Table
class User constructor(
    photo: ByteArray?,
    firstName: String,
    lastName: String,
    bio: String?,
    username: String,
    password: String,
    role: Set<Role>
) {

    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    val userID: Int = 0;

    @Column
    val photo: ByteArray?;

    @Column(
            name="first_name",
            length=64,
            nullable=false
    )
    val firstName: String;

    @Column(
            name="last_name",
            length=64,
            nullable=false
    )
    val lastName: String;

    @Column(length=64)
    val bio: String?;

    @Column(
            length=32,
            nullable=false
    )
    val username: String;

    @Column(
            length=64,
            nullable=false
    )
    val password: String;

    @ManyToMany( fetch = FetchType.LAZY )
    @JoinTable(
            joinColumns = [JoinColumn(name="user_id")],
            inverseJoinColumns = [JoinColumn(name="role_id")]
    )
    var role: Set<Role> = HashSet();

    init {
        this.photo = photo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
