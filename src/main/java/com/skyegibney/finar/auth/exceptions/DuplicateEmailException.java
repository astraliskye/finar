package com.skyegibney.finar.auth.exceptions;

public class DuplicateEmailException extends Exception {
    public DuplicateEmailException (String message) {
        super(message);
    }
}
