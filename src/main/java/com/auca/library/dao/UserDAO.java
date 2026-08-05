package com.auca.library.dao;

import com.auca.library.domain.User;
import com.auca.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDAO {

    public User save(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public String getProvinceNameByPersonId(String personId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT province.locationName FROM User u " +
                         "JOIN u.location village " +
                         "JOIN village.parent cell " +
                         "JOIN cell.parent sector " +
                         "JOIN sector.parent district " +
                         "JOIN district.parent province " +
                         "WHERE u.personId = :personId";
            return session.createQuery(hql, String.class)
                    .setParameter("personId", personId)
                    .uniqueResult();
        }
    }
}
