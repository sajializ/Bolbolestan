package com.enrollment.Bolbolestan.model;

public class Prerequisite {
    String course;
    String prerequisiteCode;
    String prerequisiteName;

    public Prerequisite(String courseCode, String prerequisiteCode, String prerequisiteName) {
        this.course = courseCode;
        this.prerequisiteCode = prerequisiteCode;
        this.prerequisiteName = prerequisiteName;
    }

    public void setPrerequisiteName(String prerequisiteName) {
        this.prerequisiteName = prerequisiteName;
    }

    public String getCourseCode() { return course; }

    public String getPrerequisiteCode() { return prerequisiteCode; }

    public String getPrerequisiteName() { return prerequisiteName; }

}
