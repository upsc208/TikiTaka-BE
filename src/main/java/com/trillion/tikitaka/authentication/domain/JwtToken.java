package com.trillion.tikitaka.authentication.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "jwt_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String refreshToken;
    private Date expiration;

    @Builder
    public JwtToken(String username, String refreshToken, Date expiration) {
        this.username = username;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }
}
