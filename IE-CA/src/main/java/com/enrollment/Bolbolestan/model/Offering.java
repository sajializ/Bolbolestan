package com.enrollment.Bolbolestan.model;

import com.enrollment.Bolbolestan.model.Exceptions.ClassTimeCollisionError;
import com.enrollment.Bolbolestan.model.Exceptions.ExamTimeCollisionError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Offering {
    private final String code;
    private final String classCode;
    private final String name;
    private final String instructor;
    private final String type;
    private final Integer units;
    private final ClassTime classTime;
    private final ExamTime examTime;
    private Integer capacity;
    private final ArrayList<String> prerequisites = new ArrayList<>();
    private JsonNode jsonObject;
    private Integer registeredStudents;
    private ArrayList<String> waitingList;
    ObjectMapper mapper = new ObjectMapper();

    public Offering(JsonNode course) {
        this.code = course.get("code").asText();
        this.classCode = course.get("classCode").asText();
        this.name = course.get("name").asText();
        this.instructor = course.get("instructor").asText();
        this.type = course.get("type").asText();
        this.units = course.get("units").asInt();
        this.classTime = new ClassTime(course.get("classTime"));
        this.examTime = new ExamTime(course.get("examTime"));
        this.capacity = course.get("capacity").asInt();
        this.waitingList = new ArrayList<>();

        ArrayNode arrayNode = (ArrayNode) course.get("prerequisites");
        for(JsonNode jsonNode : arrayNode)
            this.prerequisites.add(jsonNode.asText());

        this.jsonObject = course;
        registeredStudents = 0;
    }

    public void addPrerequisites(List<Prerequisite> pre) {
        for (Prerequisite p:pre) {
            this.prerequisites.add(p.getPrerequisiteName());
        }
    }

    public Offering(String code, String classCode, String name, Integer units, String type, String instructor, Integer capacity, String classDays, String classTime, String examStart, String examEnd, Integer totalRegisteredStudents) {
        this.code = code;
        this.classCode = classCode;
        this.name = name;
        this.instructor = instructor;
        this.type = type;
        this.units = units;
        String[] splitString = classDays.split("\\|");
        ObjectNode ct = mapper.createObjectNode();
        ArrayNode days = mapper.createArrayNode();
        for (String s : splitString) {
            days.add(s);
        }
        ct.set("days", days);
        ct.put("time", classTime);
        this.classTime = new ClassTime(ct);
        ObjectNode et = mapper.createObjectNode();
        et.put("start", examStart);
        et.put("end", examEnd);
        this.examTime = new ExamTime(et);
        this.capacity = capacity;
        this.waitingList = new ArrayList<>();

//        ArrayNode arrayNode = (ArrayNode) course.get("prerequisites");
//        for(JsonNode jsonNode : arrayNode)
//            this.prerequisites.add(jsonNode.asText());

//        this.jsonObject = course;
        this.registeredStudents = totalRegisteredStudents;
    }

    public String getCode() {
        return code;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getName() {
        return name;
    }

    public String getInstructor() {
        return instructor;
    }

    public Integer getUnits() {
        return units;
    }

    public ClassTime getClassTime() {
        return classTime;
    }

    public ExamTime getExamTime() {
        return examTime;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public ArrayList<String> getWaitingList() { return waitingList; }

    public void addToWaitingList(String sid) {
        waitingList.add(sid);
    }

    public void removeFromWaitingList(String sid) {
        for (int i =0; i < waitingList.size(); i++) {
            if (waitingList.get(i).equals(sid)) {
                waitingList.remove(i);
                break;
            }
        }
    }

    public String getType() { return type; }

    public String getDays() {
        ArrayList<String> days = new ArrayList<>(classTime.getDays());
        return String.join("|", days);
    }

    public String getTime() {
        return classTime.getTime();
    }

    public String getExamStartTime() {
        return jsonObject.get("examTime").get("start").asText();
    }

    public String getExamEndTime() {
        return jsonObject.get("examTime").get("end").asText();
    }

    public String getPrerequisitesConcat() {
        return String.join("|", prerequisites);
    }

    public Integer getRegisteredStudents() {
        return registeredStudents;
    }

    public void setRegisteredStudents(Integer registeredStudents) {
        this.registeredStudents = registeredStudents;
    }

    public ArrayList<String> getPrerequisites() { return prerequisites; }

    public JsonNode getJsonObject() {
        return jsonObject;
    }


    public void resetWaitingList() {
        this.capacity += this.waitingList.size();
        this.registeredStudents += this.waitingList.size();
        this.waitingList = new ArrayList<>();
    }

    public JsonNode getOffering() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("code", this.code);
        jsonNode.put("classCode", this.classCode);
        jsonNode.put("name", this.name);
        jsonNode.put("instructor", this.instructor);
        jsonNode.put("units", this.units);
        if (this.type.equals("Asli"))
            jsonNode.put("type", "اصلی");
        if (this.type.equals("Takhasosi"))
            jsonNode.put("type", "تخصصی");
        if (this.type.equals("Paaye"))
            jsonNode.put("type", "پایه");
        if (this.type.equals("Umumi"))
            jsonNode.put("type", "عمومی");
        jsonNode.set("classTime", this.classTime.getClassTime());
        jsonNode.set("examTime", this.examTime.getExamTime());
        jsonNode.put("capacity", this.capacity);
        ArrayNode pre = mapper.createArrayNode();
        for (String p : this.prerequisites) {
            pre.add(p);
        }
        jsonNode.set("prerequisites", pre);
        jsonNode.put("registeredStudents", this.registeredStudents);
        return jsonNode;
    }

    public ArrayList<Exception> checkTimeConflicts(List<Offering> schedule) {
        ArrayList<Exception> exceptions = new ArrayList<>();
        for (Offering course : schedule) {
            Set<String> days1 = course.getClassTime().getDays();
            Set<String> days2 = getClassTime().getDays();
            Set<String> intersection = new HashSet<>(days1);
            intersection.retainAll(days2);

            if (intersection.size() == 0)
                continue;

            if (course.getClassTime().hasTimeConflict(getClassTime()))
                exceptions.add(new ClassTimeCollisionError(course.getCode(), getCode()));
        }
        return exceptions;
    }

    public ArrayList<Exception> checkExamTimeConflicts(List<Offering> schedule) {
        ArrayList<Exception> exceptions = new ArrayList<>();
        for (Offering scheduleItem : schedule) {
            if (scheduleItem.getExamTime().hasTimeConflict(getExamTime()))
                exceptions.add(new ExamTimeCollisionError(scheduleItem.getCode(), getCode()));
        }
        return exceptions;
    }

    @Override
    public String toString() {
        return "{" +
                "\"code\": \"" + this.code
                + "\"classCode\": \"" + this.classCode
                + "\",\"name\": \"" + this.name
                + "\",\"instructor\": \"" + this.type + "\""
                + "\",\"type\": \"" + this.type + "\""
                + "\",\"units\": " + this.units
                + "\",\"classTime\": " + this.classTime
                + "\",\"examTime\": " + this.examTime
                + "\",\"capacity\": " + this.capacity
                + "\",\"prerequisites\": " + this.prerequisites
                + "}";
    }
}
