package com.enrollment.Bolbolestan.model.Exceptions;

public class SameEmailFound extends Exception {
    @Override
    public String toString() {
        return "دانشجویی با ایمیل وارد شده در سیستم وجود دارد. در صورت فراموشی رمز عبور از طریق صفحه لاگین برای دریافت رمز جدید اقدام کنید.";
    }
}
