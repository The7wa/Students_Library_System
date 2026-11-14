package com.library.entity;

import java.sql.Timestamp;

/**
 * 站内通知。
 */
public class Notification {
    private int id;
    private int userId;
    private String type;
    private String message;
    private String link;
    private boolean read;
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getTypeLabel() {
        if (type == null) {
            return "系统";
        }
        switch (type) {
            case "UPLOAD": return "新上传";
            case "APPROVE": return "审核通过";
            case "REJECT": return "审核驳回";
            case "USER": return "用户";
            case "COMMENT": return "评论";
            default: return "系统";
        }
    }
}
