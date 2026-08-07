package com.auca.library.dao;

import com.auca.library.domain.Membership;
import com.auca.library.domain.MembershipType;
import com.auca.library.domain.enums.MembershipStatus;
import com.auca.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.UUID;

public class MembershipDAO {

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

    public MembershipType findMembershipTypeById(UUID membershipTypeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MembershipType.class, membershipTypeId);
        }
    }

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
