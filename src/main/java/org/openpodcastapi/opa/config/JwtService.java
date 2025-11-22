package org.openpodcastapi.opa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${jwt.ttl}")
    private String jwtExpiration;

    public long getExpirationTime() {
        return Long.parseLong(jwtExpiration);
    }
}
