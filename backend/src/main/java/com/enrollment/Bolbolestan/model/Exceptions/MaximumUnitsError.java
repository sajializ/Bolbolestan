package com.enrollment.Bolbolestan.model.Exceptions;

public class MaximumUnitsError extends Exception {
    @Override
    public String toString() {
        return "تعداد واحدهای برداشته شده نمیتواند از ۲۰ بیشتر باشد";
    }
}
