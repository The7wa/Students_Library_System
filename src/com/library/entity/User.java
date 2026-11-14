package com.library.entity;

import java.sql.Timestamp;

/**
 * 系统用户。
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String avatar;
    private String bio;
    private String lang = "zh";
    private String role = "USER";
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /** 页面展示名：优先昵称。 */
    public String getDisplayName() {
        return nickname == null || nickname.isBlank() ? username : nickname;
    }

    /** 头像：优先用户设置的 emoji，否则用昵称首字。 */
    public String getAvatarText() {
        if (avatar != null && !avatar.isBlank()) {
            return avatar;
        }
        String name = getDisplayName();
        return name == null || name.isEmpty() ? "书" : name.substring(0, 1);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
