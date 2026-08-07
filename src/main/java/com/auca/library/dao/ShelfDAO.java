package com.auca.library.dao;

import com.auca.library.domain.Book;
import com.auca.library.domain.Room;
import com.auca.library.domain.Shelf;
import com.auca.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.UUID;

public class ShelfDAO {

    public void assignBookToShelf(UUID bookId, UUID shelfId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Book book = session.get(Book.class, bookId);
            if (book == null) throw new IllegalArgumentException("book not found: " + bookId);

            Shelf shelf = session.get(Shelf.class, shelfId);
            if (shelf == null) throw new IllegalArgumentException("shelf not found: " + shelfId);

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

    public void assignShelfToRoom(UUID shelfId, UUID roomId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Shelf shelf = session.get(Shelf.class, shelfId);
            if (shelf == null) throw new IllegalArgumentException("shelf not found: " + shelfId);

            Room room = session.get(Room.class, roomId);
            if (room == null) throw new IllegalArgumentException("room not found: " + roomId);

            shelf.setRoom(room);
            session.merge(shelf);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

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

    public Shelf findShelfById(UUID shelfId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Shelf.class, shelfId);
        }
    }

    public Book findBookById(UUID bookId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Book.class, bookId);
        }
    }

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

    public Room findRoomById(UUID roomId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Room.class, roomId);
        }
    }
}
