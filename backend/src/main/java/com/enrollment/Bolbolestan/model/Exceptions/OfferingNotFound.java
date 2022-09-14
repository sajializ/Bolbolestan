package com.enrollment.Bolbolestan.model.Exceptions;

public class OfferingNotFound extends Exception {
    String text;

    public OfferingNotFound() {
        text = "درسی با کد موردنظر یافت نشد.";
    }
    public OfferingNotFound(String code, String classCode) {
        text = "شما درسی با کد " + code +  "و گروه " + classCode + " در برنامه خود ندارید.";
    }
    @Override
    public String toString() {
        return text;
    }
}
