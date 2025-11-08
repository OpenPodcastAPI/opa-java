package org.openpodcastapi.opa.subscriptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openpodcastapi.opa.subscription.dto.UserSubscriptionDto;
import org.openpodcastapi.opa.subscription.mapper.UserSubscriptionMapper;
import org.openpodcastapi.opa.subscription.mapper.UserSubscriptionMapperImpl;
import org.openpodcastapi.opa.subscription.model.Subscription;
import org.openpodcastapi.opa.subscription.model.UserSubscription;
import org.openpodcastapi.opa.subscription.repository.UserSubscriptionRepository;
import org.openpodcastapi.opa.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserSubscriptionMapperImpl.class)
class UserSubscriptionMapperTest {
    @Autowired
    private UserSubscriptionMapper mapper;

    @MockitoBean
    private UserSubscriptionRepository userSubscriptionRepository;

    /// Tests that a [UserSubscription] entity maps to a [UserSubscriptionDto] representation
    @Test
    void testToDto() {
        final Instant timestamp = Instant.now();
        final UUID uuid = UUID.randomUUID();
        User user = User.builder()
                .uuid(UUID.randomUUID())
                .username("test")
                .email("test@test.test")
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();

        Subscription subscription = Subscription.builder()
                .uuid(UUID.randomUUID())
                .feedUrl("test.com/feed1")
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();

        UserSubscription userSubscription = UserSubscription.builder()
                .uuid(uuid)
                .user(user)
                .subscription(subscription)
                .isSubscribed(true)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();

        UserSubscriptionDto dto = mapper.toDto(userSubscription);
        assertNotNull(dto);

        // The DTO should inherit the feed URL from the Subscription
        assertEquals(subscription.getFeedUrl(), dto.feedUrl());

        // The DTO should use the Subscription's UUID rather than the UserSubscription's
        assertEquals(subscription.getUuid(), dto.uuid());
        assertTrue(dto.isSubscribed());
    }
}
