package com.enrollment.Bolbolestan.model.Exceptions;

public class SameStudentIdFound extends Exception {
    @Override
    public String toString() {
        return "دانشجویی با شماره دانشجویی وارد شده در سیستم وجود دارد. در صورت فراموشی رمز عبور از طریق صفحه لاگین برای دریافت رمز جدید اقدام کنید.";
    }
}
