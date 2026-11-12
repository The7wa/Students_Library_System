package com.library.entity;

import java.sql.Timestamp;

/**
 * 图书。与 category 表联查时一并带出板块信息，便于书架展示。
 */
public class Book {
    private int id;
    private int categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private String name;
    private String author;
    private String isbn;
    private String publisher;
    private String description;
    /** 正文 TXT 在正文目录中的文件名（null 表示未收录正文）。 */
    private String contentFile;
    /** 上传时的原始文件名，仅用于展示。 */
    private String contentName;
    private Timestamp createTime;
    private Timestamp updateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }

    public void setCategoryIcon(String categoryIcon) {
        this.categoryIcon = categoryIcon;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentFile() {
        return contentFile;
    }

    public void setContentFile(String contentFile) {
        this.contentFile = contentFile;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    /** 无封面图时用书名首字生成封面。 */
    public String getInitial() {
        if (name == null || name.isBlank()) {
            return "书";
        }
        return name.substring(0, 1);
    }

    /** 是否已收录正文 TXT，可进入阅读页。 */
    public boolean isHasContent() {
        return contentFile != null && !contentFile.isBlank();
    }
}
