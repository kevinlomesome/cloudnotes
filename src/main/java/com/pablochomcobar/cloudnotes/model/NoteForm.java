package com.pablochomcobar.cloudnotes.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form object (DTO) used by the Add and Edit pages.
 *
 * We keep it separate from Note so the user can only send title and description,
 * never id or dates. The annotations below are checked automatically by Spring
 * when the controller method parameter is marked with @Valid.
 */
public class NoteForm {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be 100 characters or less")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must be 5000 characters or less")
    private String description;

    public NoteForm() {
    }

    public NoteForm(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /** Builds a form pre-filled from an existing note (used by the Edit page). */
    public static NoteForm fromNote(Note note) {
        return new NoteForm(note.getTitle(), note.getDescription());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
