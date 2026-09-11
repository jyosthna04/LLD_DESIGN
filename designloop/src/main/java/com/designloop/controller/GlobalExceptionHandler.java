package com.designloop.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

/**
 * Catches errors from any controller and shows a friendly error page
 * instead of a raw stack trace. This covers things like: the problem
 * or attempt ID doesn't exist, or trying to submit an attempt twice.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFound(NoSuchElementException e, Model model) {
        model.addAttribute("message", "We couldn't find what you were looking for. It may have been removed, or the link may be incorrect.");
        return "error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception e, Model model) {
        model.addAttribute("message", "Something went wrong on our side. Please try again.");
        return "error";
    }
}
