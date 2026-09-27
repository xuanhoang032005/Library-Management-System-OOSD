package com.library.management.model;

public class BookStatusDTO {

    private String isbn;
    private String title;
    private String author;
    private String status;
    private int quantity;
    private boolean borrowedByMe;  
    private boolean requestedByMe; 
    public BookStatusDTO(String isbn, String title, String author, String status, int quantity, boolean borrowedByMe, boolean requestedByMe) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.status = status;
        this.quantity = quantity;
        this.borrowedByMe = borrowedByMe;
        this.requestedByMe = requestedByMe;
    }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getStatus() { return status; }
    public int getQuantity() { return quantity; }
    public boolean isBorrowedByMe() { return borrowedByMe; }
    public boolean isRequestedByMe() { return requestedByMe; }
}