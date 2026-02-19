package org.openpodcastapi.opa.subscriptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openpodcastapi.opa.feed.FeedEntity;
import org.openpodcastapi.opa.subscription.*;
import org.openpodcastapi.opa.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SubscriptionMapperImpl.class)
class SubscriptionMapperTest {
    @Autowired
    private SubscriptionMapper mapper;

    @MockitoBean
    private SubscriptionRepository subscriptionRepository;

    /// Tests that a [SubscriptionEntity] entity maps to a [SubscriptionDTO.UserSubscriptionDTO] representation
    @Test
    void testToDto() {
        final var uuid = UUID.randomUUID();
        final var userEntity = new UserEntity(1L, UUID.randomUUID(), "test", "test@test.test");

        final var feed = new FeedEntity(UUID.randomUUID(), "test.com/feed1");

        final var userSubscriptionEntity = new SubscriptionEntity(uuid, userEntity, feed);

        SubscriptionDTO.UserSubscriptionDTO dto = mapper.toDto(userSubscriptionEntity);
        assertNotNull(dto);

        // The DTO should inherit the feed URL from the SubscriptionEntity
        assertEquals(feed.getFeedUrl(), dto.feedUrl());

        // The DTO should use the SubscriptionEntity's UUID rather than the SubscriptionEntity's
        assertEquals(feed.getUuid(), dto.uuid());
        assertNull(dto.unsubscribedAt());
    }
}
