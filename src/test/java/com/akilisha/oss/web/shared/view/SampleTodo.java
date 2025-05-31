package com.akilisha.oss.web.shared.view;

import java.time.LocalDate;

public class SampleTodo {
    private String title;
    private boolean done;
    private LocalDate createdOn;

    public SampleTodo(String title, boolean done, LocalDate createdOn) {
        this.title = title;
        this.done = done;
        this.createdOn = createdOn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDate createdOn) {
        this.createdOn = createdOn;
    }
}