package com.finalProjectLedZeppelin.auth.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = JwtService.class)
class JwtServiceContextTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void contextLoads_andJwtServiceUsesApplicationYamlProperties() {
        // given
        long userId = 7L;
        String email = "spring@test.com";
        String role = "ADMIN";
        // when
        String token = jwtService.generateAccessToken(userId, email, role);
        var claims = jwtService.parse(token);
        // then
        assertNotNull(jwtService);
        assertNotNull(token);
        assertEquals(email, claims.getSubject());
        assertEquals(userId, claims.get("uid", Number.class).longValue());
        assertEquals(role, claims.get("role", String.class));
    }
}
