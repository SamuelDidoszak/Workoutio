package com.example.workout.model;

public class Note {
    private int noteId;
    private String date;
    private String note;


    public Note() {
    }

    public Note(String date, String note) {
        this.date = date;
        this.note = note;
    }

    public Note(int noteId, String date, String note) {
        this.noteId = noteId;
        this.date = date;
        this.note = note;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
