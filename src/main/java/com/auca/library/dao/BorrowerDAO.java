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

    public long countActiveByReader(UUID readerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT COUNT(b) FROM Borrower b WHERE b.reader.personId = :readerId " +
                    "AND b.returnDate IS NULL", Long.class)
                    .setParameter("readerId", readerId.toString())
                    .uniqueResult();
        }
    }

    public Book findBookById(UUID bookId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Book.class, bookId);
        }
    }

    public User findUserById(UUID readerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, readerId.toString());
        }
    }

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

    public Borrower findById(UUID borrowerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Borrower.class, borrowerId);
        }
    }

    public Borrower updateFine(UUID borrowerId, int fine) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Borrower borrower = session.get(Borrower.class, borrowerId);
            borrower.setFine(fine);
            session.merge(borrower);
            tx.commit();
            return borrower;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

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
