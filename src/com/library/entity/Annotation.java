package com.library.entity;

import java.sql.Timestamp;

/**
 * 阅读批注 / 彩色笔记，仅本人可见。
 */
public class Annotation {
    private int id;
    private int bookId;
    private int userId;
    private int chapterIndex;
    private int paragraphIndex;
    private String quote;
    private String note;
    private String color;
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public int getParagraphIndex() {
        return paragraphIndex;
    }

    public void setParagraphIndex(int paragraphIndex) {
        this.paragraphIndex = paragraphIndex;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
