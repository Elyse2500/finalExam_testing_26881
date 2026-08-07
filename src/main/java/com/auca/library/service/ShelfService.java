package com.auca.library.service;

import com.auca.library.dao.ShelfDAO;
import com.auca.library.domain.Room;
import com.auca.library.domain.Shelf;
import java.util.UUID;

public class ShelfService {

    private final ShelfDAO shelfDAO = new ShelfDAO();

    public void assignBookToShelf(UUID bookId, UUID shelfId) {
        shelfDAO.assignBookToShelf(bookId, shelfId);
    }

    public void assignShelfToRoom(UUID shelfId, UUID roomId) {
        shelfDAO.assignShelfToRoom(shelfId, roomId);
    }

    public Shelf saveShelf(Shelf shelf) {
        return shelfDAO.saveShelf(shelf);
    }

    public Room saveRoom(Room room) {
        return shelfDAO.saveRoom(room);
    }

    public int countBooksInRoom(UUID roomId) {
        return shelfDAO.countBooksInRoom(roomId);
    }

    public Room findRoomWithFewestBooks() {
        return shelfDAO.findRoomWithFewestBooks();
    }
}
