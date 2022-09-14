package com.enrollment.Bolbolestan.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class ExamTime {
    private Date begin;
    private Date end;
    private final JsonNode jsonObject;

    public ExamTime(JsonNode examTime) {
        this.jsonObject = examTime;
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        try {
            this.begin = dataFormat.parse(examTime.get("start").asText());
            this.end = dataFormat.parse(examTime.get("end").asText());
        }
        catch (Exception ignored) {}
    }

    public boolean hasTimeConflict(ExamTime other) {
        return other.begin.compareTo(this.end) < 0 && other.end.compareTo(this.begin) > 0;
    }

    public String getExamStart() { return begin.toString(); }

    public String getExamEnd() { return end.toString(); }

    public JsonNode getExamTime() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        LocalDateTime dateTime1 = LocalDateTime.parse(jsonObject.get("start").asText());
        LocalDateTime dateTime2 = LocalDateTime.parse(jsonObject.get("end").asText());
        jsonNode.put("start",
                        dateTime1.getMonth().getValue() + "/" + dateTime1.getDayOfMonth()
                        + " - "
                        + dateTime2.getHour() + ":" + dateTime2.getMinute()
                        + " - "
                        + dateTime1.getHour() + ":" + dateTime1.getMinute());
        jsonNode.put("days", String.valueOf(this.end));
        return jsonNode;
    }

    public String getHtmlTable() {
        return "<td>"
                + jsonObject.get("start").asText()
                + "</td><td>"
                + jsonObject.get("end").asText()
                + "</td>";
    }


    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
