package com.auca.library.dao;

import com.auca.library.domain.Book;
import com.auca.library.domain.Room;
import com.auca.library.domain.Shelf;
import com.auca.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.UUID;

public class ShelfDAO {

    /*
     * Links a book to a specific shelf and bumps the shelf's available stock
     * by one to reflect the newly placed physical copy.
     */
    public void assignBookToShelf(UUID bookId, UUID shelfId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Book book = session.get(Book.class, bookId);
            if (book == null) throw new IllegalArgumentException("Book not found: " + bookId);

            Shelf shelf = session.get(Shelf.class, shelfId);
            if (shelf == null) throw new IllegalArgumentException("Shelf not found: " + shelfId);

            // Place the book on the shelf and update the running stock count
            book.setShelf(shelf);
            shelf.setAvailableStock(shelf.getAvailableStock() + 1);
            shelf.setInitialStock(shelf.getInitialStock() + 1);

            session.merge(book);
            session.merge(shelf);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /*
     * Places a shelf inside a room. A shelf must belong to exactly one room
     * so this replaces any previous room assignment.
     */
    public void assignShelfToRoom(UUID shelfId, UUID roomId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Shelf shelf = session.get(Shelf.class, shelfId);
            if (shelf == null) throw new IllegalArgumentException("Shelf not found: " + shelfId);

            Room room = session.get(Room.class, roomId);
            if (room == null) throw new IllegalArgumentException("Room not found: " + roomId);

            shelf.setRoom(room);
            session.merge(shelf);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // Saves a new shelf record — used in test setup before assigning books or rooms
    public Shelf saveShelf(Shelf shelf) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(shelf);
            tx.commit();
            return shelf;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // Saves a new room record — used in test setup before assigning shelves
    public Room saveRoom(Room room) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(room);
            tx.commit();
            return room;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // Reloads a shelf from the database to verify changes made during a test
    public Shelf findShelfById(UUID shelfId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Shelf.class, shelfId);
        }
    }

    // Reloads a book from the database to verify shelf assignment after the operation
    public Book findBookById(UUID bookId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Book.class, bookId);
        }
    }

    /*
     * Counts every book sitting on any shelf that belongs to the given room.
     * The query sums initialStock across all shelves in the room because
     * initialStock tracks the total number of physical copies placed there,
     * regardless of whether they are currently borrowed or on the shelf.
     */
    public int countBooksInRoom(UUID roomId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COALESCE(SUM(s.initialStock), 0) FROM Shelf s WHERE s.room.roomId = :roomId",
                    Long.class)
                    .setParameter("roomId", roomId)
                    .uniqueResult();
            return count == null ? 0 : count.intValue();
        }
    }

    /*
     * Scans every room and returns the one whose shelves hold the fewest books in total.
     * Rooms with no shelves at all are treated as having zero books and can win.
     * The result is ordered ascending so the first row is always the least-stocked room.
     */
    public Room findRoomWithFewestBooks() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT r FROM Room r LEFT JOIN Shelf s ON s.room.roomId = r.roomId " +
                    "GROUP BY r.roomId, r.roomCode " +
                    "ORDER BY COALESCE(SUM(s.initialStock), 0) ASC",
                    Room.class)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    // Fetches a room by its UUID — used to reload and verify state after operations
    public Room findRoomById(UUID roomId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Room.class, roomId);
        }
    }
}
