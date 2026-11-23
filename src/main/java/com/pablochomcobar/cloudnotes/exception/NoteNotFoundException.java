package com.pablochomcobar.cloudnotes.exception;

/** Thrown when a note with the given ID does not exist in Firestore. */
public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(String id) {
        super("Note not found with id: " + id);
    }
}
