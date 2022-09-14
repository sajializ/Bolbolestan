package com.enrollment.Bolbolestan.model;

import com.enrollment.Bolbolestan.model.Grade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Database {
    static public HashMap<String, Student> students = new HashMap<>();
    static public HashMap<String, Offering> offerings = new HashMap<>();
    static public HashMap<String, Offering> filteredOfferings = new HashMap<>();
    static private final String defaultHost = "http://138.197.181.131:5100";

}
