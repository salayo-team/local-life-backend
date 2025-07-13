package com.salayo.locallifebackend.global.util;

public class ValidationPatterns {

    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,12}$";
    public static final String PHONE_NUMBER_REGEX = "^010-?\\d{4}-?\\d{4}$";
    public static final String BIRTH_REGEX = "^\\d{8}$";

    private ValidationPatterns() {}
}
