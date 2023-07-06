package com.bms.backend.models.user

import com.bms.backend.models.book.BookMetadata
import com.bms.backend.models.role.Role
import javax.persistence.*

@Entity
@Table
class User constructor(
    photo: ByteArray?,
    firstName: String,
    lastName: String,
    bio: String?,
    role: Set<Role>
) {

    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy=GenerationType.AUTO)
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            joinColumns = [JoinColumn(name="user_id")],
            inverseJoinColumns = [JoinColumn(name="role_id")]
    )
    var role: Set<Role> = HashSet();

    @OneToOne(
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name="user_id")
    lateinit var userMetadata: UserMetadata;

    init {
        this.photo = photo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.role = role;
    }
}
