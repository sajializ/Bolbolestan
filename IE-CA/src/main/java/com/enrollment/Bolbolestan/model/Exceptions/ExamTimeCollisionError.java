package com.enrollment.Bolbolestan.model.Exceptions;

public class ExamTimeCollisionError extends Exception {
    private String code1;
    private String code2;

    public ExamTimeCollisionError(String _code1, String _code2) {
        code1 = _code1;
        code2 = _code2;
    }

    @Override
    public String toString() {
        return "امتحان درس با کد " + code2 + " با درس با کد " + code1 + " تداخل دارد";
    }
}
