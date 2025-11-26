package org.openpodcastapi.opa.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openpodcastapi.opa.security.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/// A scheduled task to clean up expired refresh tokens
@Component
@RequiredArgsConstructor
@Log4j2
public class RefreshTokenCleanup {

    private final RefreshTokenRepository repository;

    /// Runs a task every hour to clean up expired refresh tokens
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void deleteExpiredTokens() {
        final int deleted = repository.deleteAllByExpiresAtBefore(Instant.now());
        log.info("Deleted {} expired refresh tokens", deleted);
    }
}