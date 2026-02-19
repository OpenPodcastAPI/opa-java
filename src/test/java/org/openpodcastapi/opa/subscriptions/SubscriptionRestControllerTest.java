package org.openpodcastapi.opa.subscriptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpodcastapi.opa.feed.FeedDTO;
import org.openpodcastapi.opa.feed.FeedRepository;
import org.openpodcastapi.opa.security.TokenService;
import org.openpodcastapi.opa.subscription.SubscriptionService;
import org.openpodcastapi.opa.user.UserDTO;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserMapper;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

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
class SubscriptionRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private UserEntity mockUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        feedRepository.deleteAll();
        final var mockUserDetails = new UserDTO.CreateUserDTO("user", "testPassword", "test@test.test");
        final var convertedUser = userMapper.toEntity(mockUserDetails);
        convertedUser.setUuid(UUID.randomUUID());
        convertedUser.setPassword(passwordEncoder.encode("testPassword"));
        mockUser = userRepository.save(convertedUser);
    }

    @Test
    void getAllSubscriptionsForAnonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/subscriptions")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllSubscriptionsForUser_shouldReturnSubscriptions() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        final var uuid1 = UUID.randomUUID();
        final var uuid2 = UUID.randomUUID();

        final var sub1DTO = new FeedDTO.NewFeedRequestDTO(uuid1.toString(), "test.com/feed1");
        final var sub2DTO = new FeedDTO.NewFeedRequestDTO(uuid2.toString(), "test.com/feed2");

        subscriptionService.addSubscriptions(List.of(sub1DTO, sub2DTO), mockUser.getId());

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
                                        .description("If true, includes unsubscribed feed in the results. Defaults to false.")
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
    void getAllSubscriptionsForUser_shouldIncludeUnsubscribedWhenRequested() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        final var uuid1 = UUID.randomUUID();
        final var uuid2 = UUID.randomUUID();

        final var sub1DTO = new FeedDTO.NewFeedRequestDTO(uuid1.toString(), "test.com/feed1");
        final var sub2DTO = new FeedDTO.NewFeedRequestDTO(uuid2.toString(), "test.com/feed2");

        subscriptionService.addSubscriptions(List.of(sub1DTO, sub2DTO), mockUser.getId());

        subscriptionService.unsubscribeUserFromFeed(uuid2, mockUser.getId());

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
    void getNonexistentSubscription_shouldReturnNotFound() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        mockMvc.perform(get("/api/v1/subscriptions/{uuid}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSubscriptionByUuid_shouldReturnSubscription() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        final var uuid1 = UUID.randomUUID();

        final var sub1DTO = new FeedDTO.NewFeedRequestDTO(uuid1.toString(), "test.com/feed1");

        subscriptionService.addSubscriptions(List.of(sub1DTO), mockUser.getId());

        mockMvc.perform(get("/api/v1/subscriptions/{uuid}", uuid1)
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
    void createUserSubscriptionsWithoutBody_shouldReturnBadRequest() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserSubscriptions_shouldReturnMixedResponse() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        final var uuid1 = UUID.randomUUID();
        final var BAD_UUID = "62ad30ce-aac0-4f0a-a811";

        final var sub1DTO = new FeedDTO.NewFeedRequestDTO(uuid1.toString(), "test.com/feed1");
        final var sub2DTO = new FeedDTO.NewFeedRequestDTO(BAD_UUID, "test.com/feed1");

        mockMvc.perform(post("/api/v1/subscriptions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(List.of(sub1DTO, sub2DTO))))
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
    void createUserSubscription_shouldReturnSuccess() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        final var dto = new FeedDTO.NewFeedRequestDTO(UUID.randomUUID().toString(), "test.com/feed1");

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
    void createUserSubscription_shouldReturnFailure() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        final var dto = new FeedDTO.NewFeedRequestDTO("62ad30ce-aac0-4f0a-a811", "test.com/feed2");

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
    void unsubscribingNonexistentEntity_shouldReturnNotFound() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        mockMvc.perform(post("/api/v1/subscriptions/{uuid}/unsubscribe", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void unsubscribe_shouldReturnUpdatedSubscription() throws Exception {
        final var accessToken = tokenService.generateAccessToken(mockUser);

        final var subscriptionUuid = UUID.randomUUID();
        final var sub1DTO = new FeedDTO.NewFeedRequestDTO(subscriptionUuid.toString(), "test.com/feed1");

        subscriptionService.addSubscriptions(List.of(sub1DTO), mockUser.getId());

        // Act & Assert
        mockMvc.perform(post("/api/v1/subscriptions/{uuid}/unsubscribe", subscriptionUuid)
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(subscriptionUuid.toString()))
                .andExpect(jsonPath("$.feedUrl").value("test.com/feed1"))
                .andExpect(jsonPath("$.unsubscribedAt").exists())
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
