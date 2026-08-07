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

    public Membership registerMembership(UUID userId, UUID membershipTypeId) {
        Membership existing = membershipDAO.findActiveMembership(userId);
        if (existing != null) {
            throw new IllegalStateException("user already has an active membership");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found: " + userId);
        }

        MembershipType membershipType = membershipDAO.findMembershipTypeById(membershipTypeId);
        if (membershipType == null) {
            throw new IllegalArgumentException("membership type not found: " + membershipTypeId);
        }

        // new membership starts as PENDING until librarian approves
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

    public Membership approveMembership(UUID membershipId) {
        return membershipDAO.approveMembership(membershipId);
    }
}
