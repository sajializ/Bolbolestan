package com.enrollment.Bolbolestan.model.Exceptions;

public class CapacityError extends Exception {
    private String code;

    public CapacityError(String _code) {
        code = _code;
    }

    @Override
    public String toString() {
        return "ظرفیت درس " + code + " تکمیل است";
    }
}
