package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

import java.util.Optional;

@WebMvcTest(HelloController.class)
@org.springframework.context.annotation.Import(com.example.demo.config.SecurityConfig.class)
class HelloControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private com.example.demo.service.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.example.demo.repository.UserRepository userRepository;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void publicHello() throws Exception {
        mvc.perform(get("/api/public/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, world!"));
    }

    @Test
    void registration() throws Exception {
        // prepare stubbed behavior for new user
        com.example.demo.model.User saved = new com.example.demo.model.User();
        saved.setUsername("alice");
        saved.setRole("ROLE_USER");
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(com.example.demo.model.User.class)))
                .thenReturn(saved);

        String json = "{\"username\":\"alice\",\"password\":\"secret\"}";
        mvc.perform(post("/api/public/register")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"alice\",\"role\":\"ROLE_USER\"}"));
    }

    @Test
    void registration_existingUser_correctPassword() throws Exception {
        com.example.demo.model.User existing = new com.example.demo.model.User();
        existing.setUsername("bob");
        existing.setPassword("hashed");
        existing.setRole("ROLE_USER");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        String json = "{\"username\":\"bob\",\"password\":\"secret\"}";
        mvc.perform(post("/api/public/register")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":\"bob\",\"password\":\"hashed\",\"role\":\"ROLE_USER\"}"));
    }

    @Test
    void registration_existingUser_wrongPassword() throws Exception {
        com.example.demo.model.User existing = new com.example.demo.model.User();
        existing.setUsername("carol");
        existing.setPassword("hashed");
        existing.setRole("ROLE_USER");
        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("bad", "hashed")).thenReturn(false);

        String json = "{\"username\":\"carol\",\"password\":\"bad\"}";
        mvc.perform(post("/api/public/register")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .content(json))
                .andExpect(status().isUnauthorized());
    }
}
