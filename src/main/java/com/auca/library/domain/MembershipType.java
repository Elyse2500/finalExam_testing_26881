package com.auca.library.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "membership_type")
public class MembershipType {

    @Id
    @Column(name = "membership_type_id")
    private UUID membershipTypeId;

    @Column(name = "membership_name", unique = true)
    private String membershipName;

    @Column(name = "max_books")
    private int maxBooks;

    @Column(name = "price")
    private int price;

    public UUID getMembershipTypeId() { return membershipTypeId; }
    public void setMembershipTypeId(UUID membershipTypeId) { this.membershipTypeId = membershipTypeId; }

    public String getMembershipName() { return membershipName; }
    public void setMembershipName(String membershipName) { this.membershipName = membershipName; }

    public int getMaxBooks() { return maxBooks; }
    public void setMaxBooks(int maxBooks) { this.maxBooks = maxBooks; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
}
