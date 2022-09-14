package com.enrollment.Bolbolestan.model.Exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ClassTimeCollisionError extends Exception {
    private String code1;
    private String code2;

    public ClassTimeCollisionError(String _code1, String _code2) {
        code1 = _code1;
        code2 = _code2;
    }

    public JsonNode getJsonError() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("message", this.toString());
        return jsonNode;
    }
    @Override
    public String toString() {
        return "درس با کد " + code2 + " با درس با کد " + code1 + " تداخل دارد";
    }
}
