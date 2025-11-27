package org.openpodcastapi.opa.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpodcastapi.opa.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class UserRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    private UserEntity mockUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        final var mockUserDetails = new UserDTO.CreateUserDTO("user", "testPassword", "test@test.test");
        final var convertedUser = userMapper.toEntity(mockUserDetails);
        convertedUser.setUuid(UUID.randomUUID());
        convertedUser.setPassword(passwordEncoder.encode("testPassword"));
        mockUser = userRepository.save(convertedUser);
    }

    @Test
    void getAllUsers_shouldReturn401_forAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_shouldReturn200_andList() throws Exception {
        mockUser.setUserRoles(Set.of(UserRoles.USER, UserRoles.ADMIN));
        mockUser = userRepository.save(mockUser);

        final var accessToken = tokenService.generateAccessToken(mockUser);

        // Mock a second user
        final var uuid = UUID.randomUUID();
        final var dto = new UserDTO.CreateUserDTO("bob", "testPassword", "bob@test.test");
        final var convertedUser = userMapper.toEntity(dto);
        convertedUser.setUuid(uuid);
        convertedUser.setPassword(passwordEncoder.encode("testPassword"));
        userRepository.save(convertedUser);

        // Perform the test for the admin role
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk()) // Expect 200 for admin role
                .andDo(document("users-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description("The access token used to authenticate the user")
                        ),
                        queryParameters(
                                parameterWithName("page").description("The page number to fetch").optional(),
                                parameterWithName("size").description("The number of results to include on each page").optional()
                        ),
                        responseFields(
                                fieldWithPath("users[].uuid").description("The userEntity's UUID").type(JsonFieldType.STRING),
                                fieldWithPath("users[].username").description("The userEntity's username").type(JsonFieldType.STRING),
                                fieldWithPath("users[].email").description("UserEntity email address").type(JsonFieldType.STRING),
                                fieldWithPath("users[].createdAt").description("The date at which the userEntity was created").type(JsonFieldType.STRING),
                                fieldWithPath("users[].updatedAt").description("The date at which the userEntity was last updated").type(JsonFieldType.STRING),
                                fieldWithPath("page").description("Current page number").type(JsonFieldType.NUMBER),
                                fieldWithPath("size").description("Page size").type(JsonFieldType.NUMBER),
                                fieldWithPath("totalElements").description("Total number of users").type(JsonFieldType.NUMBER),
                                fieldWithPath("totalPages").description("Total number of pages").type(JsonFieldType.NUMBER),
                                fieldWithPath("first").description("Whether this is the first page of results").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("last").description("Whether this is the last page of results").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("numberOfElements").description("The total number of results on the current page").type(JsonFieldType.NUMBER)
                        )
                ));
    }

    @Test
    void getAllUsers_shouldReturn403_forUserRole() throws Exception {
        String accessToken = tokenService.generateAccessToken(mockUser);

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isForbidden())
                .andDo(document("users-list-unsuccessful",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("The page number to fetch").optional(),
                                parameterWithName("size").description("The number of results to include on each page").optional()
                        ),
                        responseFields(
                                fieldWithPath("error").description("Error message").type(JsonFieldType.STRING),
                                fieldWithPath("message").description("The specific error message").type(JsonFieldType.STRING)
                        )
                ));
    }
}

