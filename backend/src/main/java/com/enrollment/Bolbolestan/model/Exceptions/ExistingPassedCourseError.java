package com.enrollment.Bolbolestan.model.Exceptions;

public class ExistingPassedCourseError extends Exception {
    private String code;

    public ExistingPassedCourseError(String _code) {
        this.code = _code;
    }

    @Override
    public String toString() {
        return "درس با کد " + code + " قبلا گذرانده شده است";
    }
}
