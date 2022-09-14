package com.enrollment.Bolbolestan.model.Exceptions;

public class LoginNotFound extends Exception {
    @Override
    public String toString() {
        return "You have not logged in yet. Sign in to proceed.";
    }
}
