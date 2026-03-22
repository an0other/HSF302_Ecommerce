package com.hsf.hsf302_ecom.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 404 — page / resource not found ───────────────────────────────────────
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex, HttpServletRequest request, Model model) {
        model.addAttribute("status",  404);
        model.addAttribute("error",   "Page Not Found");
        model.addAttribute("message", "The page you're looking for doesn't exist or has been moved.");
        model.addAttribute("path",    request.getRequestURI());
        return "error/error";
    }

    // ── IllegalArgumentException — bad input ───────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("status",  400);
        model.addAttribute("error",   "Bad Request");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path",    request.getRequestURI());
        return "error/error";
    }

    // ── IllegalStateException — business rule violation ────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(IllegalStateException ex, HttpServletRequest request, Model model) {
        log.warn("Conflict at {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("status",  409);
        model.addAttribute("error",   "Action Not Allowed");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("path",    request.getRequestURI());
        return "error/error";
    }

    // ── RuntimeException — general server error ────────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntime(RuntimeException ex, HttpServletRequest request, Model model) {
        log.error("Unhandled runtime exception at {}", request.getRequestURI(), ex);
        model.addAttribute("status",  500);
        model.addAttribute("error",   "Something Went Wrong");
        model.addAttribute("message", "An unexpected error occurred. Please try again or contact support.");
        model.addAttribute("path",    request.getRequestURI());
        return "error/error";
    }

    // ── Catch-all fallback ─────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        model.addAttribute("status",  500);
        model.addAttribute("error",   "Something Went Wrong");
        model.addAttribute("message", "An unexpected error occurred. Please try again or contact support.");
        model.addAttribute("path",    request.getRequestURI());
        return "error/error";
    }
}