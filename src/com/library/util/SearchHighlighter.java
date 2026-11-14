package com.library.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 搜索关键字高亮与上下文摘要。所有输出都做了 HTML 转义，可安全放入 JSP。
 */
public final class SearchHighlighter {

    private SearchHighlighter() {
    }

    /**
     * 将 text 中出现的 keyword 用 &lt;mark&gt; 包裹，其余内容 HTML 转义。
     */
    public static String highlight(String text, String keyword) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (keyword == null || keyword.isEmpty()) {
            return escape(text);
        }
        Pattern pattern = Pattern.compile(Pattern.quote(keyword),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            out.append(escape(text.substring(last, matcher.start())));
            out.append("<mark class=\"search-hit\">")
                    .append(escape(matcher.group()))
                    .append("</mark>");
            last = matcher.end();
        }
        out.append(escape(text.substring(last)));
        return out.toString();
    }

    /**
     * 截取关键字前后各 radius 个字符作为摘要，并高亮关键字。
     * 找不到关键字时返回开头一段，便于展示简介。
     */
    public static String snippet(String text, String keyword, int radius) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String source = text.replaceAll("\\s+", " ").trim();
        if (source.isEmpty()) {
            return "";
        }
        if (keyword == null || keyword.isEmpty()) {
            return escape(source.length() > radius * 2 ? source.substring(0, radius * 2) + "…" : source);
        }
        int index = indexOfIgnoreCase(source, keyword);
        if (index < 0) {
            String head = source.length() > radius * 2 ? source.substring(0, radius * 2) + "…" : source;
            return escape(head);
        }
        int start = Math.max(0, index - radius);
        int end = Math.min(source.length(), index + keyword.length() + radius);
        String part = source.substring(start, end);
        String prefix = start > 0 ? "…" : "";
        String suffix = end < source.length() ? "…" : "";
        return prefix + highlight(part, keyword) + suffix;
    }

    private static int indexOfIgnoreCase(String source, String keyword) {
        int max = source.length() - keyword.length();
        for (int i = 0; i <= max; i++) {
            if (source.regionMatches(true, i, keyword, 0, keyword.length())) {
                return i;
            }
        }
        return -1;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&': out.append("&amp;"); break;
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&#39;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }
}
