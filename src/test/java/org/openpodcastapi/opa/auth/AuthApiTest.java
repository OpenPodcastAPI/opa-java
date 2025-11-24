package org.openpodcastapi.opa.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpodcastapi.opa.security.RefreshTokenRepository;
import org.openpodcastapi.opa.security.TokenService;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserRepository;
import org.openpodcastapi.opa.user.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class AuthApiTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    @Qualifier("apiLoginManager")
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private TokenService tokenService;

    @BeforeEach
    void setup() {
        // Mock the userEntity lookup
        UserEntity mockUserEntity = UserEntity.builder()
                .id(2L)
                .uuid(UUID.randomUUID())
                .email("test@test.test")
                .password("password")
                .username("test_user")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .userRoles(Set.of(UserRoles.USER))
                .build();

        // Mock repository behavior for finding user by username
        when(userRepository.findByUsername("test_user")).thenReturn(Optional.of(mockUserEntity));

        // Mock the refresh token validation to return the mock user
        when(tokenService.validateRefreshToken(anyString(), any(UserEntity.class)))
                .thenReturn(mockUserEntity);

        // Mock the access token generation
        when(tokenService.generateAccessToken(any(UserEntity.class))).thenReturn("eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI2MmJjZjczZC0xNGVjLTRkZmMtOGY5ZS1hMDQ0YjE4YjJiYTUiLCJ1c2VybmFtZSI6ImFkbWluIiwiaWF0IjoxNzYzODQzMzEwLCJleHAiOjE3NjM4NDQyMTB9.B9aj5DoVpNe6HTxXm8iTHj5XaqFCcR1ZHRZq6xiqY28YvGGStVkPpedDVZfc02-B");

        // Mock the refresh token generation
        when(tokenService.generateRefreshToken(any(UserEntity.class))).thenReturn("8be54fc2-70ec-48ef-a8ff-4548fd8932b8e947a7ab-99b5-4cfb-b546-ac37eafa6c98");
    }

    @Test
    void authenticate_and_get_tokens() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                     { "username": "test_user", "password": "password" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andDo(document("auth-token",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").description("The user's username"),
                                fieldWithPath("password").description("The user's password")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("A JWT access token"),
                                fieldWithPath("refreshToken").description("A JWT refresh token"),
                                fieldWithPath("expiresIn").description("The number of milliseconds until the access token expires")
                        )
                ));
    }

    @Test
    void refresh_token_flow() throws Exception {
        String json = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                     { "username": "test_user", "password": "password" }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refresh = com.jayway.jsonpath.JsonPath.read(json, "$.refreshToken");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                     {
                                        "username": "%s",
                                        "refreshToken": "%s"
                                     }
                                """.formatted("test_user", refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andDo(document("auth-refresh",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").description("The username of the user requesting the refresh"),
                                fieldWithPath("refreshToken").description("A valid refresh token")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("New JWT access token"),
                                fieldWithPath("expiresIn").description("The number of milliseconds until the access token expires")
                        )
                ));
    }
}

