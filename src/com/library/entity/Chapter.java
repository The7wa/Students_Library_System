package com.library.entity;

/**
 * 阅读页的一个章节。由 TXT 正文按章节标题切分而来。
 */
public class Chapter {
    private final int index;
    private final String title;
    private final String content;

    public Chapter(int index, String title, String content) {
        this.index = index;
        this.title = title;
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getCharCount() {
        return content == null ? 0 : content.length();
    }
}
