package org.openpodcastapi.opa.helpers;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;

import java.util.UUID;

/// A helper class containing methods for validating UUID values
public class UUIDHelper {

    /// The podcasting namespace UUID
    static final UUID podcastNamespace = UUID.fromString("ead4c236-bf58-58c6-a2c6-a6b28d128cb6");
    /// A generator that calculates podcast UUID values from feed URLs using the podcast index namespace
    static final NameBasedGenerator generator = Generators.nameBasedGenerator(podcastNamespace);

    private UUIDHelper() {
        throw new IllegalStateException("Class shouldn't be instantiated");
    }

    /// Sanitizes a feed URL by stripping the scheme and any trailing slashes
    ///
    /// @param feedUrl the URL to sanitize
    /// @return the sanitized URL
    public static String sanitizeFeedUrl(String feedUrl) {
        if (feedUrl == null || feedUrl.isBlank()) {
            throw new IllegalArgumentException("Invalid feed URL passed to function");
        }

        // Reject unsupported schemes (e.g., ftp://)
        if (feedUrl.matches("^[a-zA-Z]+://.*") && !feedUrl.startsWith("http://") && !feedUrl.startsWith("https://")) {
            throw new IllegalArgumentException("Invalid feed URL passed to function");
        }

        String sanitized = feedUrl.replaceFirst("^(https?://)", "").replaceAll("/+$", "");

        if (!sanitized.contains(".")) {
            throw new IllegalArgumentException("Invalid feed URL passed to function");
        }

        return sanitized;
    }

    /// Calculates the UUID of a provided feed URL using Podcast index methodology.
    ///
    /// See [the Podcast index's documentation](https://github.com/Podcastindex-org/podcast-namespace/blob/main/docs/tags/guid.md)
    /// for more information
    ///
    /// @param feedUrl the URL of the podcast feed
    /// @return the calculated UUID
    public static UUID getFeedUUID(String feedUrl) {
        final String sanitizedFeedUrl = sanitizeFeedUrl(feedUrl);
        return generator.generate(sanitizedFeedUrl);
    }

    /// Validates that a supplied subscription UUID has been calculated properly
    ///
    /// @param feedUrl      the URL of the podcast feed
    /// @param suppliedUUID the UUID to validate
    /// @return whether the UUID values strictly match
    public static boolean validateSubscriptionUUID(String feedUrl, UUID suppliedUUID) {
        UUID calculatedUUID = getFeedUUID(feedUrl);
        return calculatedUUID.equals(suppliedUUID);
    }

    /// Validates that a string is a valid UUID
    ///
    /// @param uuid the UUID string to validate
    /// @return `true` if the string is a valid UUID
    public static boolean validateUUIDString(String uuid) {
        try {
            UUID result = UUID.fromString(uuid);
            return !result.toString().isEmpty();
        } catch (IllegalArgumentException _) {
            return false;
        }
    }
}
