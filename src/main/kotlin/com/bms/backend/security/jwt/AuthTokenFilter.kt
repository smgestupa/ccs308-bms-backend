package com.bms.backend.security.jwt

import com.bms.backend.security.services.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthTokenFilter : OncePerRequestFilter() {

    @Autowired
    private lateinit var jwtUtils: JwtUtils;

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl;

    @Throws( ServletException::class, IOException::class )
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt: String? = parseJwt( request );

            jwt?.let {
                if ( !jwtUtils.validateJwtToken( jwt ) ) return;

                val username: String = jwtUtils.getUserNameFromJwtToken( jwt );
                val userDetails: UserDetails = userDetailsService.loadUserByUsername( username );
                val authentication: UsernamePasswordAuthenticationToken
                        = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                );

                authentication.details = WebAuthenticationDetailsSource().buildDetails( request );
                SecurityContextHolder.getContext().authentication = authentication;
            }
        } catch ( e: Exception ) {
            System.err.println( "Cannot set user authentication: $e" );
        }

        filterChain.doFilter( request, response );
    }

    private fun parseJwt( request: HttpServletRequest ): String? {
        val headerAuth: String = request.getHeader( "Authorization" );
        if ( StringUtils.hasText( headerAuth ) && headerAuth.startsWith( "Bearer " ) )
            return headerAuth.substring( 7, headerAuth.length );

        return null;
    }

    override fun shouldNotFilter( request: HttpServletRequest ): Boolean {
        return filterURL( request.servletPath );
    }

    private fun filterURL( servletPath: String ): Boolean {
        return when ( servletPath ) {
            "**/user/login" -> true;
            "**/user/register" -> true;
            else -> false;
        }
    }
}