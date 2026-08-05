package com.auca.library.service;

import com.auca.library.dao.ShelfDAO;
import com.auca.library.domain.Room;
import com.auca.library.domain.Shelf;

import java.util.UUID;

public class ShelfService {

    private final ShelfDAO shelfDAO = new ShelfDAO();

    /*
     * Assigns a book to a shelf and updates the shelf's available stock.
     * The librarian calls this when a new book arrives and is placed on a shelf.
     */
    public void assignBookToShelf(UUID bookId, UUID shelfId) {
        shelfDAO.assignBookToShelf(bookId, shelfId);
    }

    /*
     * Assigns a shelf to a room. Called when the library is being organised
     * or when a shelf is moved from one room to another.
     */
    public void assignShelfToRoom(UUID shelfId, UUID roomId) {
        shelfDAO.assignShelfToRoom(shelfId, roomId);
    }

    // Persists a new shelf — used during library setup or test preparation
    public Shelf saveShelf(Shelf shelf) {
        return shelfDAO.saveShelf(shelf);
    }

    // Persists a new room — used during library setup or test preparation
    public Room saveRoom(Room room) {
        return shelfDAO.saveRoom(room);
    }
}
