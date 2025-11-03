package org.openpodcastapi.opa.client;

import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Repository;

@Log4j2
@Repository
public class CustomRegisteredClientRepository extends JdbcRegisteredClientRepository {

    public CustomRegisteredClientRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void save(RegisteredClient client) {
        client.getRedirectUris().forEach(uri -> {
            if (!uri.startsWith("https://") && !uri.startsWith("myapp://")) {
                throw new IllegalArgumentException("Invalid redirect URI: " + uri);
            }
        });

        // Add defaults if missing
        var modified = RegisteredClient.from(client)
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(true)
                        .build())
                .build();

        log.info("Registering new OAuth client: {}", modified.getClientId());
        super.save(modified);
    }
}
