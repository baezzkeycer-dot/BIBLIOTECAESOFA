package com.example.bibliotecaesofa;

public class Book {

    private final String id;
    private final String title;
    private final String author;
    private final String synopsis;
    private final int coverRes;

    private String reservedBy;
    private long reservedUntil;
    private String pickupCode;

    public Book(String id, String title, String author, String synopsis, int coverRes) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.synopsis = synopsis;
        this.coverRes = coverRes;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public int getCoverRes() {
        return coverRes;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    public long getReservedUntil() {
        return reservedUntil;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public boolean isReserved() {
        return reservedBy != null && reservedUntil > System.currentTimeMillis();
    }

    void setReservation(String user, long until, String code) {
        reservedBy = user;
        reservedUntil = until;
        pickupCode = code;
    }

    void clearReservation() {
        reservedBy = null;
        reservedUntil = 0L;
        pickupCode = null;
    }
}
