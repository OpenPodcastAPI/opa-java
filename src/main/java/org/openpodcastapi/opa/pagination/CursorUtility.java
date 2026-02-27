package org.openpodcastapi.opa.pagination;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/// Utility class for pagination operations
public final class CursorUtility {

    /// The object mapper used to construct the JSON payload
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /// No-args constructor
    private CursorUtility() {
    }

    /// Encodes a pagination payload to a string for use in database queries
    ///
    /// @param payload the pagination payload to encode
    /// @return an encoded pagination as a String
    public static String encode(CursorPayload payload) {
        try {
            String json = MAPPER.writeValueAsString(payload);
            return Base64.getUrlEncoder()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode pagination", e);
        }
    }

    /// Decodes a pagination from a String
    ///
    /// @param cursor the encoded pagination value
    /// @return a pagination payload decoded from the provided string
    public static CursorPayload decode(String cursor) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            return MAPPER.readValue(decoded, CursorPayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pagination", e);
        }
    }
}
