package com.example.aanya.todolist.models;

/**
 * Created by aanya on 7/13/2016.
 */
public class Task {

    int id;
    String title;
    String date;
    boolean selected = false;

    public Task(int id, String title, String date) {
        this.id = id;
        this.title = title;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
