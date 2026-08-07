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

    private Shelf createShelf() {
        Shelf shelf = new Shelf();
        shelf.setShelfId(UUID.randomUUID());
        shelf.setBookCategory("Science");
        shelf.setInitialStock(0);
        shelf.setAvailableStock(0);
        shelf.setBorrowedNumber(0);
        return shelfService.saveShelf(shelf);
    }

    private Room createRoom() {
        Room room = new Room();
        room.setRoomId(UUID.randomUUID());
        room.setRoomCode("ROOM-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        return shelfService.saveRoom(room);
    }

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

    @Test
    public void assignBookToShelf_updatesBookShelfId() {
        Book book = createBook();
        Shelf shelf = createShelf();

        shelfService.assignBookToShelf(book.getBookId(), shelf.getShelfId());

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

        Shelf updated = shelfDAO.findShelfById(shelf.getShelfId());
        assertEquals(stockBefore + 1, updated.getAvailableStock());
    }

    @Test
    public void assignShelfToRoom_updatesShelfRoomId() {
        Shelf shelf = createShelf();
        Room room = createRoom();

        shelfService.assignShelfToRoom(shelf.getShelfId(), room.getRoomId());

        Shelf updated = shelfDAO.findShelfById(shelf.getShelfId());
        assertNotNull(updated.getRoom());
        assertEquals(room.getRoomId(), updated.getRoom().getRoomId());
    }

    @Test
    public void roomWithMultipleShelves_sumsBookCountsAcrossShelves() {
        Room room = createRoom();

        Shelf shelf1 = createShelf();
        shelfService.assignShelfToRoom(shelf1.getShelfId(), room.getRoomId());
        Book b1 = createBook();
        Book b2 = createBook();
        shelfService.assignBookToShelf(b1.getBookId(), shelf1.getShelfId());
        shelfService.assignBookToShelf(b2.getBookId(), shelf1.getShelfId());

        Shelf shelf2 = createShelf();
        shelfService.assignShelfToRoom(shelf2.getShelfId(), room.getRoomId());
        Book b3 = createBook();
        Book b4 = createBook();
        Book b5 = createBook();
        shelfService.assignBookToShelf(b3.getBookId(), shelf2.getShelfId());
        shelfService.assignBookToShelf(b4.getBookId(), shelf2.getShelfId());
        shelfService.assignBookToShelf(b5.getBookId(), shelf2.getShelfId());

        int total = shelfService.countBooksInRoom(room.getRoomId());
        assertEquals(5, total);
    }

    @Test
    public void roomWithNoShelves_returnsZero() {
        Room emptyRoom = createRoom();
        int total = shelfService.countBooksInRoom(emptyRoom.getRoomId());
        assertEquals(0, total);
    }

    @Test
    public void multipleRooms_returnsRoomWithLowestBookCount() {
        Room roomA = createRoom();
        Shelf shelfA = createShelf();
        shelfService.assignShelfToRoom(shelfA.getShelfId(), roomA.getRoomId());
        for (int i = 0; i < 4; i++) {
            shelfService.assignBookToShelf(createBook().getBookId(), shelfA.getShelfId());
        }

        Room roomB = createRoom();
        Shelf shelfB = createShelf();
        shelfService.assignShelfToRoom(shelfB.getShelfId(), roomB.getRoomId());
        shelfService.assignBookToShelf(createBook().getBookId(), shelfB.getShelfId());

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
