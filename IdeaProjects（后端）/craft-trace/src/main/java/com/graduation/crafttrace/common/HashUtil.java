package com.graduation.crafttrace.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

public class HashUtil {

    private HashUtil() {}

    /**
     * 基础 SHA-256（十六进制小写）
     * - null 会被当成空串
     * - 自动 trim（避免前后空格导致 hash 变化）
     */
    public static String sha256(String input) {
        return sha256Raw(normalize(input));
    }

    /**
     * 链式 hash：
     * currentHash = sha256( prevHash + "|" + payload )
     * - prevHash 为空时当成 "GENESIS"（链起点）
     * - payload 会 normalize
     */
    public static String chainHash(String prevHash, String payload) {
        String p = normalize(prevHash);
        if (p.isEmpty()) p = "GENESIS";
        String body = p + "|" + normalize(payload);
        return sha256Raw(body);
    }

    /**
     * 将多个字段拼成稳定的 payload 字符串（避免 null、空格、顺序不稳定）
     */
    public static String payloadOf(Object... parts) {
        if (parts == null || parts.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("|");
            Object v = parts[i];
            sb.append(normalize(v == null ? "" : Objects.toString(v)));
        }
        return sb.toString();
    }

    // ====== internal ======
    private static String sha256Raw(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }
}
