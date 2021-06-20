package com.assessment;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PwdTest {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("Pass@123"));
    }
}
