package org.openpodcastapi.opa.user;

/// The roles associated with users. All users have `USER` permissions.
/// Admins require the `ADMIN` role to perform administrative functions.
public enum UserRoles {
    USER,
    ADMIN,
}
