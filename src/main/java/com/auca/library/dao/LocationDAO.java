package com.auca.library.dao;

import com.auca.library.domain.Location;
import com.auca.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.UUID;

public class LocationDAO {

    public Location createLocation(Location location, UUID parentId) {
        // Check for duplicate location code
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(l) FROM Location l WHERE l.locationCode = :code", Long.class)
                    .setParameter("code", location.getLocationCode())
                    .uniqueResult();
            if (count != null && count > 0) {
                throw new IllegalArgumentException("Duplicate location code: " + location.getLocationCode());
            }
        }

        // Resolve parent if provided
        if (parentId != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Location parent = session.get(Location.class, parentId);
                if (parent == null) {
                    throw new IllegalArgumentException("Parent location not found for id: " + parentId);
                }
                location.setParent(parent);
            }
        }

        // Persist
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            location.setLocationId(UUID.randomUUID());
            session.persist(location);
            tx.commit();
            return location;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Location findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Location.class, id);
        }
    }

    public String getProvinceNameByVillageId(UUID villageId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Traverse: village -> cell -> sector -> district -> province
            String hql = "SELECT province.locationName FROM Location village " +
                         "JOIN village.parent cell " +
                         "JOIN cell.parent sector " +
                         "JOIN sector.parent district " +
                         "JOIN district.parent province " +
                         "WHERE village.locationId = :villageId";
            return session.createQuery(hql, String.class)
                    .setParameter("villageId", villageId)
                    .uniqueResult();
        }
    }
}
