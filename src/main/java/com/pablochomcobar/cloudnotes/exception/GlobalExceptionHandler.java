package com.pablochomcobar.cloudnotes.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Catches exceptions thrown from ANY controller and shows a friendly error page
 * instead of an ugly stack trace. @ControllerAdvice makes it apply globally.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoteNotFoundException ex, Model model) {
        return errorPage(model, 404, "Note not found",
                "The note you are looking for does not exist or was already deleted.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlePageNotFound(NoResourceFoundException ex, Model model) {
        return errorPage(model, 404, "Page not found",
                "The page you requested does not exist.");
    }

    @ExceptionHandler(DatabaseException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleDatabase(DatabaseException ex, Model model) {
        log.error("Database error", ex);
        return errorPage(model, 503, "Database error",
                "We couldn't reach the cloud database. Please try again in a moment.");
    }

    /** Last safety net for anything we did not expect. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        return errorPage(model, 500, "Something went wrong",
                "An unexpected error occurred. Please try again.");
    }

    private String errorPage(Model model, int status, String title, String message) {
        model.addAttribute("status", status);
        model.addAttribute("errorTitle", title);
        model.addAttribute("errorMessage", message);
        return "error";
    }
}
