package com.auca.library;

import com.auca.library.domain.Membership;
import com.auca.library.domain.MembershipType;
import com.auca.library.domain.User;
import com.auca.library.domain.enums.Gender;
import com.auca.library.domain.enums.MembershipStatus;
import com.auca.library.domain.enums.RoleType;
import com.auca.library.service.MembershipService;
import com.auca.library.service.UserService;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class MembershipTest {

    private MembershipService membershipService;
    private UserService userService;

    @Before
    public void setUp() {
        membershipService = new MembershipService();
        userService = new UserService();
    }

    private User createUser() {
        User user = new User();
        user.setPersonId(UUID.randomUUID().toString());
        user.setFirstName("Bob");
        user.setLastName("Mugisha");
        user.setGender(Gender.MALE);
        user.setPhoneNumber("0788000001");
        user.setUserName("bob_" + UUID.randomUUID());
        user.setPassword("pass5678");
        user.setRole(RoleType.STUDENT);
        return userService.save(user);
    }

    private MembershipType createGoldType() {
        MembershipType gold = new MembershipType();
        gold.setMembershipTypeId(UUID.randomUUID());
        gold.setMembershipName("Gold_" + UUID.randomUUID().toString().substring(0, 4));
        gold.setPrice(50);
        gold.setMaxBooks(5);
        return membershipService.saveMembershipType(gold);
    }

    @Test
    public void registerMembership_gold_createsPendingMembershipLinkedToGoldType() {
        User user = createUser();
        MembershipType gold = createGoldType();

        Membership membership = membershipService.registerMembership(
                UUID.fromString(user.getPersonId()), gold.getMembershipTypeId());

        assertNotNull(membership.getMembershipId());
        assertEquals(MembershipStatus.PENDING, membership.getMembershipStatus());
        assertEquals(gold.getMembershipTypeId(), membership.getMembershipType().getMembershipTypeId());
        assertEquals(user.getPersonId(), membership.getReader().getPersonId());
    }

    @Test(expected = IllegalStateException.class)
    public void registerMembership_userAlreadyHasActiveMembership_throwsException() {
        User user = createUser();
        MembershipType gold = createGoldType();
        UUID userId = UUID.fromString(user.getPersonId());

        membershipService.registerMembership(userId, gold.getMembershipTypeId());
        membershipService.registerMembership(userId, gold.getMembershipTypeId());
    }
}
