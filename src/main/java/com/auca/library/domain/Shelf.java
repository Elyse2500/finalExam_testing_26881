package com.auca.library.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "shelf")
public class Shelf {

    @Id
    @Column(name = "shelf_id")
    private UUID shelfId;

    @Column(name = "book_category")
    private String bookCategory;

    @Column(name = "initial_stock")
    private int initialStock;

    @Column(name = "borrowed_number")
    private int borrowedNumber;

    @Column(name = "available_stock")
    private int availableStock;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    public UUID getShelfId() { return shelfId; }
    public void setShelfId(UUID shelfId) { this.shelfId = shelfId; }

    public String getBookCategory() { return bookCategory; }
    public void setBookCategory(String bookCategory) { this.bookCategory = bookCategory; }

    public int getInitialStock() { return initialStock; }
    public void setInitialStock(int initialStock) { this.initialStock = initialStock; }

    public int getBorrowedNumber() { return borrowedNumber; }
    public void setBorrowedNumber(int borrowedNumber) { this.borrowedNumber = borrowedNumber; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
}
