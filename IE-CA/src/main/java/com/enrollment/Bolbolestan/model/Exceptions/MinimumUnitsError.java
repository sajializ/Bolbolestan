package com.enrollment.Bolbolestan.model.Exceptions;

public class MinimumUnitsError extends Exception {
    @Override
    public String toString() {
        return "تعداد واحدهای برداشته شده نمیتواند از ۱۲ کمتر باشد";
    }
}
