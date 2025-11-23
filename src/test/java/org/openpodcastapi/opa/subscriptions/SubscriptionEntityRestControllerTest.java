package org.openpodcastapi.opa.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openpodcastapi.opa.service.CustomUserDetails;
import org.openpodcastapi.opa.subscription.SubscriptionDTO;
import org.openpodcastapi.opa.subscription.SubscriptionService;
import org.openpodcastapi.opa.user.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class SubscriptionEntityRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @Test
    @WithMockUser(username = "alice")
    void getAllSubscriptionsForUser_shouldReturnSubscriptions() throws Exception {
        CustomUserDetails user = new CustomUserDetails(1L, UUID.randomUUID(), "alice", "alice@test.com", Set.of(UserRoles.USER));

        SubscriptionDTO.UserSubscriptionDTO sub1 = new SubscriptionDTO.UserSubscriptionDTO(UUID.randomUUID(), "test.com/feed1", Instant.now(), Instant.now(), true);
        SubscriptionDTO.UserSubscriptionDTO sub2 = new SubscriptionDTO.UserSubscriptionDTO(UUID.randomUUID(), "test.com/feed2", Instant.now(), Instant.now(), true);
        Page<SubscriptionDTO.UserSubscriptionDTO> page = new PageImpl<>(List.of(sub1, sub2));

        when(subscriptionService.getAllActiveSubscriptionsForUser(eq(user.id()), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/subscriptions")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities())))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptions.length()").value(2))
                .andDo(document("subscriptions-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("The page number to fetch").optional(),
                                parameterWithName("size").description("The number of results to include on each page").optional(),
                                parameterWithName("includeUnsubscribed")
                                        .optional()
                                        .description("If true, includes unsubscribed feeds in the results. Defaults to false.")
                        ),
                        responseFields(
                                fieldWithPath("subscriptions[].uuid").description("The UUID of the subscriptionEntity").type(JsonFieldType.STRING),
                                fieldWithPath("subscriptions[].feedUrl").description("The feed URL of the subscriptionEntity").type(JsonFieldType.STRING),
                                fieldWithPath("subscriptions[].createdAt").description("Creation timestamp of the subscriptionEntity").type(JsonFieldType.STRING),
                                fieldWithPath("subscriptions[].updatedAt").description("Last update timestamp of the subscriptionEntity").type(JsonFieldType.STRING),
                                fieldWithPath("subscriptions[].isSubscribed").description("Whether the user is subscribed to the feed").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("page").description("Current page number").type(JsonFieldType.NUMBER),
                                fieldWithPath("size").description("Size of the page").type(JsonFieldType.NUMBER),
                                fieldWithPath("totalElements").description("Total number of subscriptions").type(JsonFieldType.NUMBER),
                                fieldWithPath("totalPages").description("Total number of pages").type(JsonFieldType.NUMBER),
                                fieldWithPath("first").description("Whether this is the first page").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("last").description("Whether this is the last page").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("numberOfElements").description("Number of subscriptions on the current page").type(JsonFieldType.NUMBER)
                        )
                ));
    }

    @Test
    @WithMockUser(username = "alice")
    void getAllSubscriptionsForUser_shouldIncludeUnsubscribedWhenRequested() throws Exception {
        CustomUserDetails user = new CustomUserDetails(
                1L, UUID.randomUUID(), "alice", "alice@test.com",
                Set.of(UserRoles.USER)
        );

        SubscriptionDTO.UserSubscriptionDTO sub1 = new SubscriptionDTO.UserSubscriptionDTO(UUID.randomUUID(), "test.com/feed1", Instant.now(), Instant.now(), true);
        SubscriptionDTO.UserSubscriptionDTO sub2 = new SubscriptionDTO.UserSubscriptionDTO(UUID.randomUUID(), "test.com/feed2", Instant.now(), Instant.now(), false);
        Page<SubscriptionDTO.UserSubscriptionDTO> page = new PageImpl<>(List.of(sub1, sub2));

        when(subscriptionService.getAllSubscriptionsForUser(eq(user.id()), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/subscriptions")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities())))
                        .param("includeUnsubscribed", "true"))
                .andExpect(status().isOk())
                .andDo(document("subscriptions-list-with-unsubscribed",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }


    @Test
    @WithMockUser(username = "alice")
    void getSubscriptionByUuid_shouldReturnSubscription() throws Exception {
        CustomUserDetails user = new CustomUserDetails(1L, UUID.randomUUID(), "alice", "alice@test.com", Set.of(UserRoles.USER));
        UUID subscriptionUuid = UUID.randomUUID();

        SubscriptionDTO.UserSubscriptionDTO sub = new SubscriptionDTO.UserSubscriptionDTO(subscriptionUuid, "test.com/feed1", Instant.now(), Instant.now(), true);
        when(subscriptionService.getUserSubscriptionBySubscriptionUuid(subscriptionUuid, user.id()))
                .thenReturn(sub);

        mockMvc.perform(get("/api/v1/subscriptions/{uuid}", subscriptionUuid)
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities()))))
                .andExpect(status().isOk())
                .andDo(document("subscriptionEntity-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("uuid").description("UUID of the subscriptionEntity to retrieve")
                        ),
                        responseFields(
                                fieldWithPath("uuid").description("The UUID of the subscriptionEntity").type(JsonFieldType.STRING),
                                fieldWithPath("feedUrl").description("The feed URL of the subscriptionEntity").type(JsonFieldType.STRING),
                                fieldWithPath("createdAt").description("Creation timestamp").type(JsonFieldType.STRING),
                                fieldWithPath("updatedAt").description("Last update timestamp").type(JsonFieldType.STRING),
                                fieldWithPath("isSubscribed").description("Whether the user is subscribed to the feed").type(JsonFieldType.BOOLEAN)
                        )
                ));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createUserSubscriptions_shouldReturnMixedResponse() throws Exception {
        final CustomUserDetails user = new CustomUserDetails(1L, UUID.randomUUID(), "testuser", "test@test.com", Set.of(UserRoles.USER));
        final Instant timestamp = Instant.now();

        final UUID goodFeedUUID = UUID.randomUUID();
        final String BAD_UUID = "62ad30ce-aac0-4f0a-a811";

        SubscriptionDTO.SubscriptionCreateDTO dto1 = new SubscriptionDTO.SubscriptionCreateDTO(goodFeedUUID.toString(), "test.com/feed1");
        SubscriptionDTO.SubscriptionCreateDTO dto2 = new SubscriptionDTO.SubscriptionCreateDTO(BAD_UUID, "test.com/feed2");

        SubscriptionDTO.BulkSubscriptionResponseDTO response = new SubscriptionDTO.BulkSubscriptionResponseDTO(
                List.of(new SubscriptionDTO.UserSubscriptionDTO(goodFeedUUID, "test.com/feed1", timestamp, timestamp, true)),
                List.of(new SubscriptionDTO.SubscriptionFailureDTO(BAD_UUID, "test.com/feed2", "invalid UUID format"))
        );

        when(subscriptionService.addSubscriptions(anyList(), eq(user.id())))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(dto1, dto2))))
                .andExpect(status().isMultiStatus())
                .andDo(document("subscriptions-bulk-create-mixed",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("[].uuid").description("The UUID of the subscriptionEntity"),
                                fieldWithPath("[].feedUrl").description("The feed URL of the subscriptionEntity to create")
                        ),
                        responseFields(
                                fieldWithPath("success[]").description("List of feed URLs successfully added").type(JsonFieldType.ARRAY),
                                fieldWithPath("success[].uuid").description("The UUID of the feed").type(JsonFieldType.STRING),
                                fieldWithPath("success[].feedUrl").description("The feed URL").type(JsonFieldType.STRING),
                                fieldWithPath("success[].createdAt").description("The timestamp at which the subscriptionEntity was created").type(JsonFieldType.STRING),
                                fieldWithPath("success[].updatedAt").description("The timestamp at which the subscriptionEntity was updated").type(JsonFieldType.STRING),
                                fieldWithPath("success[].isSubscribed").description("Whether the user is subscribed to the feed").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("failure[]").description("List of feed URLs that failed to add").type(JsonFieldType.ARRAY),
                                fieldWithPath("failure[].uuid").description("The UUID of the feed").type(JsonFieldType.STRING),
                                fieldWithPath("failure[].feedUrl").description("The feed URL").type(JsonFieldType.STRING),
                                fieldWithPath("failure[].message").description("The error message").type(JsonFieldType.STRING)
                        )
                ));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createUserSubscription_shouldReturnSuccess() throws Exception {
        final CustomUserDetails user = new CustomUserDetails(1L, UUID.randomUUID(), "testuser", "test@test.com", Set.of(UserRoles.USER));

        final UUID goodFeedUUID = UUID.randomUUID();
        final Instant timestamp = Instant.now();

        SubscriptionDTO.SubscriptionCreateDTO dto = new SubscriptionDTO.SubscriptionCreateDTO(goodFeedUUID.toString(), "test.com/feed1");

        SubscriptionDTO.BulkSubscriptionResponseDTO response = new SubscriptionDTO.BulkSubscriptionResponseDTO(
                List.of(new SubscriptionDTO.UserSubscriptionDTO(goodFeedUUID, "test.com/feed1", timestamp, timestamp, true)),
                List.of()
        );

        when(subscriptionService.addSubscriptions(anyList(), eq(user.id())))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(dto))))
                .andExpect(status().is2xxSuccessful())
                .andDo(document("subscriptions-bulk-create-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("[].uuid").description("The UUID of the subscriptionEntity"),
                                fieldWithPath("[].feedUrl").description("The feed URL of the subscriptionEntity to create")
                        ),
                        responseFields(
                                fieldWithPath("success[]").description("List of feed URLs successfully added").type(JsonFieldType.ARRAY),
                                fieldWithPath("success[].uuid").description("The UUID of the feed").type(JsonFieldType.STRING),
                                fieldWithPath("success[].feedUrl").description("The feed URL").type(JsonFieldType.STRING),
                                fieldWithPath("success[].createdAt").description("The timestamp at which the subscriptionEntity was created").type(JsonFieldType.STRING),
                                fieldWithPath("success[].updatedAt").description("The timestamp at which the subscriptionEntity was updated").type(JsonFieldType.STRING),
                                fieldWithPath("success[].isSubscribed").description("Whether the user is subscribed to the feed").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("failure[]").description("List of feed URLs that failed to add").type(JsonFieldType.ARRAY).ignored())));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createUserSubscription_shouldReturnFailure() throws Exception {
        final CustomUserDetails user = new CustomUserDetails(1L, UUID.randomUUID(), "testuser", "test@test.com", Set.of(UserRoles.USER));

        final String BAD_UUID = "62ad30ce-aac0-4f0a-a811";

        SubscriptionDTO.SubscriptionCreateDTO dto = new SubscriptionDTO.SubscriptionCreateDTO(BAD_UUID, "test.com/feed2");

        SubscriptionDTO.BulkSubscriptionResponseDTO response = new SubscriptionDTO.BulkSubscriptionResponseDTO(
                List.of(),
                List.of(new SubscriptionDTO.SubscriptionFailureDTO(BAD_UUID, "test.com/feed2", "invalid UUID format"))
        );

        when(subscriptionService.addSubscriptions(anyList(), eq(user.id())))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities())))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(dto))))
                .andExpect(status().isBadRequest())
                .andDo(document("subscriptions-bulk-create-failure",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("success[]").description("List of feed URLs successfully added").type(JsonFieldType.ARRAY).ignored(),
                                fieldWithPath("failure[]").description("List of feed URLs that failed to add").type(JsonFieldType.ARRAY),
                                fieldWithPath("failure[].uuid").description("The UUID of the feed").type(JsonFieldType.STRING),
                                fieldWithPath("failure[].feedUrl").description("The feed URL").type(JsonFieldType.STRING),
                                fieldWithPath("failure[].message").description("The error message").type(JsonFieldType.STRING)
                        )));
    }

    @Test
    @WithMockUser(username = "alice")
    void updateSubscriptionStatus_shouldReturnUpdatedSubscription() throws Exception {
        CustomUserDetails user = new CustomUserDetails(
                1L,
                UUID.randomUUID(),
                "alice",
                "alice@test.com",
                Set.of(UserRoles.USER)
        );

        UUID subscriptionUuid = UUID.randomUUID();
        boolean newStatus = false;

        SubscriptionDTO.UserSubscriptionDTO updatedSubscription = new SubscriptionDTO.UserSubscriptionDTO(
                subscriptionUuid,
                "test.com/feed1",
                Instant.now(),
                Instant.now(),
                newStatus
        );

        when(subscriptionService.unsubscribeUserFromFeed(subscriptionUuid, user.id()))
                .thenReturn(updatedSubscription);

        // Act & Assert
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/v1/subscriptions/{uuid}/unsubscribe", subscriptionUuid)
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, "password", user.getAuthorities())))
                        .with(csrf().asHeader())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(subscriptionUuid.toString()))
                .andExpect(jsonPath("$.feedUrl").value("test.com/feed1"))
                .andExpect(jsonPath("$.isSubscribed").value(false))
                .andDo(document("subscriptionEntity-unsubscribe",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("uuid").description("UUID of the subscriptionEntity to update")
                        ),
                        responseFields(
                                fieldWithPath("uuid").description("The UUID of the subscriptionEntity").type(JsonFieldType.STRING),
                                fieldWithPath("feedUrl").description("The feed URL of the subscriptionEntity").type(JsonFieldType.STRING),
                                fieldWithPath("createdAt").description("When the subscriptionEntity was created").type(JsonFieldType.STRING),
                                fieldWithPath("updatedAt").description("When the subscriptionEntity was last updated").type(JsonFieldType.STRING),
                                fieldWithPath("isSubscribed").description("The updated subscriptionEntity status").type(JsonFieldType.BOOLEAN)
                        )
                ));
    }
}
