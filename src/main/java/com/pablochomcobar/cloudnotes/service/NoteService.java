package com.pablochomcobar.cloudnotes.service;

import com.pablochomcobar.cloudnotes.exception.NoteNotFoundException;
import com.pablochomcobar.cloudnotes.model.Note;
import com.pablochomcobar.cloudnotes.model.NoteForm;
import com.pablochomcobar.cloudnotes.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Business-logic layer. Controllers call this service; this service calls the repository.
 * Keeping logic here (not in controllers) makes the code cleaner and easier to test.
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;

    /** Constructor injection - Spring automatically provides the NoteRepository. */
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /** All notes for a user, newest first. */
    public List<Note> getAllNotes(String userId) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        return noteRepository.findAllByUserIdNewestFirst(userId);
    }

    /** All notes, newest first (fallback). */
    public List<Note> getAllNotes() {
        return noteRepository.findAllNewestFirst();
    }

    /**
     * Searches a specific user's notes by title (case-insensitive, "contains" match).
     */
    public List<Note> searchByTitle(String userId, String keyword) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }
        List<Note> userNotes = getAllNotes(userId);
        if (keyword == null || keyword.isBlank()) {
            return userNotes;
        }
        String search = keyword.trim().toLowerCase(Locale.ROOT);
        return userNotes.stream()
                .filter(note -> note.getTitle() != null
                        && note.getTitle().toLowerCase(Locale.ROOT).contains(search))
                .toList();
    }

    /** Searches notes by title without user scoping. */
    public List<Note> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllNotes();
        }
        String search = keyword.trim().toLowerCase(Locale.ROOT);
        return getAllNotes().stream()
                .filter(note -> note.getTitle() != null
                        && note.getTitle().toLowerCase(Locale.ROOT).contains(search))
                .toList();
    }

    /** Returns a note or throws NoteNotFoundException (shown as a 404 page). */
    public Note getNoteById(String id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    /** Creates a note for a specific user and sets both timestamps to "now". */
    public Note createNote(String userId, NoteForm form) {
        Note note = new Note(form.getTitle().trim(), form.getDescription().trim());
        note.setUserId(userId);
        Date now = new Date();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        return noteRepository.save(note);
    }

    /** Creates a note and sets both timestamps to "now". */
    public Note createNote(NoteForm form) {
        Note note = new Note(form.getTitle().trim(), form.getDescription().trim());
        Date now = new Date();
        note.setCreatedAt(now);
        note.setUpdatedAt(now);
        return noteRepository.save(note);
    }

    /** Updates title/description and refreshes updatedAt. createdAt stays the same. */
    public Note updateNote(String id, NoteForm form) {
        Note existing = getNoteById(id);
        existing.setTitle(form.getTitle().trim());
        existing.setDescription(form.getDescription().trim());
        existing.setUpdatedAt(new Date());
        return noteRepository.update(existing);
    }

    /** Deletes a note. We check it exists first so the user gets a clear message. */
    public void deleteNote(String id) {
        getNoteById(id);
        noteRepository.deleteById(id);
    }
}
