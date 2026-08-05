package com.auca.library.service;

import com.auca.library.dao.MembershipDAO;
import com.auca.library.dao.UserDAO;
import com.auca.library.domain.Membership;
import com.auca.library.domain.MembershipType;
import com.auca.library.domain.User;
import com.auca.library.domain.enums.MembershipStatus;

import java.util.Date;
import java.util.UUID;

public class MembershipService {

    private final MembershipDAO membershipDAO = new MembershipDAO();
    private final UserDAO userDAO = new UserDAO();

    /*
     * Registers a membership for the given user under the chosen plan.
     * Before creating a new record, we check whether the user already
     * holds an active (APPROVED or PENDING) membership to prevent duplicates.
     * New memberships always start with PENDING status until approved by a librarian.
     */
    public Membership registerMembership(UUID userId, UUID membershipTypeId) {
        // Reject the request if the user already has an active membership
        Membership existing = membershipDAO.findActiveMembership(userId);
        if (existing != null) {
            throw new IllegalStateException(
                    "User already has an active membership. Cancel it before registering a new one.");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found for id: " + userId);
        }

        MembershipType membershipType = membershipDAO.findMembershipTypeById(membershipTypeId);
        if (membershipType == null) {
            throw new IllegalArgumentException("Membership type not found for id: " + membershipTypeId);
        }

        // Build the membership record with today as the registration date
        Membership membership = new Membership();
        membership.setMembershipId(UUID.randomUUID());
        membership.setMembershipCode("MEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        membership.setRegistrationDate(new Date());
        membership.setMembershipStatus(MembershipStatus.PENDING);
        membership.setReader(user);
        membership.setMembershipType(membershipType);

        return membershipDAO.save(membership);
    }

    public MembershipType saveMembershipType(MembershipType membershipType) {
        return membershipDAO.saveMembershipType(membershipType);
    }
}
