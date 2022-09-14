package com.enrollment.Bolbolestan.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.SimpleDateFormat;
import java.util.*;

public class ClassTime {
    private final Set<String> days;
    private Date startTime;
    private Date endTime;
    private final JsonNode jsonObject;
    private final String start;
    private final String end;


    public ClassTime(JsonNode time) {
        this.jsonObject = time;
        ArrayNode days = (ArrayNode) time.get("days");
        this.days = new HashSet<>();
        for (JsonNode day : days)
            this.days.add(day.asText());

        SimpleDateFormat onlyHour = new SimpleDateFormat("HH");
        SimpleDateFormat withMinute = new SimpleDateFormat("HH:mm");
        String[] classTime = time.get("time").asText().split("-");

        start = classTime[0];
        end = classTime[1];

        try {
            this.startTime = withMinute.parse(classTime[0]);
        }
        catch (Exception e) {
            try {
                this.startTime = onlyHour.parse(classTime[0]);
            }
            catch (Exception ignored) {}
        }
        try {
            this.endTime = withMinute.parse(classTime[1]);
        }
        catch (Exception e) {
            try {
                this.endTime = onlyHour.parse(classTime[1]);
            }
            catch (Exception ignored) {}
        }
    }

    public boolean hasTimeConflict(ClassTime other) {
        return other.startTime.compareTo(this.endTime) < 0 && other.endTime.compareTo(this.startTime) > 0;
    }

    public Set<String> getDays() {
        return days;
    }

    public String getTime() {
        return this.start
                + " - "
                + this.end;
    }

    public String getHtmlTable() {
        return "<td>"
                + String.join("|", this.days)
                + "</td><td>"
                + this.start
                + "-"
                + this.end
                + "</td>";
    }

    public JsonNode getClassTime() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("time", this.getTime());
        ArrayList<String> days = new ArrayList<>();
        for (String day : this.days) {
            if (day.equals("Saturday"))
                days.add("شنبه");
            if (day.equals("Sunday"))
                days.add("یکشنبه");
            if (day.equals("Monday"))
                days.add("دوشنبه");
            if (day.equals("Tuesday"))
                days.add("سه شنبه");
            if (day.equals("Wednesday"))
                days.add("چهارشنبه");
            if (day.equals("Thursday"))
                days.add("پنجشنبه");
            if (day.equals("Friday"))
                days.add("جمعه");
        }
        jsonNode.put("days", String.join(" - ", days));
        return jsonNode;
    }

    public long getLength() {
        return ((endTime.getTime() - startTime.getTime()) / 1000) / 60;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
