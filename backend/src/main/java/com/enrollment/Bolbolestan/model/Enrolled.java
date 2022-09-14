package com.enrollment.Bolbolestan.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Enrolled {
    private String status;
    private final String studentId;
    private final String courseCode;
    private final String classCode;
    private final String instructor;
    private final String courseName;
    private final Integer units;

    public Enrolled(String studentId, String courseCode, String classCode, String courseName, String instructor, Integer units) {
        this.status = "non-finalized";
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.classCode = classCode;
        this.courseName = courseName;
        this.instructor = instructor;
        this.units = units;
    }

    public Enrolled(String studentId, String courseCode, String classCode, String courseName, String instructor, Integer units, String status) {
        this.status = status;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.classCode = classCode;
        this.courseName = courseName;
        this.instructor = instructor;
        this.units = units;
    }

    public String getStatus() { return status; }

    public String getStudentID() { return studentId; }

    public String getCourseCode() { return courseCode; }

    public String getClassCode() { return classCode; }

    public String getInstructor() { return instructor; }

    public String getCourseName() { return courseName; }

    public Integer getUnits() { return units; }

    public void setStatus(String status) { this.status = status; }

    public JsonNode getEnrolled() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("code", courseCode);
        jsonNode.put("classCode", classCode);
        jsonNode.put("name", courseName);
        jsonNode.put("instructor", instructor);
        jsonNode.put("units", units);
        if (this.status.equals("finalized"))
            jsonNode.put("status", "ثبت شده");
        if (this.status.equals("non-finalized"))
            jsonNode.put("status", "ثبت نهایی نشده");
        if (this.status.equals("waiting"))
            jsonNode.put("status", "در انتظار");
        if (this.status.equals("deleted"))
            jsonNode.put("status", "حذف شده");
        return jsonNode;
    }

}
