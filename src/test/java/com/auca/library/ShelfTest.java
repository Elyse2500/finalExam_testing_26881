package com.auca.library;

import com.auca.library.dao.BorrowerDAO;
import com.auca.library.dao.ShelfDAO;
import com.auca.library.domain.Book;
import com.auca.library.domain.Room;
import com.auca.library.domain.Shelf;
import com.auca.library.domain.enums.BookStatus;
import com.auca.library.service.ShelfService;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class ShelfTest {

    private ShelfService shelfService;
    private ShelfDAO shelfDAO;
    private BorrowerDAO borrowerDAO;

    @Before
    public void setUp() {
        shelfService = new ShelfService();
        shelfDAO = new ShelfDAO();
        borrowerDAO = new BorrowerDAO();
    }

    /*
     * Builds a shelf with zero stock. Stock values are updated
     * automatically when books are assigned, so we start from scratch.
     */
    private Shelf createShelf() {
        Shelf shelf = new Shelf();
        shelf.setShelfId(UUID.randomUUID());
        shelf.setBookCategory("Science");
        shelf.setInitialStock(0);
        shelf.setAvailableStock(0);
        shelf.setBorrowedNumber(0);
        return shelfService.saveShelf(shelf);
    }

    // Creates a room that shelves can later be placed into
    private Room createRoom() {
        Room room = new Room();
        room.setRoomId(UUID.randomUUID());
        room.setRoomCode("ROOM-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        return shelfService.saveRoom(room);
    }

    /*
     * Creates a standalone AVAILABLE book with no shelf yet.
     * The shelf assignment is what we are testing, so the book starts unassigned.
     */
    private Book createBook() {
        Book book = new Book();
        book.setBookId(UUID.randomUUID());
        book.setTitle("Library Book " + UUID.randomUUID().toString().substring(0, 6));
        book.setIsbnCode("ISBN-" + UUID.randomUUID().toString().substring(0, 8));
        book.setPublisherName("AUCA Press");
        book.setEdition(1);
        book.setBookStatus(BookStatus.AVAILABLE);
        return borrowerDAO.saveBook(book);
    }

    // --- Requirement 8: Assign a book to a shelf ---

    @Test
    public void assignBookToShelf_updatesBookShelfId() {
        Book book = createBook();
        Shelf shelf = createShelf();

        shelfService.assignBookToShelf(book.getBookId(), shelf.getShelfId());

        // After assignment the book record must point to the correct shelf
        Book updated = shelfDAO.findBookById(book.getBookId());
        assertNotNull(updated.getShelf());
        assertEquals(shelf.getShelfId(), updated.getShelf().getShelfId());
    }

    @Test
    public void assignBookToShelf_incrementsShelfAvailableStock() {
        Book book = createBook();
        Shelf shelf = createShelf();
        int stockBefore = shelf.getAvailableStock();

        shelfService.assignBookToShelf(book.getBookId(), shelf.getShelfId());

        // Placing a book on the shelf must increase the available count by exactly one
        Shelf updated = shelfDAO.findShelfById(shelf.getShelfId());
        assertEquals(stockBefore + 1, updated.getAvailableStock());
    }

    // --- Requirement 9: Assign a shelf to a room ---

    @Test
    public void assignShelfToRoom_updatesShelfRoomId() {
        Shelf shelf = createShelf();
        Room room = createRoom();

        shelfService.assignShelfToRoom(shelf.getShelfId(), room.getRoomId());

        // The shelf record must now reference the room it was placed in
        Shelf updated = shelfDAO.findShelfById(shelf.getShelfId());
        assertNotNull(updated.getRoom());
        assertEquals(room.getRoomId(), updated.getRoom().getRoomId());
    }

    // --- Requirement 10: Count books in a room ---

    @Test
    public void roomWithMultipleShelves_sumsBookCountsAcrossShelves() {
        Room room = createRoom();

        // First shelf gets 2 books placed on it
        Shelf shelf1 = createShelf();
        shelfService.assignShelfToRoom(shelf1.getShelfId(), room.getRoomId());
        Book b1 = createBook();
        Book b2 = createBook();
        shelfService.assignBookToShelf(b1.getBookId(), shelf1.getShelfId());
        shelfService.assignBookToShelf(b2.getBookId(), shelf1.getShelfId());

        // Second shelf gets 3 books placed on it
        Shelf shelf2 = createShelf();
        shelfService.assignShelfToRoom(shelf2.getShelfId(), room.getRoomId());
        Book b3 = createBook();
        Book b4 = createBook();
        Book b5 = createBook();
        shelfService.assignBookToShelf(b3.getBookId(), shelf2.getShelfId());
        shelfService.assignBookToShelf(b4.getBookId(), shelf2.getShelfId());
        shelfService.assignBookToShelf(b5.getBookId(), shelf2.getShelfId());

        // The room total must be the combined count from both shelves
        int total = shelfService.countBooksInRoom(room.getRoomId());
        assertEquals(5, total);
    }

    @Test
    public void roomWithNoShelves_returnsZero() {
        // A freshly created room has no shelves, so the book count must be zero
        Room emptyRoom = createRoom();
        int total = shelfService.countBooksInRoom(emptyRoom.getRoomId());
        assertEquals(0, total);
    }

    // --- Requirement 11: Find the room with the fewest books ---

    @Test
    public void multipleRooms_returnsRoomWithLowestBookCount() {
        // Room A gets 4 books spread across two shelves
        Room roomA = createRoom();
        Shelf shelfA = createShelf();
        shelfService.assignShelfToRoom(shelfA.getShelfId(), roomA.getRoomId());
        for (int i = 0; i < 4; i++) {
            shelfService.assignBookToShelf(createBook().getBookId(), shelfA.getShelfId());
        }

        // Room B gets only 1 book — it should be identified as the least-stocked room
        Room roomB = createRoom();
        Shelf shelfB = createShelf();
        shelfService.assignShelfToRoom(shelfB.getShelfId(), roomB.getRoomId());
        shelfService.assignBookToShelf(createBook().getBookId(), shelfB.getShelfId());

        // Room C gets 7 books, making it the most stocked of the three
        Room roomC = createRoom();
        Shelf shelfC = createShelf();
        shelfService.assignShelfToRoom(shelfC.getShelfId(), roomC.getRoomId());
        for (int i = 0; i < 7; i++) {
            shelfService.assignBookToShelf(createBook().getBookId(), shelfC.getShelfId());
        }

        Room fewest = shelfService.findRoomWithFewestBooks();
        assertEquals(roomB.getRoomId(), fewest.getRoomId());
    }
}
