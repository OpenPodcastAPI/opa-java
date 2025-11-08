package org.openpodcastapi.opa.helpers;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.openpodcastapi.opa.helpers.UUIDHelper.*;

class UUIDHelperTest {
    @Test
    void sanitizeFeedUrl_shouldSanitizeValidUrl() {
        final String feedUrl = "https://test.com/feed1/";
        final String expectedUrl = "test.com/feed1";
        String cleanedUrl = sanitizeFeedUrl(feedUrl);

        assertEquals(expectedUrl, cleanedUrl);
    }

    @Test
    void sanitizeFeedUrl_shouldSanitizeUrlWithoutScheme() {
        final String feedUrl = "test.com/feed1";
        final String expectedUrl = "test.com/feed1";
        String cleanedUrl = sanitizeFeedUrl(feedUrl);

        assertEquals(expectedUrl, cleanedUrl);
    }

    @Test
    void sanitizeFeedUrl_shouldThrowOnInvalidUrl() {
        final String feedUrl = "ftp://test.com/feed1";
        final String expectedMessage = "Invalid feed URL passed to function";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> sanitizeFeedUrl(feedUrl));

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    void getFeedUUID_shouldReturnGeneratedUUID() {
        final String feedUrl = "podnews.net/rss";
        final UUID expectedUUID = UUID.fromString("9b024349-ccf0-5f69-a609-6b82873eab3c");

        UUID calculatedUUID = getFeedUUID(feedUrl);

        assertEquals(expectedUUID, calculatedUUID);
    }

    @Test
    void getFeedUUID_shouldReturnDeterministicUUID() {
        final String feedUrl = "podnews.net/rss";
        final UUID incorrectUUID = UUID.fromString("d5d5520d-81da-474e-928b-5fa66233a1ac");

        UUID calculatedUUID = getFeedUUID(feedUrl);

        assertNotEquals(incorrectUUID, calculatedUUID);
    }

    @Test
    void validateSubscriptionUUID_shouldReturnTrueWhenValid() {
        final String feedUrl = "podnews.net/rss";
        final UUID expectedUUID = UUID.fromString("9b024349-ccf0-5f69-a609-6b82873eab3c");

        assertTrue(validateSubscriptionUUID(feedUrl, expectedUUID));
    }

    @Test
    void validateSubscriptionUUID_shouldReturnFalseWhenInvalid() {
        final String feedUrl = "podnews.net/rss";
        final UUID incorrectUUID = UUID.fromString("d5d5520d-81da-474e-928b-5fa66233a1ac");

        assertFalse(validateSubscriptionUUID(feedUrl, incorrectUUID));
    }

    @Test
    void validateUUIDString_shouldReturnTrueWhenValid() {
        final String validUUID = "d5d5520d-81da-474e-928b-5fa66233a1ac";

        assertTrue(validateUUIDString(validUUID));
    }

    @Test
    void validateUUIDString_shouldReturnFalseWhenInvalid() {
        final String validUUID = "not-a-uuid";

        assertFalse(validateUUIDString(validUUID));
    }
}
