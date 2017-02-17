package com.epam.client;

public class Resourse {

    private String filepath;
    private Client owner;

    public Resourse(String filepath, Client owner) {
        this.filepath = filepath;
        this.owner = owner;
    }

    public String getFilepath() {
        return filepath;
    }
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public Client getOwner() {
        return owner;
    }
    public synchronized void setOwner(Client owner) {
        this.owner = owner;
    }

}
