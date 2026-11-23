package com.pablochomcobar.cloudnotes.controller;

import com.pablochomcobar.cloudnotes.exception.DatabaseException;
import com.pablochomcobar.cloudnotes.model.Note;
import com.pablochomcobar.cloudnotes.model.NoteForm;
import com.pablochomcobar.cloudnotes.service.NoteService;
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
 * Web layer: receives browser requests, calls NoteService, and chooses which
 * Thymeleaf page to show. No database code lives here.
 *
 * Pattern used: Post/Redirect/Get - after a form POST we redirect, so refreshing
 * the page won't submit the form twice. Flash attributes carry the success/error
 * message across that redirect.
 */
@Controller
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // ---------- HOME: list + search ----------

    @GetMapping("/")
    public String home(@RequestParam(name = "search", required = false) String search, Model model) {
        List<Note> notes = noteService.searchByTitle(search);
        model.addAttribute("notes", notes);
        model.addAttribute("search", search == null ? "" : search.trim());
        return "index";
    }

    // ---------- CREATE ----------

    @GetMapping("/notes/new")
    public String showAddForm(Model model) {
        model.addAttribute("noteForm", new NoteForm());
        return "add-note";
    }

    /**
     * @Valid runs the validation rules in NoteForm.
     * BindingResult holds any errors; if there are errors we show the form again.
     */
    @PostMapping("/notes")
    public String createNote(@Valid @ModelAttribute("noteForm") NoteForm noteForm,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "add-note";
        }
        try {
            noteService.createNote(noteForm);
            redirect.addFlashAttribute("successMessage", "Note created successfully!");
            return "redirect:/";
        } catch (DatabaseException e) {
            model.addAttribute("errorMessage", "Could not save the note. Please try again.");
            return "add-note";
        }
    }

    // ---------- UPDATE ----------

    @GetMapping("/notes/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model) {
        Note note = noteService.getNoteById(id);   // throws 404 if missing
        model.addAttribute("note", note);
        model.addAttribute("noteForm", NoteForm.fromNote(note));
        return "edit-note";
    }

    @PostMapping("/notes/{id}")
    public String updateNote(@PathVariable String id,
                             @Valid @ModelAttribute("noteForm") NoteForm noteForm,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("note", noteService.getNoteById(id));
            return "edit-note";
        }
        try {
            noteService.updateNote(id, noteForm);
            redirect.addFlashAttribute("successMessage", "Note updated successfully!");
            return "redirect:/";
        } catch (DatabaseException e) {
            model.addAttribute("note", noteService.getNoteById(id));
            model.addAttribute("errorMessage", "Could not update the note. Please try again.");
            return "edit-note";
        }
    }

    // ---------- DELETE ----------

    @PostMapping("/notes/{id}/delete")
    public String deleteNote(@PathVariable String id, RedirectAttributes redirect) {
        try {
            noteService.deleteNote(id);
            redirect.addFlashAttribute("successMessage", "Note deleted successfully!");
        } catch (DatabaseException e) {
            redirect.addFlashAttribute("errorMessage", "Could not delete the note. Please try again.");
        }
        return "redirect:/";
    }
}
