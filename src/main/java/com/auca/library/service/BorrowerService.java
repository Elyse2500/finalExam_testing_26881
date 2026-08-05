package com.auca.library.service;

import com.auca.library.dao.BorrowerDAO;
import com.auca.library.dao.MembershipDAO;
import com.auca.library.domain.Book;
import com.auca.library.domain.Borrower;
import com.auca.library.domain.Membership;
import com.auca.library.domain.User;
import com.auca.library.domain.enums.BookStatus;
import com.auca.library.domain.enums.MembershipStatus;
import com.auca.library.exception.BorrowLimitExceededException;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class BorrowerService {

    // Standard loan period given to every borrower regardless of membership tier
    private static final int LOAN_PERIOD_DAYS = 14;

    private final BorrowerDAO borrowerDAO = new BorrowerDAO();
    private final MembershipDAO membershipDAO = new MembershipDAO();

    /*
     * Processes a book borrowing request for the given reader.
     * Steps performed:
     *   1. Confirm the book exists and is currently available on the shelf.
     *   2. Confirm the reader exists in the system.
     *   3. Check the reader has not exceeded their membership borrow limit.
     *   4. Create the borrower record with today as pickup date and
     *      pickup + 14 days as the due date. Fine starts at zero.
     *   5. Mark the book as BORROWED so no one else can take it.
     */
    public Borrower borrowBook(UUID readerId, UUID bookId) {
        Book book = borrowerDAO.findBookById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book not found: " + bookId);
        }
        if (book.getBookStatus() != BookStatus.AVAILABLE) {
            throw new IllegalStateException("Book is not available for borrowing. Current status: " + book.getBookStatus());
        }

        User reader = borrowerDAO.findUserById(readerId);
        if (reader == null) {
            throw new IllegalArgumentException("Reader not found: " + readerId);
        }

        // Enforce membership borrow limit before proceeding
        validateBorrowLimit(readerId);

        Date pickupDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(pickupDate);
        cal.add(Calendar.DAY_OF_YEAR, LOAN_PERIOD_DAYS);
        Date dueDate = cal.getTime();

        Borrower borrower = new Borrower();
        borrower.setId(UUID.randomUUID());
        borrower.setReader(reader);
        borrower.setBook(book);
        borrower.setPickupDate(pickupDate);
        borrower.setDueDate(dueDate);
        borrower.setFine(0);
        // Late charge fee is taken from the membership type price
        Membership membership = membershipDAO.findActiveMembership(readerId);
        borrower.setLateChargeFee(membership != null ? membership.getMembershipType().getPrice() : 0);

        Borrower saved = borrowerDAO.save(borrower);

        // Flip the book status so the catalogue reflects it is no longer on the shelf
        borrowerDAO.updateBookStatus(bookId, BookStatus.BORROWED);

        return saved;
    }

    /*
     * Validates that the reader has not reached the borrow ceiling set by their plan.
     * Rules:
     *   - Gold:    up to 5 books
     *   - Silver:  up to 3 books
     *   - Striver: up to 2 books
     * A reader with no APPROVED membership is blocked entirely.
     */
    public void validateBorrowLimit(UUID readerId) {
        Membership membership = membershipDAO.findActiveMembership(readerId);

        if (membership == null || membership.getMembershipStatus() != MembershipStatus.APPROVED) {
            throw new BorrowLimitExceededException(
                    "Reader does not have an approved membership and cannot borrow books.");
        }

        int maxBooks = membership.getMembershipType().getMaxBooks();
        long activeBorrows = borrowerDAO.countActiveByReader(readerId);

        if (activeBorrows >= maxBooks) {
            throw new BorrowLimitExceededException(
                    "Borrow limit reached. Your " + membership.getMembershipType().getMembershipName() +
                    " plan allows a maximum of " + maxBooks + " books at a time.");
        }
    }
}
