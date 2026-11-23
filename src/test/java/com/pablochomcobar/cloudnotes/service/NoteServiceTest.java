package com.pablochomcobar.cloudnotes.service;

import com.pablochomcobar.cloudnotes.exception.NoteNotFoundException;
import com.pablochomcobar.cloudnotes.model.Note;
import com.pablochomcobar.cloudnotes.model.NoteForm;
import com.pablochomcobar.cloudnotes.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Unit tests for NoteService - Firestore is replaced by a Mockito mock, so no internet needed. */
class NoteServiceTest {

    private NoteRepository repository;
    private NoteService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(NoteRepository.class);
        service = new NoteService(repository);
    }

    private Note note(String id, String title) {
        Note n = new Note(title, "desc");
        n.setId(id);
        n.setCreatedAt(new Date());
        n.setUpdatedAt(new Date());
        return n;
    }

    @Test
    void searchIsCaseInsensitiveAndMatchesPartOfTitle() {
        when(repository.findAllNewestFirst()).thenReturn(List.of(
                note("1", "Shopping List"), note("2", "Gym plan"), note("3", "shopping ideas")));

        List<Note> result = service.searchByTitle("  SHOP ");

        assertThat(result).extracting(Note::getId).containsExactly("1", "3");
    }

    @Test
    void blankSearchReturnsAllNotes() {
        when(repository.findAllNewestFirst()).thenReturn(List.of(note("1", "A"), note("2", "B")));
        assertThat(service.searchByTitle("")).hasSize(2);
    }

    @Test
    void createSetsTimestampsAndTrimsInput() {
        when(repository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note saved = service.createNote(new NoteForm("  Title  ", "  Body  "));

        assertThat(saved.getTitle()).isEqualTo("Title");
        assertThat(saved.getDescription()).isEqualTo("Body");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void updateKeepsCreatedAtAndChangesUpdatedAt() throws InterruptedException {
        Note existing = note("1", "Old");
        Date created = new Date(System.currentTimeMillis() - 60_000);
        existing.setCreatedAt(created);
        existing.setUpdatedAt(created);
        when(repository.findById("1")).thenReturn(Optional.of(existing));
        when(repository.update(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note updated = service.updateNote("1", new NoteForm("New", "New body"));

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getCreatedAt()).isEqualTo(created);
        assertThat(updated.getUpdatedAt()).isAfter(created);
        assertThat(updated.isEdited()).isTrue();
    }

    @Test
    void missingNoteThrowsNotFound() {
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getNoteById("x")).isInstanceOf(NoteNotFoundException.class);
    }
}
