package com.auca.library;

import com.auca.library.dao.BorrowerDAO;
import com.auca.library.domain.*;
import com.auca.library.domain.enums.BookStatus;
import com.auca.library.domain.enums.Gender;
import com.auca.library.domain.enums.MembershipStatus;
import com.auca.library.domain.enums.RoleType;
import com.auca.library.exception.BorrowLimitExceededException;
import com.auca.library.service.BorrowerService;
import com.auca.library.service.MembershipService;
import com.auca.library.service.UserService;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

public class BorrowerTest {

    private BorrowerService borrowerService;
    private MembershipService membershipService;
    private UserService userService;
    private BorrowerDAO borrowerDAO;

    @Before
    public void setUp() {
        borrowerService = new BorrowerService();
        membershipService = new MembershipService();
        userService = new UserService();
        borrowerDAO = new BorrowerDAO();
    }

    /*
     * Creates a plain user with no location — location is not needed
     * for borrowing tests, so we keep the setup minimal.
     */
    private User createUser() {
        User user = new User();
        user.setPersonId(UUID.randomUUID().toString());
        user.setFirstName("Reader");
        user.setLastName("Test");
        user.setGender(Gender.MALE);
        user.setPhoneNumber("0780000010");
        user.setUserName("reader_" + UUID.randomUUID());
        user.setPassword("testpass");
        user.setRole(RoleType.STUDENT);
        return userService.save(user);
    }

    /*
     * Builds a membership type with the given plan name, daily charge, and book limit.
     * Mirrors the three tiers defined in the requirements: Gold, Silver, Striver.
     */
    private MembershipType createMembershipType(String name, int price, int maxBooks) {
        MembershipType type = new MembershipType();
        type.setMembershipTypeId(UUID.randomUUID());
        type.setMembershipName(name + "_" + UUID.randomUUID().toString().substring(0, 4));
        type.setPrice(price);
        type.setMaxBooks(maxBooks);
        return membershipService.saveMembershipType(type);
    }

    /*
     * Registers and immediately approves a membership for the given user.
     * In real usage a librarian would approve it, but here we skip that step
     * so the reader is ready to borrow within the same test.
     */
    private Membership createApprovedMembership(User user, MembershipType type) {
        Membership membership = membershipService.registerMembership(
                UUID.fromString(user.getPersonId()), type.getMembershipTypeId());
        return membershipService.approveMembership(membership.getMembershipId());
    }

    /*
     * Creates an AVAILABLE book with no shelf assignment.
     * Shelf is optional for these tests since we only care about borrow logic.
     */
    private Book createAvailableBook() {
        Book book = new Book();
        book.setBookId(UUID.randomUUID());
        book.setTitle("Test Book " + UUID.randomUUID().toString().substring(0, 6));
        book.setIsbnCode("ISBN-" + UUID.randomUUID().toString().substring(0, 8));
        book.setPublisherName("Test Publisher");
        book.setEdition(1);
        book.setBookStatus(BookStatus.AVAILABLE);
        return borrowerDAO.saveBook(book);
    }

    // --- Requirement 6: Borrow a book ---

    @Test
    public void borrowBook_availableBook_createsBorrowerRecordWithZeroFine() {
        User user = createUser();
        MembershipType gold = createMembershipType("Gold", 50, 5);
        createApprovedMembership(user, gold);
        Book book = createAvailableBook();

        Borrower borrower = borrowerService.borrowBook(
                UUID.fromString(user.getPersonId()), book.getBookId());

        assertNotNull(borrower.getId());
        // Fine must be zero on the day the book is picked up
        assertEquals(0, borrower.getFine());
        assertNotNull(borrower.getPickupDate());
        assertNotNull(borrower.getDueDate());
    }

    @Test
    public void borrowBook_setsBookStatusToBorrowed() {
        User user = createUser();
        MembershipType gold = createMembershipType("Gold", 50, 5);
        createApprovedMembership(user, gold);
        Book book = createAvailableBook();

        borrowerService.borrowBook(UUID.fromString(user.getPersonId()), book.getBookId());

        // After borrowing, the catalogue must show the book is no longer on the shelf
        Book updated = borrowerDAO.findBookById(book.getBookId());
        assertEquals(BookStatus.BORROWED, updated.getBookStatus());
    }

    @Test
    public void borrowBook_dueDateIsPickupDatePlusLoanPeriod() {
        User user = createUser();
        MembershipType gold = createMembershipType("Gold", 50, 5);
        createApprovedMembership(user, gold);
        Book book = createAvailableBook();

        Borrower borrower = borrowerService.borrowBook(
                UUID.fromString(user.getPersonId()), book.getBookId());

        // Due date should be exactly 14 days after the pickup date
        long diffMs = borrower.getDueDate().getTime() - borrower.getPickupDate().getTime();
        long diffDays = diffMs / (1000 * 60 * 60 * 24);
        assertEquals(14, diffDays);
    }

    // --- Requirement 7: Validate membership borrow limit ---

    @Test
    public void goldMember_withFourActiveBorrows_canBorrowAFifth() {
        User user = createUser();
        MembershipType gold = createMembershipType("Gold", 50, 5);
        createApprovedMembership(user, gold);

        // Borrow 4 books — should all succeed for a Gold member
        for (int i = 0; i < 4; i++) {
            borrowerService.borrowBook(UUID.fromString(user.getPersonId()), createAvailableBook().getBookId());
        }

        // The fifth borrow must also succeed since the limit is 5
        Book fifthBook = createAvailableBook();
        Borrower result = borrowerService.borrowBook(
                UUID.fromString(user.getPersonId()), fifthBook.getBookId());
        assertNotNull(result.getId());
    }

    @Test(expected = BorrowLimitExceededException.class)
    public void goldMember_withFiveActiveBorrows_cannotBorrowASixth() {
        User user = createUser();
        MembershipType gold = createMembershipType("Gold", 50, 5);
        createApprovedMembership(user, gold);

        // Fill up the Gold limit of 5 books
        for (int i = 0; i < 5; i++) {
            borrowerService.borrowBook(UUID.fromString(user.getPersonId()), createAvailableBook().getBookId());
        }

        // The sixth attempt must be rejected
        borrowerService.borrowBook(UUID.fromString(user.getPersonId()), createAvailableBook().getBookId());
    }

    @Test(expected = BorrowLimitExceededException.class)
    public void silverMember_withThreeActiveBorrows_isBlocked() {
        User user = createUser();
        MembershipType silver = createMembershipType("Silver", 30, 3);
        createApprovedMembership(user, silver);

        // Borrow up to the Silver ceiling of 3
        for (int i = 0; i < 3; i++) {
            borrowerService.borrowBook(UUID.fromString(user.getPersonId()), createAvailableBook().getBookId());
        }

        // Any further borrow must be blocked
        borrowerService.borrowBook(UUID.fromString(user.getPersonId()), createAvailableBook().getBookId());
    }

    @Test(expected = BorrowLimitExceededException.class)
    public void striverMember_withTwoActiveBorrows_isBlocked() {
        User user = createUser();
        MembershipType striver = createMembershipType("Striver", 10, 2);
        createApprovedMembership(user, striver);

        // Borrow up to the Striver ceiling of 2
        for (int i = 0; i < 2; i++) {
            borrowerService.borrowBook(UUID.fromString(user.getPersonId()), createAvailableBook().getBookId());
        }

        // Third borrow must be rejected
        borrowerService.borrowBook(UUID.fromString(user.getPersonId()), createAvailableBook().getBookId());
    }

    @Test(expected = BorrowLimitExceededException.class)
    public void userWithoutApprovedMembership_isBlocked() {
        // This user has no membership at all — borrowing must be denied immediately
        User user = createUser();
        Book book = createAvailableBook();
        borrowerService.borrowBook(UUID.fromString(user.getPersonId()), book.getBookId());
    }

    // --- Requirement 12: Late fee calculation ---

    /*
     * Builds a borrower record with manually set pickup, due, and return dates
     * so we can simulate any late-return scenario without waiting real days.
     * The lateChargeFee on the record drives the per-day penalty.
     */
    private Borrower createBorrowerWithDates(User user, Book book,
                                              Date pickupDate, Date dueDate,
                                              Date returnDate, int dailyRate) {
        Borrower borrower = new Borrower();
        borrower.setId(UUID.randomUUID());
        borrower.setReader(user);
        borrower.setBook(book);
        borrower.setPickupDate(pickupDate);
        borrower.setDueDate(dueDate);
        borrower.setReturnDate(returnDate);
        borrower.setFine(0);
        borrower.setLateChargeFee(dailyRate);
        return borrowerDAO.save(borrower);
    }

    // Shifts a date by the given number of days — negative values go into the past
    private Date shiftDays(Date base, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }

    @Test
    public void returnedOnDueDate_feeIsZero() {
        User user = createUser();
        Book book = createAvailableBook();
        Date today = new Date();
        // Pickup was 14 days ago, due today, returned today — no late charge
        Borrower borrower = createBorrowerWithDates(
                user, book, shiftDays(today, -14), today, today, 50);
        assertEquals(0, borrowerService.calculateLateFee(borrower.getId()));
    }

    @Test
    public void goldMember_returnedThreeDaysLate_feeIs150() {
        User user = createUser();
        Book book = createAvailableBook();
        Date today = new Date();
        // Due 3 days ago, returned today — 3 days late at Gold rate of 50 Rwf = 150 Rwf
        Date dueDate = shiftDays(today, -3);
        Borrower borrower = createBorrowerWithDates(
                user, book, shiftDays(today, -17), dueDate, today, 50);
        assertEquals(150, borrowerService.calculateLateFee(borrower.getId()));
    }

    @Test
    public void silverMember_returnedFiveDaysLate_feeIs150() {
        User user = createUser();
        Book book = createAvailableBook();
        Date today = new Date();
        // Due 5 days ago, returned today — 5 days late at Silver rate of 30 Rwf = 150 Rwf
        Date dueDate = shiftDays(today, -5);
        Borrower borrower = createBorrowerWithDates(
                user, book, shiftDays(today, -19), dueDate, today, 30);
        assertEquals(150, borrowerService.calculateLateFee(borrower.getId()));
    }

    @Test
    public void striverMember_returnedOneDayLate_feeIs10() {
        User user = createUser();
        Book book = createAvailableBook();
        Date today = new Date();
        // Due yesterday, returned today — 1 day late at Striver rate of 10 Rwf = 10 Rwf
        Date dueDate = shiftDays(today, -1);
        Borrower borrower = createBorrowerWithDates(
                user, book, shiftDays(today, -15), dueDate, today, 10);
        assertEquals(10, borrowerService.calculateLateFee(borrower.getId()));
    }

    @Test
    public void notYetReturned_feeIsComputedAgainstToday() {
        User user = createUser();
        Book book = createAvailableBook();
        Date today = new Date();
        // Due 4 days ago, book not yet returned — fee is measured against today
        Date dueDate = shiftDays(today, -4);
        Borrower borrower = createBorrowerWithDates(
                user, book, shiftDays(today, -18), dueDate, null, 50);
        // 4 days overdue at 50 Rwf per day = 200 Rwf
        assertEquals(200, borrowerService.calculateLateFee(borrower.getId()));
    }
}
