package com.library.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Web 层小工具。
 */
public final class WebUtil {

    private WebUtil() {
    }

    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static int intValue(String value, int defaultValue) {
        try {
            return Integer.parseInt(trim(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void flash(HttpServletRequest request, String type, String message) {
        Map<String, String> flash = new LinkedHashMap<>();
        flash.put("type", type);
        flash.put("message", message);
        request.getSession().setAttribute("flash", flash);
    }

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    public static String greeting() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 6) {
            return "夜深了";
        }
        if (hour < 12) {
            return "早上好";
        }
        if (hour < 14) {
            return "中午好";
        }
        if (hour < 18) {
            return "下午好";
        }
        return "晚上好";
    }

    public static String greetingEn() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 6) {
            return "Good night";
        }
        if (hour < 12) {
            return "Good morning";
        }
        if (hour < 18) {
            return "Good afternoon";
        }
        return "Good evening";
    }

    /** 把用户输入的安全文本转成 HTML 属性安全的字符串（JSTL fn:escapeXml 的互补工具）。 */
    public static String attr(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&#39;");
    }
}
