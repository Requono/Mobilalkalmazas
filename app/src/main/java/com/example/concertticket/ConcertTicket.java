package com.example.concertticket;

public class ConcertTicket {
    private String id;
    private String title;
    private String description;
    private String price;
    private int imageResource;

    public ConcertTicket() { }

    public ConcertTicket(String title, String description, String price, int imageResource) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageResource = imageResource;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPrice() { return price; }
    public int getImageResource() { return imageResource; }

    public String _getId() { return id; }
    public void setId(String id) { this.id = id;}
}
