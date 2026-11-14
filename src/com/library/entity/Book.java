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
    /** 上传者用户 id（null 表示系统预置）。 */
    private Integer uploaderId;
    /** 上传者昵称，联查带出。 */
    private String uploaderName;
    /** 审核状态：APPROVED / PENDING / REJECTED。 */
    private String status = "APPROVED";
    /** 平均评分（1-5，无评分为 0）。 */
    private double avgStars;
    /** 评分人数。 */
    private int ratingCount;
    /** 是否属于最近上新的 3 本（用于封面“新”角标）。 */
    private boolean newArrival;
    /** 书架中加入时间（仅书架查询填充）。 */
    private Timestamp shelfAddedAt;
    /** 书架中上次阅读章节（仅书架查询填充）。 */
    private int lastChapter;
    /** 搜索结果中的高亮 HTML / 上下文摘要（仅查询时填充）。 */
    private String searchNameHtml;
    private String searchAuthorHtml;
    private String searchPublisherHtml;
    private String searchSnippetHtml;
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

    public Integer getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Integer uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAvgStars() {
        return avgStars;
    }

    public void setAvgStars(double avgStars) {
        this.avgStars = avgStars;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public boolean isNewArrival() {
        return newArrival;
    }

    public void setNewArrival(boolean newArrival) {
        this.newArrival = newArrival;
    }

    public Timestamp getShelfAddedAt() {
        return shelfAddedAt;
    }

    public void setShelfAddedAt(Timestamp shelfAddedAt) {
        this.shelfAddedAt = shelfAddedAt;
    }

    public int getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(int lastChapter) {
        this.lastChapter = lastChapter;
    }

    public String getSearchNameHtml() {
        return searchNameHtml;
    }

    public void setSearchNameHtml(String searchNameHtml) {
        this.searchNameHtml = searchNameHtml;
    }

    public String getSearchAuthorHtml() {
        return searchAuthorHtml;
    }

    public void setSearchAuthorHtml(String searchAuthorHtml) {
        this.searchAuthorHtml = searchAuthorHtml;
    }

    public String getSearchPublisherHtml() {
        return searchPublisherHtml;
    }

    public void setSearchPublisherHtml(String searchPublisherHtml) {
        this.searchPublisherHtml = searchPublisherHtml;
    }

    public String getSearchSnippetHtml() {
        return searchSnippetHtml;
    }

    public void setSearchSnippetHtml(String searchSnippetHtml) {
        this.searchSnippetHtml = searchSnippetHtml;
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }

    public String getStatusLabel() {
        if (isPending()) {
            return "待审核";
        }
        if (isRejected()) {
            return "未通过";
        }
        return "已上架";
    }

    /** 平均分显示，如 4.5；无评分返回空串。 */
    public String getAvgStarsLabel() {
        if (ratingCount <= 0) {
            return "";
        }
        return String.format(java.util.Locale.ROOT, "%.1f", avgStars);
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
