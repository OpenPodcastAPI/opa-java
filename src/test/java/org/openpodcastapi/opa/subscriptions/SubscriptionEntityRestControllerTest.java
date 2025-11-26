package org.openpodcastapi.opa.subscriptions;

import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpodcastapi.opa.security.TokenService;
import org.openpodcastapi.opa.subscription.SubscriptionDTO;
import org.openpodcastapi.opa.subscription.SubscriptionService;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserRepository;
import org.openpodcastapi.opa.user.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
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
    private JsonMapper jsonMapper;

    @Autowired
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private SubscriptionService subscriptionService;

    private String accessToken;

    private UserEntity mockUser;

    @BeforeEach
    void setup() {
        mockUser = UserEntity
                .builder()
                .id(1L)
                .uuid(UUID.randomUUID())
                .username("user")
                .email("user@test.test")
                .userRoles(Set.of(UserRoles.USER))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findUserByUuid(any(UUID.class))).thenReturn(Optional.of(mockUser));

        accessToken = tokenService.generateAccessToken(mockUser);
    }

    @Test
    void getAllSubscriptionsForAnonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void getAllSubscriptionsForUser_shouldReturnSubscriptions() throws Exception {
        SubscriptionDTO.UserSubscriptionDTO sub1 = new SubscriptionDTO.UserSubscriptionDTO(UUID.randomUUID(), "test.com/feed1", Instant.now(), Instant.now(), null);
        SubscriptionDTO.UserSubscriptionDTO sub2 = new SubscriptionDTO.UserSubscriptionDTO(UUID.randomUUID(), "test.com/feed2", Instant.now(), Instant.now(), null);
        Page<SubscriptionDTO.@NonNull UserSubscriptionDTO> page = new PageImpl<>(List.of(sub1, sub2));

        when(subscriptionService.getAllActiveSubscriptionsForUser(eq(mockUser.getId()), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptions.length()").value(2))
                .andDo(document("subscriptions-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("The access token used to authenticate the user")
                        ),
                        queryParameters(
                                parameterWithName("page").description("The page number to fetch").optional(),
                                parameterWithName("size").description("The number of results to include on each page").optional(),
                                parameterWithName("includeUnsubscribed")
                                        .optional()
                                        .description("If true, includes unsubscribed feeds in the results. Defaults to false.")
                        ),
                        responseFields(
                                fieldWithPath("subscriptions[].uuid").description("The UUID of the subscription").type(JsonFieldType.STRING),
                                fieldWithPath("subscriptions[].feedUrl").description("The feed URL of the subscription").type(JsonFieldType.STRING),
                                fieldWithPath("subscriptions[].createdAt").description("Creation timestamp of the subscription").type(JsonFieldType.STRING),
                                fieldWithPath("subscriptions[].updatedAt").description("Last update timestamp of the subscription").type(JsonFieldType.STRING),
                                fieldWithPath("subscriptions[].unsubscribedAt").description("The date at which the user unsubscribed from the feed").type(JsonFieldType.STRING).optional(),
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
    @WithMockUser(username = "user")
    void getAllSubscriptionsForUser_shouldIncludeUnsubscribedWhenRequested() throws Exception {
        SubscriptionDTO.UserSubscriptionDTO sub1 = new SubscriptionDTO.UserSubscriptionDTO(UUID.randomUUID(), "test.com/feed1", Instant.now(), Instant.now(), null);
        SubscriptionDTO.UserSubscriptionDTO sub2 = new SubscriptionDTO.UserSubscriptionDTO(UUID.randomUUID(), "test.com/feed2", Instant.now(), Instant.now(), Instant.now());
        Page<SubscriptionDTO.@NonNull UserSubscriptionDTO> page = new PageImpl<>(List.of(sub1, sub2));

        when(subscriptionService.getAllSubscriptionsForUser(eq(mockUser.getId()), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("includeUnsubscribed", "true"))
                .andExpect(status().isOk())
                .andDo(document("subscriptions-list-with-unsubscribed",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    @Test
    void getSubscriptionByUuidForAnonymous_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions/{uuid}", UUID.randomUUID())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test")
    void getNonexistentSubscription_shouldReturnNotFound() throws Exception {
        when(subscriptionService.getUserSubscriptionBySubscriptionUuid(any(UUID.class), anyLong()))
                .thenThrow(new EntityNotFoundException());

        mockMvc.perform(get("/api/v1/subscriptions/{uuid}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user")
    void getSubscriptionByUuid_shouldReturnSubscription() throws Exception {
        UUID subscriptionUuid = UUID.randomUUID();

        SubscriptionDTO.UserSubscriptionDTO sub = new SubscriptionDTO.UserSubscriptionDTO(subscriptionUuid, "test.com/feed1", Instant.now(), Instant.now(), null);
        when(subscriptionService.getUserSubscriptionBySubscriptionUuid(subscriptionUuid, mockUser.getId()))
                .thenReturn(sub);

        mockMvc.perform(get("/api/v1/subscriptions/{uuid}", subscriptionUuid)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(document("subscription-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("The access token used to authenticate the user")
                        ),
                        pathParameters(
                                parameterWithName("uuid").description("UUID of the subscription to retrieve")
                        ),
                        responseFields(
                                fieldWithPath("uuid").description("The UUID of the subscription").type(JsonFieldType.STRING),
                                fieldWithPath("feedUrl").description("The feed URL of the subscription").type(JsonFieldType.STRING),
                                fieldWithPath("createdAt").description("Creation timestamp").type(JsonFieldType.STRING),
                                fieldWithPath("updatedAt").description("Last update timestamp").type(JsonFieldType.STRING),
                                fieldWithPath("unsubscribedAt").description("The date at which the user unsubscribed from the feed").type(JsonFieldType.STRING).optional()
                        )
                ));
    }

    @Test
    void createUserSubscriptionWithAnonymousUser_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void createUserSubscriptionsWithoutBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user")
    void createUserSubscriptions_shouldReturnMixedResponse() throws Exception {
        final Instant timestamp = Instant.now();

        final UUID goodFeedUUID = UUID.randomUUID();
        final String BAD_UUID = "62ad30ce-aac0-4f0a-a811";

        SubscriptionDTO.SubscriptionCreateDTO dto1 = new SubscriptionDTO.SubscriptionCreateDTO(goodFeedUUID.toString(), "test.com/feed1");
        SubscriptionDTO.SubscriptionCreateDTO dto2 = new SubscriptionDTO.SubscriptionCreateDTO(BAD_UUID, "test.com/feed2");

        SubscriptionDTO.BulkSubscriptionResponseDTO response = new SubscriptionDTO.BulkSubscriptionResponseDTO(
                List.of(new SubscriptionDTO.UserSubscriptionDTO(goodFeedUUID, "test.com/feed1", timestamp, timestamp, null)),
                List.of(new SubscriptionDTO.SubscriptionFailureDTO(BAD_UUID, "test.com/feed2", "invalid UUID format"))
        );

        when(subscriptionService.addSubscriptions(anyList(), eq(mockUser.getId())))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(List.of(dto1, dto2))))
                .andExpect(status().isMultiStatus())
                .andDo(document("subscriptions-bulk-create-mixed",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("The access token used to authenticate the user")
                        ),
                        requestFields(
                                fieldWithPath("[].uuid").description("The UUID of the subscription"),
                                fieldWithPath("[].feedUrl").description("The feed URL of the subscription to create")
                        ),
                        responseFields(
                                fieldWithPath("success[]").description("List of feed URLs successfully added").type(JsonFieldType.ARRAY),
                                fieldWithPath("success[].uuid").description("The UUID of the feed").type(JsonFieldType.STRING),
                                fieldWithPath("success[].feedUrl").description("The feed URL").type(JsonFieldType.STRING),
                                fieldWithPath("success[].createdAt").description("The timestamp at which the subscription was created").type(JsonFieldType.STRING),
                                fieldWithPath("success[].updatedAt").description("The timestamp at which the subscription was updated").type(JsonFieldType.STRING),
                                fieldWithPath("success[].unsubscribedAt").description("The date at which the user unsubscribed from the feed").type(JsonFieldType.STRING).optional(),
                                fieldWithPath("failure[]").description("List of feed URLs that failed to add").type(JsonFieldType.ARRAY),
                                fieldWithPath("failure[].uuid").description("The UUID of the feed").type(JsonFieldType.STRING),
                                fieldWithPath("failure[].feedUrl").description("The feed URL").type(JsonFieldType.STRING),
                                fieldWithPath("failure[].message").description("The error message").type(JsonFieldType.STRING)
                        )
                ));
    }

    @Test
    @WithMockUser(username = "user")
    void createUserSubscription_shouldReturnSuccess() throws Exception {
        final UUID goodFeedUUID = UUID.randomUUID();
        final Instant timestamp = Instant.now();

        SubscriptionDTO.SubscriptionCreateDTO dto = new SubscriptionDTO.SubscriptionCreateDTO(goodFeedUUID.toString(), "test.com/feed1");

        final var response = new SubscriptionDTO.BulkSubscriptionResponseDTO(
                List.of(new SubscriptionDTO.UserSubscriptionDTO(goodFeedUUID, "test.com/feed1", timestamp, timestamp, null)),
                List.of()
        );

        when(subscriptionService.addSubscriptions(anyList(), eq(mockUser.getId())))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(List.of(dto))))
                .andExpect(status().is2xxSuccessful())
                .andDo(document("subscriptions-bulk-create-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("The access token used to authenticate the user")
                        ),
                        requestFields(
                                fieldWithPath("[].uuid").description("The UUID of the subscription"),
                                fieldWithPath("[].feedUrl").description("The feed URL of the subscription to create")
                        ),
                        responseFields(
                                fieldWithPath("success[]").description("List of feed URLs successfully added").type(JsonFieldType.ARRAY),
                                fieldWithPath("success[].uuid").description("The UUID of the feed").type(JsonFieldType.STRING),
                                fieldWithPath("success[].feedUrl").description("The feed URL").type(JsonFieldType.STRING),
                                fieldWithPath("success[].createdAt").description("The timestamp at which the subscription was created").type(JsonFieldType.STRING),
                                fieldWithPath("success[].updatedAt").description("The timestamp at which the subscription was updated").type(JsonFieldType.STRING),
                                fieldWithPath("success[].unsubscribedAt").description("The date at which the user unsubscribed from the feed").type(JsonFieldType.STRING).optional(),
                                fieldWithPath("failure[]").description("List of feed URLs that failed to add").type(JsonFieldType.ARRAY).ignored())));
    }

    @Test
    @WithMockUser(username = "user")
    void createUserSubscription_shouldReturnFailure() throws Exception {
        final String BAD_UUID = "62ad30ce-aac0-4f0a-a811";

        final var dto = new SubscriptionDTO.SubscriptionCreateDTO(BAD_UUID, "test.com/feed2");

        final var response = new SubscriptionDTO.BulkSubscriptionResponseDTO(
                List.of(),
                List.of(new SubscriptionDTO.SubscriptionFailureDTO(BAD_UUID, "test.com/feed2", "invalid UUID format"))
        );

        when(subscriptionService.addSubscriptions(anyList(), eq(mockUser.getId())))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(List.of(dto))))
                .andExpect(status().isBadRequest())
                .andDo(document("subscriptions-bulk-create-failure",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("The access token used to authenticate the user")
                        ),
                        responseFields(
                                fieldWithPath("success[]").description("List of feed URLs successfully added").type(JsonFieldType.ARRAY).ignored(),
                                fieldWithPath("failure[]").description("List of feed URLs that failed to add").type(JsonFieldType.ARRAY),
                                fieldWithPath("failure[].uuid").description("The UUID of the feed").type(JsonFieldType.STRING),
                                fieldWithPath("failure[].feedUrl").description("The feed URL").type(JsonFieldType.STRING),
                                fieldWithPath("failure[].message").description("The error message").type(JsonFieldType.STRING)
                        )));
    }

    @Test
    void unsubscribingWithAnonymousUser_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions/{uuid}/unsubscribe", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void unsubscribingNonexistentEntity_shouldReturnNotFound() throws Exception {
        when(subscriptionService.unsubscribeUserFromFeed(any(UUID.class), anyLong()))
                .thenThrow(new EntityNotFoundException());

        mockMvc.perform(post("/api/v1/subscriptions/{uuid}/unsubscribe", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user")
    void unsubscribe_shouldReturnUpdatedSubscription() throws Exception {
        final var subscriptionUuid = UUID.randomUUID();
        final var timestamp = Instant.now();

        SubscriptionDTO.UserSubscriptionDTO updatedSubscription = new SubscriptionDTO.UserSubscriptionDTO(
                subscriptionUuid,
                "test.com/feed1",
                timestamp,
                timestamp,
                timestamp
        );

        when(subscriptionService.unsubscribeUserFromFeed(subscriptionUuid, mockUser.getId()))
                .thenReturn(updatedSubscription);

        // Act & Assert
        mockMvc.perform(post("/api/v1/subscriptions/{uuid}/unsubscribe", subscriptionUuid)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(subscriptionUuid.toString()))
                .andExpect(jsonPath("$.feedUrl").value("test.com/feed1"))
                .andExpect(jsonPath("$.unsubscribedAt").value(timestamp.toString()))
                .andDo(document("subscription-unsubscribe",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("The access token used to authenticate the user")
                        ),
                        pathParameters(
                                parameterWithName("uuid").description("UUID of the subscription to update")
                        ),
                        responseFields(
                                fieldWithPath("uuid").description("The UUID of the subscription").type(JsonFieldType.STRING),
                                fieldWithPath("feedUrl").description("The feed URL of the subscription").type(JsonFieldType.STRING),
                                fieldWithPath("createdAt").description("When the subscription was created").type(JsonFieldType.STRING),
                                fieldWithPath("updatedAt").description("When the subscription was last updated").type(JsonFieldType.STRING),
                                fieldWithPath("unsubscribedAt").description("The date at which the user unsubscribed from the feed").type(JsonFieldType.STRING)
                        )
                ));
    }
}
