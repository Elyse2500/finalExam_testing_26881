package com.example.domain;

import com.example.domain.enums.BookStatus;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @Column(name = "book_id")
    private UUID bookId;

    @Column(name = "title")
    private String title;

    @Column(name = "isbn_code")
    private String isbnCode;

    @Column(name = "publisher_name")
    private String publisherName;

    @Column(name = "edition")
    private int edition;

    @Temporal(TemporalType.DATE)
    @Column(name = "publication_year")
    private Date publicationYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_status")
    private BookStatus bookStatus;

    @ManyToOne
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;

    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIsbnCode() { return isbnCode; }
    public void setIsbnCode(String isbnCode) { this.isbnCode = isbnCode; }

    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }

    public int getEdition() { return edition; }
    public void setEdition(int edition) { this.edition = edition; }

    public Date getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Date publicationYear) { this.publicationYear = publicationYear; }

    public BookStatus getBookStatus() { return bookStatus; }
    public void setBookStatus(BookStatus bookStatus) { this.bookStatus = bookStatus; }

    public Shelf getShelf() { return shelf; }
    public void setShelf(Shelf shelf) { this.shelf = shelf; }
}
