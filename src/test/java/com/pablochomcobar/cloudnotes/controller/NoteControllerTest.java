package com.pablochomcobar.cloudnotes.controller;

import com.pablochomcobar.cloudnotes.exception.NoteNotFoundException;
import com.pablochomcobar.cloudnotes.model.Note;
import com.pablochomcobar.cloudnotes.model.NoteForm;
import com.pablochomcobar.cloudnotes.service.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests: renders Thymeleaf templates with mocked NoteService.
 */
@WebMvcTest(NoteController.class)
class NoteControllerTest {

    private static final String TEST_USER_ID = "test-user-123";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NoteService noteService;

    private MockHttpSession authenticatedSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER_ID, TEST_USER_ID);
        session.setAttribute(AuthController.SESSION_USER_NAME, "Test User");
        session.setAttribute(AuthController.SESSION_USER_EMAIL, "test@example.com");
        return session;
    }

    private Note sample() {
        Note n = new Note("Shopping list", "Milk, eggs, bread");
        n.setId("abc123");
        n.setUserId(TEST_USER_ID);
        n.setCreatedAt(new Date());
        n.setUpdatedAt(new Date());
        return n;
    }

    @Test
    void signedOutShowsLandingPage() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Keep Your Notes Private")))
                .andExpect(content().string(containsString("Sign in with Google")));
    }

    @Test
    void homePageShowsNotesWhenLoggedIn() throws Exception {
        when(noteService.searchByTitle(eq(TEST_USER_ID), any())).thenReturn(List.of(sample()));
        mvc.perform(get("/").session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Shopping list")))
                .andExpect(content().string(containsString("/notes/abc123/edit")))
                .andExpect(content().string(containsString("/notes/abc123/delete")));
    }

    @Test
    void homePageEmptySearchStateWhenLoggedIn() throws Exception {
        when(noteService.searchByTitle(eq(TEST_USER_ID), eq("zzz"))).thenReturn(List.of());
        mvc.perform(get("/").param("search", "zzz").session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No matching notes")));
    }

    @Test
    void addPageRequiresLogin() throws Exception {
        mvc.perform(get("/notes/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void addPageRendersWhenLoggedIn() throws Exception {
        mvc.perform(get("/notes/new").session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add a new note")));
    }

    @Test
    void createWithBlankFieldsShowsValidationErrors() throws Exception {
        mvc.perform(post("/notes").param("title", " ").param("description", "").session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("add-note"))
                .andExpect(content().string(containsString("Title is required")));
        verify(noteService, never()).createNote(any(), any());
    }

    @Test
    void createValidNoteRedirectsWithMessage() throws Exception {
        mvc.perform(post("/notes").param("title", "T").param("description", "D").session(authenticatedSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("successMessage", "Note created successfully!"));
    }

    @Test
    void editPageIsPrefilled() throws Exception {
        when(noteService.getNoteById("abc123")).thenReturn(sample());
        mvc.perform(get("/notes/abc123/edit").session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Shopping list\"")))
                .andExpect(content().string(containsString("Milk, eggs, bread")));
    }

    @Test
    void updateRedirects() throws Exception {
        when(noteService.getNoteById("abc123")).thenReturn(sample());
        when(noteService.updateNote(any(), any(NoteForm.class))).thenReturn(sample());
        mvc.perform(post("/notes/abc123").param("title", "New").param("description", "Body").session(authenticatedSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Note updated successfully!"));
    }

    @Test
    void deleteRedirects() throws Exception {
        when(noteService.getNoteById("abc123")).thenReturn(sample());
        mvc.perform(post("/notes/abc123/delete").session(authenticatedSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Note deleted successfully!"));
        verify(noteService).deleteNote("abc123");
    }

    @Test
    void missingNoteShows404Page() throws Exception {
        when(noteService.getNoteById("nope")).thenThrow(new NoteNotFoundException("nope"));
        mvc.perform(get("/notes/nope/edit").session(authenticatedSession()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Note not found")));
    }
}
