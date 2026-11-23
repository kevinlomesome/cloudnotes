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

    /** All notes, newest first. */
    public List<Note> getAllNotes() {
        return noteRepository.findAllNewestFirst();
    }

    /**
     * Searches notes by title (case-insensitive, "contains" match).
     *
     * Firestore can't do "contains" text searches, so we load the notes (already
     * sorted newest first) and filter them in Java. Perfect for a personal notes app.
     */
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
