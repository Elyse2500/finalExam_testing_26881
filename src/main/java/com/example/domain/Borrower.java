package com.example.domain;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "borrower")
public class Borrower {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "reader_id")
    private User reader;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Temporal(TemporalType.DATE)
    @Column(name = "pickup_date")
    private Date pickupDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "due_date")
    private Date dueDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "return_date")
    private Date returnDate;

    @Column(name = "fine")
    private int fine;

    @Column(name = "late_charge_fee")
    private int lateChargeFee;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getReader() { return reader; }
    public void setReader(User reader) { this.reader = reader; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Date getPickupDate() { return pickupDate; }
    public void setPickupDate(Date pickupDate) { this.pickupDate = pickupDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public int getFine() { return fine; }
    public void setFine(int fine) { this.fine = fine; }

    public int getLateChargeFee() { return lateChargeFee; }
    public void setLateChargeFee(int lateChargeFee) { this.lateChargeFee = lateChargeFee; }
}
