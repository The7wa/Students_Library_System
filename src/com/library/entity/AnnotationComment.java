package com.library.entity;

import java.sql.Timestamp;

/**
 * 批注下的公开评论，parentId 不为空时表示回复。
 */
public class AnnotationComment {
    private int id;
    private int annotationId;
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

    public int getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(int annotationId) {
        this.annotationId = annotationId;
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
