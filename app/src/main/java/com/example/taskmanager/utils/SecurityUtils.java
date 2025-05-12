package com.example.taskmanager.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtils {

    // Mã hóa mật khẩu sử dụng SHA-256 với salt
    public static String hashPassword(String password) {
        try {
            // Tạo salt ngẫu nhiên
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Tạo MessageDigest với thuật toán SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Thêm salt vào MessageDigest
            md.update(salt);

            // Thêm mật khẩu và tạo hash
            byte[] hashedPassword = md.digest(password.getBytes());

            // Kết hợp salt và hash để lưu trữ
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            // Chuyển đổi thành chuỗi Base64 để lưu trữ
            return Base64.getEncoder().encodeToString(combined);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Kiểm tra mật khẩu nhập vào có khớp với mật khẩu đã mã hóa không
    public static boolean verifyPassword(String inputPassword, String storedPassword) {
        try {
            // Giải mã chuỗi Base64 đã lưu trữ
            byte[] combined = Base64.getDecoder().decode(storedPassword);

            // Tách salt và hash
            byte[] salt = new byte[16];
            byte[] storedHash = new byte[combined.length - 16];
            System.arraycopy(combined, 0, salt, 0, salt.length);
            System.arraycopy(combined, salt.length, storedHash, 0, storedHash.length);

            // Tạo MessageDigest với thuật toán SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Thêm salt vào MessageDigest
            md.update(salt);

            // Thêm mật khẩu nhập vào và tạo hash
            byte[] inputHash = md.digest(inputPassword.getBytes());

            // So sánh hai hash
            if (inputHash.length != storedHash.length) {
                return false;
            }

            for (int i = 0; i < inputHash.length; i++) {
                if (inputHash[i] != storedHash[i]) {
                    return false;
                }
            }

            return true;

        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}