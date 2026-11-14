package com.library.entity;

import java.sql.Timestamp;

/**
 * 阅读批注 / 彩色笔记。
 *
 * <p>批注的「原文 + 颜色」对所有读者可见；note 字段只对本人展示。
 * 同一段原文重复标记时，DAO 只更新最后一条，页面也只展示最新的一条。</p>
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

    // 阅读页 / 我的笔记展示用
    private String userName;
    private String userAvatar;
    private String bookName;
    private String bookAuthor;
    private String bookColor;
    private boolean bookHasContent;
    private int likeCount;
    private int commentCount;
    private boolean likedByMe;
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

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookColor() {
        return bookColor;
    }

    public void setBookColor(String bookColor) {
        this.bookColor = bookColor;
    }

    public boolean isBookHasContent() {
        return bookHasContent;
    }

    public void setBookHasContent(boolean bookHasContent) {
        this.bookHasContent = bookHasContent;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLikedByMe() {
        return likedByMe;
    }

    public void setLikedByMe(boolean likedByMe) {
        this.likedByMe = likedByMe;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    /** 头像兜底：用户名或昵称首字。 */
    public String getUserAvatarText() {
        if (userAvatar != null && !userAvatar.isBlank()) {
            return userAvatar;
        }
        String name = userName == null || userName.isBlank() ? "读" : userName;
        return name.substring(0, 1);
    }
}
