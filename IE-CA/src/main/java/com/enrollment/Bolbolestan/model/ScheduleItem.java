package com.enrollment.Bolbolestan.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ScheduleItem {
    private String status;
    private Offering offering;
    public boolean hasError = false;
//    public enum Status {
//        FINALIZED, NOT_FINALIZED;
//
//        @Override
//        public String toString() {
//            String returnValue = "";
//            switch (this) {
//                case FINALIZED: returnValue = "finalized";
//                case NOT_FINALIZED: returnValue = "non-finalized";
//            }
//            return returnValue;
//        }
//    }

    public ScheduleItem(Offering offering) {
        this.status = "non-finalized";
        this.offering = offering;
    }

    public JsonNode getScheduleItem() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("code", this.getCode());
        jsonNode.put("classCode", this.getClassCode());
        jsonNode.put("name", this.getName());
        jsonNode.put("instructor", this.offering.getInstructor());
        jsonNode.put("units", this.getUnits());
        if (this.status.equals("finalized"))
            jsonNode.put("status", "ثبت شده");
        if (this.status.equals("non-finalized"))
            jsonNode.put("status", "ثبت نهایی نشده");
        if (this.status.equals("waiting"))
            jsonNode.put("status", "در انتظار");
        return jsonNode;
    }

    public String getCode() {
        return offering.getCode();
    }

    public String getClassCode() {
        return offering.getClassCode();
    }

    public Integer getUnits() {
        return offering.getUnits();
    }

    public ClassTime getClassTime() {
        return offering.getClassTime();
    }

    public ExamTime getExamTime() {
        return offering.getExamTime();
    }

    public Integer getCapacity() {
        return offering.getCapacity();
    }

    public Integer getRegisteredStudents() {
        return offering.getRegisteredStudents();
    }

    public Offering getOffering() {
        return offering;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return offering.getName();
    }

    public void setStatus(String state) { status = state; }

    public void register() {
        if (this.status == "non-finalized")
            offering.setRegisteredStudents(offering.getRegisteredStudents() + 1);
        this.status = "finalized";
    }

    public void unregister() {
        offering.setRegisteredStudents(offering.getRegisteredStudents() - 1);
    }

    public String getHtmlTable() {
        return "<td>" + this.offering.getCode() + "</td>"
                + "<td>" + this.offering.getClassCode() + "</td>"
                + "<td>" + this.offering.getName() + "</td>"
                + "<td>" + this.offering.getUnits() + "</td>";
    }

    @Override
    public String toString() {
        return "{" +
                "\"code\":\"" + this.offering.getCode()
                + "\",\"classCode\":\"" + this.offering.getClassCode()
                + "\",\"name\":\"" + this.offering.getName()
                + "\",\"Instructor\":\"" + this.offering.getInstructor()
                + "\",\"classTime\":" + this.offering.getClassTime().toString()
                + ",\"examTime\":" + this.offering.getExamTime().toString()
                + ",\"status\":\"" + this.status + "\"}"
                ;
    }
}
