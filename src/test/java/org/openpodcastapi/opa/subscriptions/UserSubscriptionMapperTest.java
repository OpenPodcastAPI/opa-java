package org.openpodcastapi.opa.subscriptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openpodcastapi.opa.subscription.*;
import org.openpodcastapi.opa.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserSubscriptionMapperImpl.class)
class UserSubscriptionMapperTest {
    @Autowired
    private UserSubscriptionMapper mapper;

    @MockitoBean
    private UserSubscriptionRepository userSubscriptionRepository;

    /// Tests that a [UserSubscriptionEntity] entity maps to a [SubscriptionDTO.UserSubscriptionDTO] representation
    @Test
    void testToDto() {
        final var uuid = UUID.randomUUID();
        final var userEntity = new UserEntity(1L, UUID.randomUUID(), "test", "test@test.test");

        final var subscriptionEntity = new SubscriptionEntity(UUID.randomUUID(), "test.com/feed1");

        final var userSubscriptionEntity = new UserSubscriptionEntity(uuid, userEntity, subscriptionEntity);

        SubscriptionDTO.UserSubscriptionDTO dto = mapper.toDto(userSubscriptionEntity);
        assertNotNull(dto);

        // The DTO should inherit the feed URL from the SubscriptionEntity
        assertEquals(subscriptionEntity.getFeedUrl(), dto.feedUrl());

        // The DTO should use the SubscriptionEntity's UUID rather than the UserSubscriptionEntity's
        assertEquals(subscriptionEntity.getUuid(), dto.uuid());
        assertNull(dto.unsubscribedAt());
    }
}
