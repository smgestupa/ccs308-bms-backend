package com.bms.backend.security.services

import com.bms.backend.models.user.User
import com.bms.backend.models.user.UserMetadata
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import java.util.stream.Collectors

class UserDetailsImpl(
    username: String,
    password: String,
    authorities: List<GrantedAuthority>
): UserDetails {

    private val username: String;
    private val password: String;
    private val authorities: List<GrantedAuthority>;

    init {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    companion object {
        fun build( user: User): UserDetailsImpl {
            val authorities: List<GrantedAuthority> = user.role.stream()
                .map { role -> SimpleGrantedAuthority( role.type.name ) }
                .collect( Collectors.toList() );

            return UserDetailsImpl(
                user.userMetadata.username,
                user.userMetadata.password,
                authorities
            )
        }
    }

    override fun getAuthorities(): List<GrantedAuthority> {
        return authorities;
    }

    override fun getPassword(): String {
        return password;
    }

    override fun getUsername(): String {
        return username;
    }

    override fun isAccountNonExpired(): Boolean {
        return true;
    }

    override fun isAccountNonLocked(): Boolean {
        return true;
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true;
    }

    override fun isEnabled(): Boolean {
        return true;
    }

    override fun equals( other: Any? ): Boolean {
        if ( this === other ) return true;
        else if ( other == null || javaClass != other::class.java ) return false;

        val user: UserDetailsImpl = other as UserDetailsImpl;
        return Objects.equals(username, user.username);
    }
}