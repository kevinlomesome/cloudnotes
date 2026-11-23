package com.pablochomcobar.cloudnotes.controller;

import com.pablochomcobar.cloudnotes.exception.DatabaseException;
import com.pablochomcobar.cloudnotes.model.Note;
import com.pablochomcobar.cloudnotes.model.NoteForm;
import com.pablochomcobar.cloudnotes.service.NoteService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Web layer: receives browser requests, checks user session,
 * calls NoteService, and chooses which Thymeleaf page to show.
 */
@Controller
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    private String getLoggedInUserId(HttpSession session) {
        return (String) session.getAttribute(AuthController.SESSION_USER_ID);
    }

    private void addAuthAttributes(Model model, HttpSession session) {
        String userId = getLoggedInUserId(session);
        model.addAttribute("isLoggedIn", userId != null);
        model.addAttribute("userName", session.getAttribute(AuthController.SESSION_USER_NAME));
        model.addAttribute("userEmail", session.getAttribute(AuthController.SESSION_USER_EMAIL));
    }

    // ---------- HOME: list + search ----------

    @GetMapping("/")
    public String home(@RequestParam(name = "search", required = false) String search,
                       HttpSession session,
                       Model model) {
        addAuthAttributes(model, session);
        String userId = getLoggedInUserId(session);

        if (userId != null) {
            List<Note> notes = noteService.searchByTitle(userId, search);
            model.addAttribute("notes", notes);
        } else {
            model.addAttribute("notes", List.of());
        }

        model.addAttribute("search", search == null ? "" : search.trim());
        return "index";
    }

    // ---------- CREATE ----------

    @GetMapping("/notes/new")
    public String showAddForm(HttpSession session, Model model, RedirectAttributes redirect) {
        String userId = getLoggedInUserId(session);
        if (userId == null) {
            redirect.addFlashAttribute("errorMessage", "Please sign in with Google to create notes.");
            return "redirect:/";
        }
        addAuthAttributes(model, session);
        model.addAttribute("noteForm", new NoteForm());
        return "add-note";
    }

    @PostMapping("/notes")
    public String createNote(@Valid @ModelAttribute("noteForm") NoteForm noteForm,
                             BindingResult result,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirect) {
        String userId = getLoggedInUserId(session);
        if (userId == null) {
            redirect.addFlashAttribute("errorMessage", "Please sign in with Google to create notes.");
            return "redirect:/";
        }

        if (result.hasErrors()) {
            addAuthAttributes(model, session);
            return "add-note";
        }
        try {
            noteService.createNote(userId, noteForm);
            redirect.addFlashAttribute("successMessage", "Note created successfully!");
            return "redirect:/";
        } catch (DatabaseException e) {
            addAuthAttributes(model, session);
            model.addAttribute("errorMessage", "Could not save the note. Please try again.");
            return "add-note";
        }
    }

    // ---------- UPDATE ----------

    @GetMapping("/notes/{id}/edit")
    public String showEditForm(@PathVariable String id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirect) {
        String userId = getLoggedInUserId(session);
        if (userId == null) {
            redirect.addFlashAttribute("errorMessage", "Please sign in to edit notes.");
            return "redirect:/";
        }

        Note note = noteService.getNoteById(id);
        // Security check: ensure the note belongs to the logged-in user
        if (note.getUserId() != null && !note.getUserId().equals(userId)) {
            redirect.addFlashAttribute("errorMessage", "You cannot edit another user's note.");
            return "redirect:/";
        }

        addAuthAttributes(model, session);
        model.addAttribute("note", note);
        model.addAttribute("noteForm", NoteForm.fromNote(note));
        return "edit-note";
    }

    @PostMapping("/notes/{id}")
    public String updateNote(@PathVariable String id,
                             @Valid @ModelAttribute("noteForm") NoteForm noteForm,
                             BindingResult result,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirect) {
        String userId = getLoggedInUserId(session);
        if (userId == null) {
            redirect.addFlashAttribute("errorMessage", "Please sign in to update notes.");
            return "redirect:/";
        }

        Note existingNote = noteService.getNoteById(id);
        if (existingNote.getUserId() != null && !existingNote.getUserId().equals(userId)) {
            redirect.addFlashAttribute("errorMessage", "You cannot edit another user's note.");
            return "redirect:/";
        }

        if (result.hasErrors()) {
            addAuthAttributes(model, session);
            model.addAttribute("note", existingNote);
            return "edit-note";
        }
        try {
            noteService.updateNote(id, noteForm);
            redirect.addFlashAttribute("successMessage", "Note updated successfully!");
            return "redirect:/";
        } catch (DatabaseException e) {
            addAuthAttributes(model, session);
            model.addAttribute("note", existingNote);
            model.addAttribute("errorMessage", "Could not update the note. Please try again.");
            return "edit-note";
        }
    }

    // ---------- DELETE ----------

    @PostMapping("/notes/{id}/delete")
    public String deleteNote(@PathVariable String id,
                             HttpSession session,
                             RedirectAttributes redirect) {
        String userId = getLoggedInUserId(session);
        if (userId == null) {
            redirect.addFlashAttribute("errorMessage", "Please sign in to delete notes.");
            return "redirect:/";
        }

        Note note = noteService.getNoteById(id);
        if (note.getUserId() != null && !note.getUserId().equals(userId)) {
            redirect.addFlashAttribute("errorMessage", "You cannot delete another user's note.");
            return "redirect:/";
        }

        try {
            noteService.deleteNote(id);
            redirect.addFlashAttribute("successMessage", "Note deleted successfully!");
        } catch (DatabaseException e) {
            redirect.addFlashAttribute("errorMessage", "Could not delete the note. Please try again.");
        }
        return "redirect:/";
    }
}
