package com.example.demo.integration;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // Rollback after each test
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
    }

    @Test
    void testCreateUserIntegration() throws Exception {
        // Given
        User newUser = new User();
        newUser.setName("Integration Test User");
        newUser.setAddress("Test Address");

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test User"))
                .andExpect(jsonPath("$.address").value("Test Address"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testGetAllUsersIntegration() throws Exception {
        // Given - Create real users in database
        User user1 = new User(null, "Devi", "Udupi");
        User user2 = new User(null, "Raani", "Mangalore");
        userRepository.save(user1);
        userRepository.save(user2);

        // When & Then - Check real data from database
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Devi"))
                .andExpect(jsonPath("$[1].name").value("Raani"));
    }

    @Test
    void testUpdateUserIntegration() throws Exception {
        // Given - Create a real user
        User user = new User(null, "Original Name", "Original Address");
        User savedUser = userRepository.save(user);

        // Update details
        User updatedUser = new User();
        updatedUser.setName("Updated Name");
        updatedUser.setAddress("Updated Address");

        // When & Then
        mockMvc.perform(put("/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.address").value("Updated Address"));
    }

    @Test
    void testDeleteUserIntegration() throws Exception {
        // Given - Create a real user
        User user = new User(null, "To Be Deleted", "Delete Address");
        User savedUser = userRepository.save(user);

        // When & Then
        mockMvc.perform(delete("/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify user is actually deleted from database
        mockMvc.perform(get("/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserByIdIntegration() throws Exception {
        // Given - Create a real user
        User user = new User(null, "Devi", "Udupi");
        User savedUser = userRepository.save(user);

        // When & Then
        mockMvc.perform(get("/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Devi"))
                .andExpect(jsonPath("$.address").value("Udupi"));
    }
}
