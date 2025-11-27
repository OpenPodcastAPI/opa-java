package org.openpodcastapi.opa.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpodcastapi.opa.user.UserDTO;
import org.openpodcastapi.opa.user.UserEntity;
import org.openpodcastapi.opa.user.UserMapper;
import org.openpodcastapi.opa.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

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

    private final String password = "testPassword";
    @Autowired
    MockMvc mockMvc;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    private UserEntity mockUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        final var mockUserDetails = new UserDTO.CreateUserDTO("user", password, "test@test.test");
        final var convertedUser = userMapper.toEntity(mockUserDetails);
        convertedUser.setUuid(UUID.randomUUID());
        convertedUser.setPassword(passwordEncoder.encode(password));
        mockUser = userRepository.save(convertedUser);
    }

    @Test
    void authenticate_and_get_tokens() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                     { "username": "%s", "password": "%s" }
                                """.formatted(mockUser.getUsername(), password)))
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
                                     { "username": "%s", "password": "%s" }
                                """.formatted(mockUser.getUsername(), password)))
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
                                """.formatted(mockUser.getUsername(), refresh)))
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

