package com.example.demo.service;

import com.example.demo.model.Note;
import com.example.demo.model.User;
import com.example.demo.repository.NoteRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NoteServiceTest {
    private NoteRepository noteRepository;
    private UserRepository userRepository;
    private NoteService service;

    @BeforeEach
    void setup() {
        noteRepository = Mockito.mock(NoteRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        service = new NoteService(noteRepository, userRepository);
    }

    @Test
    void getNotesForUser_returnsList() {
        when(noteRepository.findByUserUsername("bob")).thenReturn(Arrays.asList(new Note()));
        List<Note> notes = service.getNotesForUser("bob");
        assertThat(notes).hasSize(1);
        verify(noteRepository).findByUserUsername("bob");
    }

    @Test
    void addNoteForUser_existingUser_savesNote() {
        User user = new User();
        user.setUsername("bob");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        when(noteRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Note saved = service.addNoteForUser("bob", "hello");

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getText()).isEqualTo("hello");
        verify(noteRepository).save(any());
    }

    @Test
    void addNoteForUser_unknownUser_throws() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.addNoteForUser("bob", "x"));
    }

    @Test
    void addNoteForUser_emptyText_throws() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.addNoteForUser("bob", "   "));
    }

    @Test
    void addNoteForUser_htmlOnly_throws() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.addNoteForUser("bob", "<p><br></p>"));
    }

    @Test
    void deleteNoteForUser_existingNote_removesNote() {
        User user = new User();
        user.setUsername("bob");
        Note note = new Note();
        note.setId(123L);
        note.setUser(user);
        when(noteRepository.findByIdAndUserUsername(123L, "bob")).thenReturn(Optional.of(note));

        service.deleteNoteForUser("bob", 123L);

        verify(noteRepository).delete(note);
    }

    @Test
    void deleteNoteForUser_wrongUser_throws() {
        when(noteRepository.findByIdAndUserUsername(123L, "bob")).thenReturn(Optional.empty());
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.deleteNoteForUser("bob", 123L));
    }
}