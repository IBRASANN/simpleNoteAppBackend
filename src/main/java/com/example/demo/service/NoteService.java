package com.example.demo.service;

import com.example.demo.model.Note;
import com.example.demo.model.User;
import com.example.demo.repository.NoteRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    // getters used only for tests
    public NoteRepository getNoteRepository() {
        return noteRepository;
    }
    public UserRepository getUserRepository() {
        return userRepository;
    }

    public List<Note> getNotesForUser(String username) {
        return noteRepository.findByUserUsername(username);
    }

    @Transactional
    public Note addNoteForUser(String username, String text) {
        if (text == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "note text cannot be empty");
        }
        // strip HTML tags (quill produces <p><br></p> when empty)
        String plain = text.replaceAll("<[^>]*>", "").trim();
        if (plain.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "note text cannot be empty");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Note note = new Note();
        note.setUser(user);
        note.setText(text);
        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNoteForUser(String username, Long noteId) {
        // verify note exists and belongs to the user
        Note note = noteRepository.findByIdAndUserUsername(noteId, username)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "note not found"));
        noteRepository.delete(note);
    }
}