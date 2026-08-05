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
}
