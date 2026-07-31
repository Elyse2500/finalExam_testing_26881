package com.example.domain;

import com.example.domain.enums.MembershipStatus;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "membership")
public class Membership {

    @Id
    @Column(name = "membership_id")
    private UUID membershipId;

    @Column(name = "membership_code")
    private String membershipCode;

    @Temporal(TemporalType.DATE)
    @Column(name = "registration_date")
    private Date registrationDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "expiring_time")
    private Date expiringTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status")
    private MembershipStatus membershipStatus;

    @ManyToOne
    @JoinColumn(name = "reader_id")
    private User reader;

    @ManyToOne
    @JoinColumn(name = "membership_type_id")
    private MembershipType membershipType;

    public UUID getMembershipId() { return membershipId; }
    public void setMembershipId(UUID membershipId) { this.membershipId = membershipId; }

    public String getMembershipCode() { return membershipCode; }
    public void setMembershipCode(String membershipCode) { this.membershipCode = membershipCode; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    public Date getExpiringTime() { return expiringTime; }
    public void setExpiringTime(Date expiringTime) { this.expiringTime = expiringTime; }

    public MembershipStatus getMembershipStatus() { return membershipStatus; }
    public void setMembershipStatus(MembershipStatus membershipStatus) { this.membershipStatus = membershipStatus; }

    public User getReader() { return reader; }
    public void setReader(User reader) { this.reader = reader; }

    public MembershipType getMembershipType() { return membershipType; }
    public void setMembershipType(MembershipType membershipType) { this.membershipType = membershipType; }
}
