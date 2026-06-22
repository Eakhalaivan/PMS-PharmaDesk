package com.pharmadesk.backend;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class HashTest {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder(12).encode("password"));
    }
}
