package com.bms.backend.security.jwt

import com.bms.backend.security.services.UserDetailsImpl
import io.jsonwebtoken.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.util.*

@Component
class JwtUtils {

    @Value( "\${bms.app.jwtSecret}" )
    lateinit var jwtSecret: String;

    @Value( "\${bms.app.jwtExpirationMs}" )
    var jwtExpirationMs: Int = 0;

    fun generateJwtToken( authentication: Authentication ): String {
        val userPrincipal: UserDetailsImpl = authentication.principal as UserDetailsImpl;

        return Jwts.builder()
            .setSubject( ( userPrincipal.username ) )
            .setIssuedAt( Date() )
            .setExpiration( Date( Date().time + jwtExpirationMs ) )
            .signWith( SignatureAlgorithm.HS512, jwtSecret )
            .compact();
    }

    fun getUserNameFromJwtToken( token: String ): String {
        return Jwts.parser()
            .setSigningKey( jwtSecret )
            .parseClaimsJws( token )
            .body
            .subject;
    }

    fun validateJwtToken( authToken: String ): Boolean {
        try {
            Jwts.parser().setSigningKey( jwtSecret ).parseClaimsJws( authToken );
            return true;
        } catch ( e: SignatureException ) {
            System.err.println( "Invalid JWT signature: ${ e.message }" );
        } catch ( e: MalformedJwtException ) {
            System.err.println( "Invalid JWT token: ${ e.message }" );
        } catch ( e: ExpiredJwtException ) {
            System.err.println( "JWT token has expired: ${ e.message }" );
        } catch ( e: UnsupportedJwtException ) {
            System.err.println( "JWT token is unsupported: ${ e.message }" );
        } catch ( e: IllegalArgumentException ) {
            System.err.println( "JWT claims string is empty: ${ e.message }" );
        }

        return false;
    }
}