package org.openpodcastapi.opa.user;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
@Log4j2
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    void getAllUsers_shouldReturn200_andList() throws Exception {
        final Instant createdDate = Instant.now();

        final UserDTO.UserResponseDTO user1 = new UserDTO.UserResponseDTO(
                UUID.randomUUID(),
                "alice",
                "alice@test.com",
                createdDate,
                createdDate
        );

        final UserDTO.UserResponseDTO user2 = new UserDTO.UserResponseDTO(
                UUID.randomUUID(),
                "bob",
                "bob@test.com",
                createdDate,
                createdDate
        );

        // Mock the service call to return users
        PageImpl<UserDTO.UserResponseDTO> page = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 2), 2);
        when(userService.getAllUsers(any())).thenReturn(page);

        // Perform the test for the admin role
        mockMvc.perform(get("/api/v1/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk()) // Expect 200 for admin role
                .andDo(document("users-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
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
    @WithMockUser(roles = "USER")
        // Mock the userEntity with a "USER" role
    void getAllUsers_shouldReturn403_forUserRole() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isForbidden()) // Expect 403 for the userEntity role
                .andDo(document("users-list",
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

