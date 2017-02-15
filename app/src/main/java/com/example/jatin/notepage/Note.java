package com.example.jatin.notepage;

public class Note {

    private int id;
    private String message;
    private boolean completed;

    Note(){}

    Note(String message){
        this.message = message;
    }

    Note(String message, int id, int completed){
        this.id = id;
        this.message = message;
        this.completed = completed!=0;
    }

    //getter methods
    public String getMessage(){
        return this.message;
    }
    public int getId(){
        return this.id;
    }
    public boolean getCompleted(){
        return this.completed;
    }

    //setter methods
    public void setMessage(String message){
        this.message = message;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setCompleted(boolean completed){
        this.completed = completed;
    }

}