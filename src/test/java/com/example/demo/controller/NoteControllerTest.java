package com.example.demo.controller;

import com.example.demo.model.Note;
import com.example.demo.service.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@org.springframework.context.annotation.Import(com.example.demo.config.SecurityConfig.class)
class NoteControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private com.example.demo.service.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private NoteService noteService;

    @Test
    @WithMockUser(username = "bob")
    void getNotes_returnsList() throws Exception {
        Note note = new Note();
        note.setId(1L);
        note.setText("hi");
        when(noteService.getNotesForUser("bob")).thenReturn(Arrays.asList(note));

        mvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"text\":\"hi\"}]"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].id").exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].created").exists());
    }

    @Test
    @WithMockUser(username = "bob")
    void postNote_createsNote() throws Exception {
        Note note = new Note();
        note.setText("hello");
        when(noteService.addNoteForUser("bob", "hello")).thenReturn(note);

        mvc.perform(post("/api/notes")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .content("{\"text\":\"hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"text\":\"hello\"}"));
    }

    @Test
    @WithMockUser(username = "bob")
    void postNote_emptyText_returnsBadRequest() throws Exception {
        when(noteService.addNoteForUser("bob", "   ")).thenThrow(
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST));

        mvc.perform(post("/api/notes")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .content("{\"text\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "bob")
    void deleteNote_removesNote() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/notes/123")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk());
        org.mockito.Mockito.verify(noteService).deleteNoteForUser("bob", 123L);
    }

    @Test
    void getNotes_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized())
                // our filter clears the header value; it may appear as empty string
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string(org.springframework.http.HttpHeaders.WWW_AUTHENTICATE, ""));
    }
}