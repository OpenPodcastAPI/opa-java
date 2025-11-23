package org.openpodcastapi.opa.subscriptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openpodcastapi.opa.config.JwtAuthenticationFilter;
import org.openpodcastapi.opa.subscription.*;
import org.openpodcastapi.opa.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserSubscriptionMapperImpl.class)
class UserSubscriptionEntityMapperTest {
    @Autowired
    private UserSubscriptionMapper mapper;

    @MockitoBean
    private UserSubscriptionRepository userSubscriptionRepository;

    @MockitoBean
    private JwtAuthenticationFilter filter;

    /// Tests that a [UserSubscriptionEntity] entity maps to a [SubscriptionDTO.UserSubscriptionDTO] representation
    @Test
    void testToDto() {
        final Instant timestamp = Instant.now();
        final UUID uuid = UUID.randomUUID();
        UserEntity userEntity = UserEntity.builder()
                .uuid(UUID.randomUUID())
                .username("test")
                .email("test@test.test")
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();

        SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                .uuid(UUID.randomUUID())
                .feedUrl("test.com/feed1")
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();

        UserSubscriptionEntity userSubscriptionEntity = UserSubscriptionEntity.builder()
                .uuid(uuid)
                .userEntity(userEntity)
                .subscription(subscriptionEntity)
                .isSubscribed(true)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();

        SubscriptionDTO.UserSubscriptionDTO dto = mapper.toDto(userSubscriptionEntity);
        assertNotNull(dto);

        // The DTO should inherit the feed URL from the SubscriptionEntity
        assertEquals(subscriptionEntity.getFeedUrl(), dto.feedUrl());

        // The DTO should use the SubscriptionEntity's UUID rather than the UserSubscriptionEntity's
        assertEquals(subscriptionEntity.getUuid(), dto.uuid());
        assertTrue(dto.isSubscribed());
    }
}
