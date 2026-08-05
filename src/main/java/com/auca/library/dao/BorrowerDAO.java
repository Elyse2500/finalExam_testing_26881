package com.auca.library.dao;

import com.auca.library.domain.Book;
import com.auca.library.domain.Borrower;
import com.auca.library.domain.User;
import com.auca.library.domain.enums.BookStatus;
import com.auca.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.UUID;

public class BorrowerDAO {

    /*
     * Persists a new borrowing record. The book status update is handled
     * separately so each operation stays focused on a single responsibility.
     */
    public Borrower save(Borrower borrower) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(borrower);
            tx.commit();
            return borrower;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /*
     * Counts how many books the reader currently has out on loan —
     * meaning borrowed but not yet returned. Used to enforce membership limits.
     */
    public long countActiveByReader(UUID readerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT COUNT(b) FROM Borrower b WHERE b.reader.personId = :readerId " +
                    "AND b.returnDate IS NULL", Long.class)
                    .setParameter("readerId", readerId.toString())
                    .uniqueResult();
        }
    }

    // Fetch a book record by its UUID to check availability before lending
    public Book findBookById(UUID bookId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Book.class, bookId);
        }
    }

    // Fetch the user who is requesting to borrow the book
    public User findUserById(UUID readerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, readerId.toString());
        }
    }

    /*
     * Updates the book status after it has been borrowed or returned.
     * Keeping this in the DAO layer avoids mixing persistence logic into the service.
     */
    public void updateBookStatus(UUID bookId, BookStatus status) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Book book = session.get(Book.class, bookId);
            book.setBookStatus(status);
            session.merge(book);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /*
     * Saves a book that does not yet exist in the database.
     * Used in test setup to prepare AVAILABLE books before borrowing.
     */
    public Book saveBook(Book book) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(book);
            tx.commit();
            return book;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
