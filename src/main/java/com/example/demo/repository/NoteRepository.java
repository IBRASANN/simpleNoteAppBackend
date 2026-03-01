package com.example.demo.repository;

import com.example.demo.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserUsername(String username);

    // helper used when deleting to ensure the note belongs to the user
    java.util.Optional<Note> findByIdAndUserUsername(Long id, String username);
}