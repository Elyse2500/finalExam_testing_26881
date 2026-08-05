package com.auca.library.exception;

/*
 * Thrown when a reader tries to borrow a book but has already reached
 * the maximum number allowed by their current membership plan.
 */
public class BorrowLimitExceededException extends RuntimeException {
    public BorrowLimitExceededException(String message) {
        super(message);
    }
}
