package org.openpodcastapi.opa.util;

import org.openpodcastapi.opa.security.RefreshTokenRepository;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.slf4j.LoggerFactory.getLogger;

/// A scheduled task to clean up expired refresh tokens
@Component
public class RefreshTokenCleanup {

    private static final Logger log = getLogger(RefreshTokenCleanup.class);
    private final RefreshTokenRepository repository;

    /// Required-args constructor
    ///
    /// @param repository the refresh token repository for handling refresh token interactions
    public RefreshTokenCleanup(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    /// Runs a task every hour to clean up expired refresh tokens
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void deleteExpiredTokens() {
        final int deleted = repository.deleteAllByExpiresAtBefore(Instant.now());
        log.info("Deleted {} expired refresh tokens", deleted);
    }
}