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

    private static final int LOAN_PERIOD_DAYS = 14;

    private final BorrowerDAO borrowerDAO = new BorrowerDAO();
    private final MembershipDAO membershipDAO = new MembershipDAO();

    public Borrower borrowBook(UUID readerId, UUID bookId) {
        Book book = borrowerDAO.findBookById(bookId);
        if (book == null) {
            throw new IllegalArgumentException("book not found: " + bookId);
        }
        if (book.getBookStatus() != BookStatus.AVAILABLE) {
            throw new IllegalStateException("book is not available, status: " + book.getBookStatus());
        }

        User reader = borrowerDAO.findUserById(readerId);
        if (reader == null) {
            throw new IllegalArgumentException("reader not found: " + readerId);
        }

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
        Membership membership = membershipDAO.findActiveMembership(readerId);
        borrower.setLateChargeFee(membership != null ? membership.getMembershipType().getPrice() : 0);

        Borrower saved = borrowerDAO.save(borrower);
        borrowerDAO.updateBookStatus(bookId, BookStatus.BORROWED);
        return saved;
    }

    public void validateBorrowLimit(UUID readerId) {
        Membership membership = membershipDAO.findActiveMembership(readerId);

        if (membership == null || membership.getMembershipStatus() != MembershipStatus.APPROVED) {
            throw new BorrowLimitExceededException("reader has no approved membership");
        }

        int maxBooks = membership.getMembershipType().getMaxBooks();
        long activeBorrows = borrowerDAO.countActiveByReader(readerId);

        if (activeBorrows >= maxBooks) {
            throw new BorrowLimitExceededException("limit reached, max allowed is " + maxBooks + " books");
        }
    }

    public int calculateLateFee(UUID borrowerId) {
        Borrower borrower = borrowerDAO.findById(borrowerId);
        if (borrower == null) {
            throw new IllegalArgumentException("borrower record not found: " + borrowerId);
        }

        Date comparisonDate = borrower.getReturnDate() != null ? borrower.getReturnDate() : new Date();
        Date dueDate = borrower.getDueDate();

        if (!comparisonDate.after(dueDate)) {
            return 0;
        }

        long diffMs = comparisonDate.getTime() - dueDate.getTime();
        long daysLate = diffMs / (1000L * 60 * 60 * 24);
        int fee = (int) daysLate * borrower.getLateChargeFee();

        borrowerDAO.updateFine(borrowerId, fee);
        return fee;
    }
}
