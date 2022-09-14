package com.enrollment.Bolbolestan.model;

import com.enrollment.Bolbolestan.utilities.HashAlgorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;



public class Student {
    private final String id;
    private final String email;
    private final String password;
    private final String name;
    private final String secondName;
    private final String birthDate;
    private final String field;
    private final String faculty;
    private final String level;
    private final String status;
    private final String img;
    private float gpa;
    private Integer totalPassedUnits;

    public Student(String id, String name, String secondName, String birthDate, String field, String faculty, String level, String status, String imageUrl, String email, String password) {
        this.id = id;
        this.name = name;
        this.secondName = secondName;
        this.birthDate = birthDate;
        this.field = field;
        this.faculty = faculty;
        this.level = level;
        this.status = status;
        this.img = imageUrl;
        this.email = email;
        this.password = password;
        this.totalPassedUnits = 0;

    }

    public String getEmail() {
        return email;
    }

    public  String getPassword() { return password; }

    public String getId() {
        return id;
    }

    public  String getName() { return name; }

    public  String getSecondName() { return secondName; }

    public String getBirthDate() { return birthDate; }

    public String getField() { return field; }

    public String getFaculty() { return faculty; }

    public String getLevel() { return level; }

    public String getStatus() { return status; }

    public String getImg() { return img; }

    public Integer getTotalPassedUnits() { return totalPassedUnits; }

    public float getGPA() {
        return this.gpa;
    }

    public void setGPA(float avg) {
        this.gpa = avg;
    }

    public void setTotalPassedUnits(Integer total) {
        this.totalPassedUnits = total;
    }

    public JsonNode getProfile() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("studentId", this.id);
        jsonNode.put("firstName", this.name);
        jsonNode.put("lastName", this.secondName);
        jsonNode.put("birthDate", this.birthDate);
        jsonNode.put("field", this.field);
        jsonNode.put("faculty", this.faculty);
        jsonNode.put("level", this.level);
        jsonNode.put("status", this.status);
        jsonNode.put("imageUrl", this.img);
        jsonNode.put("totalPassedUnits", this.totalPassedUnits);
        jsonNode.put("GPA", this.getGPA());
        return jsonNode;
    }
}
