package com.enrollment.Bolbolestan.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Grade {
    private final String studentId;
    private final String courseName;
    private String state;
    private final String code;
    private final Integer score;
    private final Integer units;
    private final Integer term;
    public Grade(String studentId, String code, String name, Integer units, Integer score, Integer term) {
        this.studentId = studentId;
        this.code = code;
        this.score = score;
        this.term = term;
        this.courseName = name;
        this.units = units;
        this.state = "قبول";
        if (this.score < 10)
            this.state = "مردود";
    }

    public JsonNode getGrade() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("code", this.code);
        jsonNode.put("name", this.courseName);
        jsonNode.put("units", this.units);
        jsonNode.put("grade", this.score);
        jsonNode.put("state", this.state);
        return jsonNode;
    }

    public String getStudentId() { return studentId; }

    public String getCode() { return code; }

    public Integer getScore() { return score; }

    public Integer getUnits() { return units; }

    public Integer getTerm() { return term; }

    public String getCourseName() { return courseName; }
}
