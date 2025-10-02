package com.salayo.locallifebackend.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.response.ErrorResponse;
import com.salayo.locallifebackend.global.security.MemberDetails;
import com.salayo.locallifebackend.global.security.jwt.JwtProvider;
import com.salayo.locallifebackend.global.util.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
        throws ServletException, IOException {

        String token = jwtProvider.resolveToken(httpServletRequest.getHeader(JwtProvider.AUTH_HEADER));

        try {
            if (token != null) {
                jwtProvider.validateTokenOrThrow(token);
                String email = jwtProvider.getUsernameFromToken(token);

                Member member = memberRepository.findByEmail(email).orElse(null);

                if (member == null) {
                    log.warn("Member Not Found: {}", email);
                    setErrorResponse(httpServletResponse, ErrorCode.MEMBER_NOT_FOUND, httpServletRequest);
                    return;
                }

                String storedAccessToken = redisUtil.getAccessToken(member.getId());
                if (storedAccessToken != null && !storedAccessToken.equals(token)) {
                    log.warn("AccessToken mismatch: 다른 기기에서 로그인 되었습니다.");
                    setErrorResponse(httpServletResponse, ErrorCode.DUPLICATE_LOGIN_DETECTED, httpServletRequest);
                    return;
                }

                MemberDetails memberDetails = new MemberDetails(member);

                String role = jwtProvider.getRoleFromToken(token);

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    memberDetails, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            setErrorResponse(httpServletResponse, ErrorCode.TOKEN_EXPIRED, httpServletRequest);
            return;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT format");
            setErrorResponse(httpServletResponse, ErrorCode.TOKEN_MALFORMED, httpServletRequest);
            return;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
            setErrorResponse(httpServletResponse, ErrorCode.TOKEN_UNSUPPORTED, httpServletRequest);
            return;
        } catch (IllegalArgumentException e) {
            log.error("JWT token is empty or null");
            setErrorResponse(httpServletResponse, ErrorCode.TOKEN_ILLEGAL, httpServletRequest);
            return;
        } catch (SecurityException e) {
            log.error("JWT signature does not match");
            setErrorResponse(httpServletResponse, ErrorCode.TOKEN_SIGNATURE_INVALID, httpServletRequest);
            return;
        } catch (Exception e) {
            log.error("Failed to validate JWT token", e);
            setErrorResponse(httpServletResponse, ErrorCode.INTERNAL_SERVER_ERROR, httpServletRequest);
            return;
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void setErrorResponse(HttpServletResponse httpServletResponse, ErrorCode errorCode, HttpServletRequest httpServletRequest)
        throws IOException {

        httpServletResponse.setStatus(errorCode.getStatus().value());
        httpServletResponse.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, httpServletRequest.getRequestURI(), httpServletRequest.getMethod());

        String json = objectMapper.writeValueAsString(errorResponse);
        httpServletResponse.getWriter().write(json);
        httpServletResponse.flushBuffer();
    }

}
