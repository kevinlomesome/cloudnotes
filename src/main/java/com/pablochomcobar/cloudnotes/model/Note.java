package com.pablochomcobar.cloudnotes.model;

import com.google.cloud.firestore.annotation.Exclude;

import java.util.Date;

/**
 * A single note stored in the Firestore "notes" collection.
 *
 * Firestore converts this class to/from a document automatically, which is why:
 *  - it needs a public no-argument constructor
 *  - it needs public getters and setters for every field
 *
 * java.util.Date is saved in Firestore as a "timestamp" type.
 */
public class Note {

    private String id;           // Firestore document ID
    private String title;
    private String description;
    private Date createdAt;
    private Date updatedAt;

    /** Required by Firestore for converting documents back into Note objects. */
    public Note() {
    }

    public Note(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * True when the note was edited after it was created (used to show "Edited" in the UI).
     * @Exclude tells Firestore NOT to save this as a field in the database.
     */
    @Exclude
    public boolean isEdited() {
        return createdAt != null && updatedAt != null && updatedAt.getTime() - createdAt.getTime() > 1000;
    }
}
