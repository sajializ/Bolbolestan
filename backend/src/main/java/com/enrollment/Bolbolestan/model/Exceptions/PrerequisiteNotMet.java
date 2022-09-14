package com.enrollment.Bolbolestan.model.Exceptions;

public class PrerequisiteNotMet extends Exception {
    private String pre;
    private String post;
    public PrerequisiteNotMet(String _code1, String _code2) {
        post = _code1;
        pre = _code2;
    }
    @Override
    public String toString() {
        return "درس " + post + " دارای پیشنیازی درس " + pre + " است که گذرانده نشده است";
    }
}