package com.library.util;

import com.library.entity.Chapter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TXT 正文读取与章节切分。
 *
 * <p>支持 UTF-8（含 BOM）、UTF-16（带 BOM）与 GBK/GB18030 编码，
 * 并按「第 X 章 / 第 X 回 / 序章 / 番外 / Chapter N」等标题自动分章。
 * 没有章节标题的 TXT 会按长度切分成若干节。</p>
 */
public final class TxtBookParser {

    private static final Pattern HEADING = Pattern.compile(
            "^(?:第[0-9０-９零〇一二三四五六七八九十百千万两]{1,12}[章回节卷部篇集]"
                    + "(?:[ \\u3000:：、.·\\-—]*\\S.{0,38})?"
                    + "|(?:序章|序言|自序|楔子|引子|前言|后记|尾声|终章|番外|外传)"
                    + "(?:[ \\u3000:：、.·\\-—]*\\S.{0,30})?"
                    + "|Chapter[ \\u3000]+[0-9]{1,4}(?:[ \\u3000:：、.·\\-—]*\\S.{0,38})?)$",
            Pattern.CASE_INSENSITIVE);

    private static final int SMALL_BOOK_CHARS = 12000;
    private static final int CHUNK_CHARS = 6000;

    private TxtBookParser() {
    }

    public static List<Chapter> parse(Path file) throws IOException {
        long size = Files.size(file);
        if (size > BookContentStore.MAX_READ_SIZE) {
            throw new IOException("正文文件超过 32MB，暂不支持在线阅读");
        }
        String text = readText(file);
        return split(text);
    }

    /**
     * 读取 TXT 文本，自动判断编码。
     */
    public static String readText(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        if (bytes.length == 0) {
            return "";
        }

        String text;
        if (startsWith(bytes, (byte) 0xEF, (byte) 0xBB, (byte) 0xBF)) {
            text = new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        } else if (startsWith(bytes, (byte) 0xFF, (byte) 0xFE)) {
            text = new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        } else if (startsWith(bytes, (byte) 0xFE, (byte) 0xFF)) {
            text = new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        } else {
            text = decodeUtf8(bytes);
            if (text == null) {
                // 国内下载的 TXT 很多是 GBK，GB18030 是它的超集。
                text = new String(bytes, Charset.forName("GB18030"));
            }
        }

        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }

    /**
     * 按章节标题切分；找不到标题时按固定长度切分。
     */
    public static List<Chapter> split(String rawText) {
        String text = rawText == null ? "" : rawText;
        if (text.isEmpty()) {
            return new ArrayList<>();
        }

        List<Chapter> chapters = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        String currentTitle = null;
        boolean foundHeading = false;

        for (String line : text.split("\n", -1)) {
            if (isHeading(line)) {
                foundHeading = true;
                addChapter(chapters, currentTitle, buffer);
                currentTitle = line.trim();
                buffer.setLength(0);
            } else {
                buffer.append(line).append('\n');
            }
        }
        addChapter(chapters, currentTitle, buffer);

        if (!foundHeading) {
            return chunk(text);
        }
        return chapters;
    }

    private static void addChapter(List<Chapter> chapters, String title, StringBuilder buffer) {
        String content = clean(buffer);
        if (title == null && content.isEmpty()) {
            return;
        }
        // 很多下载的 TXT 开头是书名、作者等元信息，不把它单独列成一章。
        if (title == null && content.length() < 300) {
            return;
        }
        String chapterTitle = title == null ? "开篇" : title;
        if (content.isEmpty()) {
            content = "（本章暂无内容）";
        }
        chapters.add(new Chapter(chapters.size(), chapterTitle, content));
    }

    private static List<Chapter> chunk(String rawText) {
        List<Chapter> chapters = new ArrayList<>();
        String text = rawText.trim();
        if (text.isEmpty()) {
            return chapters;
        }
        if (text.length() <= SMALL_BOOK_CHARS) {
            chapters.add(new Chapter(0, "全文", text));
            return chapters;
        }

        int start = 0;
        int part = 1;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + CHUNK_CHARS);
            if (end < text.length()) {
                int cut = text.lastIndexOf('\n', end);
                if (cut > start + CHUNK_CHARS / 2) {
                    end = cut;
                }
            }
            String content = text.substring(start, end).trim();
            if (!content.isEmpty()) {
                chapters.add(new Chapter(chapters.size(), "第 " + part + " 节", content));
                part++;
            }
            start = end;
            while (start < text.length() && text.charAt(start) == '\n') {
                start++;
            }
        }
        return chapters;
    }

    private static boolean isHeading(String line) {
        String text = line.trim();
        if (text.isEmpty() || text.length() > 50) {
            return false;
        }
        if (text.indexOf('。') >= 0 || text.indexOf('！') >= 0 || text.indexOf('？') >= 0) {
            return false;
        }
        return HEADING.matcher(text).matches();
    }

    private static String clean(StringBuilder buffer) {
        String text = buffer.toString();
        int start = 0;
        int end = text.length();
        while (start < end && Character.isWhitespace(text.charAt(start))) {
            start++;
        }
        while (end > start && Character.isWhitespace(text.charAt(end - 1))) {
            end--;
        }
        return text.substring(start, end);
    }

    private static boolean startsWith(byte[] bytes, byte... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static String decodeUtf8(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }
}
