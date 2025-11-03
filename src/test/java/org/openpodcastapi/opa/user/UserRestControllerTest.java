package org.openpodcastapi.opa.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.openpodcastapi.opa.user.controller.UserRestController;
import org.openpodcastapi.opa.user.dto.CreateUserDto;
import org.openpodcastapi.opa.user.dto.UserDto;
import org.openpodcastapi.opa.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
class UserRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_shouldReturn201() throws Exception {
        final Instant createdDate = Instant.now();

        final CreateUserDto request = new CreateUserDto(
                "alice",
                "aliceTest",
                "alice@test.com"
        );

        final UserDto response = new UserDto(
                UUID.randomUUID(),
                "alice",
                "alice@test.com",
                createdDate,
                createdDate
        );

        when(userService.createAndPersistUser(ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf().asHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice"))
                .andDo(document("users-create",
                        preprocessRequest(prettyPrint(), modifyHeaders().remove("X-CSRF-TOKEN")),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").description("Desired username").type(JsonFieldType.STRING),
                                fieldWithPath("email").description("User email address").type(JsonFieldType.STRING),
                                fieldWithPath("password").description("Plaintext password for the new account").type(JsonFieldType.STRING)
                        ),
                        responseFields(
                                fieldWithPath("uuid").description("Generated user UUID").type(JsonFieldType.STRING),
                                fieldWithPath("username").description("The user's username").type(JsonFieldType.STRING),
                                fieldWithPath("email").description("The user's email").type(JsonFieldType.STRING),
                                fieldWithPath("createdAt").description("The date at which the user was created").type(JsonFieldType.STRING),
                                fieldWithPath("updatedAt").description("The date at which the user was last updated").type(JsonFieldType.STRING)
                        )
                ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturn200_andList() throws Exception {
        final Instant createdDate = Instant.now();

        final UserDto user1 = new UserDto(
                UUID.randomUUID(),
                "alice",
                "alice@test.com",
                createdDate,
                createdDate
        );

        final UserDto user2 = new UserDto(
                UUID.randomUUID(),
                "bob",
                "bob@test.com",
                createdDate,
                createdDate
        );

        var page = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 2), 2);
        when(userService.getAllUsers(ArgumentMatchers.any())).thenReturn(page);

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andDo(document("users-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("The page number to fetch").optional(),
                                parameterWithName("size").description("The number of results to include on each page").optional()
                        ),
                        responseFields(
                                fieldWithPath("users[].uuid").description("The user's UUID").type(JsonFieldType.STRING),
                                fieldWithPath("users[].username").description("The user's username").type(JsonFieldType.STRING),
                                fieldWithPath("users[].email").description("User email address").type(JsonFieldType.STRING),
                                fieldWithPath("users[].createdAt").description("The date at which the user was created").type(JsonFieldType.STRING),
                                fieldWithPath("users[].updatedAt").description("The date at which the user was last updated").type(JsonFieldType.STRING),
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
    void createInvalidUser_shouldReturn400() throws Exception {
        final CreateUserDto request = new CreateUserDto(
                "alice",
                "aliceTest",
                "alice" // invalid email should throw validation error
        );

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(document("users-create-bad-request",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("username").description("Desired username").type(JsonFieldType.STRING),
                                fieldWithPath("email").description("User email address").type(JsonFieldType.STRING),
                                fieldWithPath("password").description("Plaintext password for the new account").type(JsonFieldType.STRING)
                        ),
                        responseFields(
                                fieldWithPath("timestamp").description("Time of the error").type(JsonFieldType.STRING),
                                fieldWithPath("status").description("HTTP status code").type(JsonFieldType.NUMBER),
                                fieldWithPath("errors[].field").description("Field that caused the validation error").type(JsonFieldType.STRING),
                                fieldWithPath("errors[].message").description("Validation error message").type(JsonFieldType.STRING)
                        )
                ));
    }
}
