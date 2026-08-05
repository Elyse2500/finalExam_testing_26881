package com.auca.library.dao;

import com.auca.library.domain.Membership;
import com.auca.library.domain.MembershipType;
import com.auca.library.domain.enums.MembershipStatus;
import com.auca.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.UUID;

public class MembershipDAO {

    // Persist a new membership record into the database
    public Membership save(Membership membership) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(membership);
            tx.commit();
            return membership;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    // Check whether the user already holds an APPROVED or PENDING membership
    public Membership findActiveMembership(UUID userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Membership m WHERE m.reader.personId = :userId " +
                    "AND m.membershipStatus IN (:approved, :pending)", Membership.class)
                    .setParameter("userId", userId.toString())
                    .setParameter("approved", MembershipStatus.APPROVED)
                    .setParameter("pending", MembershipStatus.PENDING)
                    .uniqueResult();
        }
    }

    // Retrieve the membership plan details by its UUID
    public MembershipType findMembershipTypeById(UUID membershipTypeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MembershipType.class, membershipTypeId);
        }
    }

    // Persist a new membership type (Gold, Silver, Striver)
    public MembershipType saveMembershipType(MembershipType membershipType) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(membershipType);
            tx.commit();
            return membershipType;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /*
     * Approves a membership so the reader gains borrowing rights.
     * Used in test setup to simulate a librarian approving a registration.
     */
    public Membership approveMembership(UUID membershipId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Membership membership = session.get(Membership.class, membershipId);
            membership.setMembershipStatus(MembershipStatus.APPROVED);
            session.merge(membership);
            tx.commit();
            return membership;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
