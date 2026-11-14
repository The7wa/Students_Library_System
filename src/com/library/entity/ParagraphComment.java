package com.library.entity;

import java.sql.Timestamp;

/**
 * 段落公开评论：任何登录用户都可以查看、评论、回复。
 * 与私有 annotation 完全分开，不暴露个人批注。
 */
public class ParagraphComment {
    private int id;
    private int bookId;
    private int chapterIndex;
    private int paragraphIndex;
    private String quote;
    private int userId;
    private Integer parentId;
    private String content;
    private Timestamp createdAt;
    private String userName;
    private String userAvatar;
    private boolean mine;

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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public String getUserAvatarText() {
        if (userAvatar != null && !userAvatar.isBlank()) {
            return userAvatar;
        }
        String name = userName == null || userName.isBlank() ? "读" : userName;
        return name.substring(0, 1);
    }
}
