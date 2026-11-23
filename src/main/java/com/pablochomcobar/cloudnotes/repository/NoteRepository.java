package com.pablochomcobar.cloudnotes.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.pablochomcobar.cloudnotes.exception.DatabaseException;
import com.pablochomcobar.cloudnotes.model.Note;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Data-access layer: the ONLY class that talks directly to Firestore.
 *
 * Firestore calls are asynchronous and return an ApiFuture. We call
 * future.get(...) to wait for the result, and convert any failure into
 * our own DatabaseException.
 */
@Repository
public class NoteRepository {

    private static final String COLLECTION = "notes";
    private static final long TIMEOUT_SECONDS = 15;

    private final Firestore firestore;

    /** Constructor injection: Spring passes in the Firestore bean from FirebaseConfig. */
    public NoteRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference notes() {
        return firestore.collection(COLLECTION);
    }

    /** Returns all notes for a specific user, newest first. */
    public List<Note> findAllByUserIdNewestFirst(String userId) {
        ApiFuture<com.google.cloud.firestore.QuerySnapshot> future =
                notes().whereEqualTo("userId", userId)
                       .get();

        List<Note> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : await(future, "load notes").getDocuments()) {
            result.add(toNote(doc));
        }
        // Sort newest first in memory to avoid requiring a composite index in Firestore
        result.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return result;
    }

    /** Returns all notes, newest first (legacy/fallback). */
    public List<Note> findAllNewestFirst() {
        ApiFuture<com.google.cloud.firestore.QuerySnapshot> future =
                notes().orderBy("createdAt", Query.Direction.DESCENDING).get();

        List<Note> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : await(future, "load notes").getDocuments()) {
            result.add(toNote(doc));
        }
        return result;
    }

    /** Finds one note by its document ID. */
    public Optional<Note> findById(String id) {
        DocumentSnapshot doc = await(notes().document(id).get(), "load note");
        return doc.exists() ? Optional.of(toNote(doc)) : Optional.empty();
    }

    /**
     * Creates a new note. Firestore generates a random ID which we
     * store inside the note too, so it's easy to use later.
     */
    public Note save(Note note) {
        DocumentReference ref = notes().document();   // auto-generated ID
        note.setId(ref.getId());
        await(ref.set(note), "create note");
        return note;
    }

    /** Overwrites an existing note document with new values. */
    public Note update(Note note) {
        await(notes().document(note.getId()).set(note), "update note");
        return note;
    }

    public void deleteById(String id) {
        await(notes().document(id).delete(), "delete note");
    }

    /** Converts a Firestore document into a Note and makes sure the ID is set. */
    private Note toNote(DocumentSnapshot doc) {
        Note note = doc.toObject(Note.class);
        if (note == null) {
            note = new Note();
        }
        note.setId(doc.getId());
        return note;
    }

    /** Waits for a Firestore operation to finish and turns errors into DatabaseException. */
    private <T> T await(ApiFuture<T> future, String action) {
        try {
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();   // good practice: keep the interrupt flag
            throw new DatabaseException("Interrupted while trying to " + action, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new DatabaseException("Failed to " + action, e);
        }
    }
}
