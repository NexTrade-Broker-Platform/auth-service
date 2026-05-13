package com.lynx.auth_service.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // login buckets
    private final Map<String, Bucket> loginBuckets =
            new ConcurrentHashMap<>();

    // register buckets
    private final Map<String, Bucket> registerBuckets =
            new ConcurrentHashMap<>();


    private Bucket createLoginBucket() {

        Bandwidth limit = Bandwidth.classic(
                5,
                Refill.intervally(5, Duration.ofMinutes(15))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket createRegisterBucket() {

        Bandwidth limit = Bandwidth.classic(
                3,
                Refill.intervally(3, Duration.ofHours(1))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // only protect login/register
        if (
                !path.equals("/users/login")
                        && !path.equals("/users/register")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();

        // LOGIN RATE LIMIT
        if (path.equals("/users/login")) {

            Bucket bucket = loginBuckets.computeIfAbsent(
                    ip,
                    k -> createLoginBucket()
            );

            if (!bucket.tryConsume(1)) {

                response.setStatus(429);
                response.getWriter()
                        .write("Too many login attempts");

                return;
            }
        }

        // REGISTER RATE LIMIT
        if (path.equals("/users/register")) {

            Bucket bucket = registerBuckets.computeIfAbsent(
                    ip,
                    k -> createRegisterBucket()
            );

            if (!bucket.tryConsume(1)) {

                response.setStatus(429);
                response.getWriter()
                        .write("Too many registration attempts");

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
