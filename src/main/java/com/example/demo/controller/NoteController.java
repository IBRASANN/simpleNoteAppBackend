package com.example.demo.controller;

import com.example.demo.model.Note;
import com.example.demo.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NoteController.class);

    @Autowired
    private NoteService noteService;

    @GetMapping
    public List<Note> getNotes(Authentication auth) {
        String username = auth.getName();
        logger.info("User {} requested notes", username);
        return noteService.getNotesForUser(username);
    }

    @PostMapping
    public Note addNote(Authentication auth, @RequestBody Map<String, String> body) {
        String username = auth.getName();
        String text = body.get("text");
        return noteService.addNoteForUser(username, text);
    }

    @DeleteMapping("/{id}")
    public void deleteNote(Authentication auth, @PathVariable Long id) {
        String username = auth.getName();
        logger.info("delete request for note {} by user {}", id, username);
        noteService.deleteNoteForUser(username, id);
    }
}