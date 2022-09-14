package com.enrollment.Bolbolestan.model;

import com.enrollment.Bolbolestan.model.Exceptions.*;
import com.enrollment.Bolbolestan.repository.*;
import com.enrollment.Bolbolestan.utilities.JWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;

public class EnrollmentSystem {
    private ArrayList<Exception> submissionErrors;
    private String searchKey;
    static private Integer hashPasswordStrength = 10;
    ObjectMapper mapper;
    static private final String defaultHost = "http://138.197.181.131:5200";
    private static EnrollmentSystem instance = null;

    private static StudentRepository studentRepository = null;

    private static CourseRepository courseRepository = null;

    private static EnrolledRepository enrolledRepository = null;

    private static PrerequisiteRepository prerequisiteRepository = null;

    private static GradeRepository gradeRepository = null;

    private EnrollmentSystem() {
        searchKey = "";
        submissionErrors = new ArrayList<>();
        mapper = new ObjectMapper();
    }

    private static void getStudentInformation() throws Exception {
        if (studentRepository.checkIfExists())
            return;
        HttpURLConnection connection = (HttpURLConnection) new URL(defaultHost + "/api/students").openConnection();
        Scanner in = new Scanner((InputStream) connection.getContent());
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonNodes = (ArrayNode) mapper.readTree(in.nextLine());
        for (JsonNode student : jsonNodes) {
            getJsonNode(student);
        }
    }

    private static void getCoursesInformation() throws Exception {
        if (courseRepository.checkIfExists())
            return;
        HttpURLConnection connection = (HttpURLConnection) new URL(defaultHost + "/api/courses").openConnection();
        Scanner in = new Scanner((InputStream) connection.getContent());
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonNodes = (ArrayNode) mapper.readTree(in.nextLine());
        for (JsonNode course : jsonNodes) {
            courseRepository.insert(new Offering(course));

        }
        if (prerequisiteRepository.checkIfExists())
            return;
        System.out.println("before adding prerequisites");
        for (JsonNode course : jsonNodes) {
            ArrayNode arrayNode = (ArrayNode) course.get("prerequisites");
            for(JsonNode jsonNode : arrayNode) {
                Offering offering = courseRepository.findByCourseCode(jsonNode.asText());
                prerequisiteRepository.insert(new Prerequisite(course.get("code").asText(), offering.getCode(), offering.getName()));
            }
        }
    }

    private static void getGradesInformation() throws Exception {
        if (gradeRepository.checkIfExists())
            return;
        List<Student> students = studentRepository.findAll();
        for (Student s:students) {
            getStudentGrades(s.getId());
        }
    }

    private static void getStudentGrades(String studentId) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(defaultHost + "/api/grades/" + studentId).openConnection();
        Scanner in = new Scanner((InputStream) connection.getContent());
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jsonNodes = (ArrayNode) mapper.readTree(in.nextLine());
        for (JsonNode grade : jsonNodes) {
            Offering offering = courseRepository.findByCourseCode(grade.get("code").asText());
            gradeRepository.insert(
                    new Grade(
                            studentId,
                            grade.get("code").asText(),
                            offering.getName(),
                            offering.getUnits(),
                            grade.get("grade").asInt(),
                            grade.get("term").asInt()
                    )
            );

        }
    }

    public static EnrollmentSystem getInstance() {
        if (instance == null) {
            instance = new EnrollmentSystem();
            studentRepository = StudentRepository.getInstance();
            courseRepository = CourseRepository.getInstance();
            enrolledRepository = EnrolledRepository.getInstance();
            prerequisiteRepository = PrerequisiteRepository.getInstance();
            gradeRepository = GradeRepository.getInstance();
            try {
                getStudentInformation();
                getCoursesInformation();
                getGradesInformation();
            } catch(Exception e) {
                System.out.println(e);
            }
        }
        return instance;
    }

    public JsonNode login(String email, String password) throws SQLException {
        ObjectNode jsonNode = mapper.createObjectNode();
        Student std = studentRepository.findByEmail(email);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if (std != null && bCryptPasswordEncoder.matches(password, std.getPassword())) {
            String token = "";
            try {
                token = JWT.createToken(std.getId(), 1440);
            } catch(UnsupportedEncodingException e) {
                System.out.println(e);
            }
            jsonNode.put("status", 200);
            jsonNode.put("token", token);

        } else {
            jsonNode.put("status", 403);
            jsonNode.put("message", "نام کاربری یا رمز عبور صحیح نمی باشد.");
        }
        return jsonNode;
    }

    public JsonNode register(JsonNode student) throws SQLException, SameStudentIdFound {
        String id = student.get("id").asText();
        String email = student.get("email").asText();
        ObjectNode jsonNode = mapper.createObjectNode();
        Student sameIdStudent = studentRepository.findById(id);
        Student sameEmailStudent = studentRepository.findByEmail(email);
        if (sameIdStudent != null) {
            jsonNode.put("status", 403);
            jsonNode.put("message", new SameStudentIdFound().toString());
            return jsonNode;
        } else if (sameEmailStudent != null) {
            jsonNode.put("status", 403);
            jsonNode.put("message", new SameEmailFound().toString());
            return jsonNode;
        }
        getJsonNode(student);
        String token = "";
        try {
            token = JWT.createToken(id, 1440);
        } catch(UnsupportedEncodingException e) {
            System.out.println(e);
        }
        jsonNode.put("status", 200);
        jsonNode.put("token", token);
        return jsonNode;
    }

    private static String getHash(String in) {
        BCryptPasswordEncoder bCryptPasswordEncoder =
                new BCryptPasswordEncoder(hashPasswordStrength, new SecureRandom());
        return bCryptPasswordEncoder.encode(in);
    }

    private static void getJsonNode(JsonNode student) throws SQLException {
        studentRepository.insert(
                new Student(
                        student.get("id").asText(),
                        student.get("name").asText(),
                        student.get("secondName").asText(),
                        student.get("birthDate").asText(),
                        student.get("field").asText(),
                        student.get("faculty").asText(),
                        student.get("level").asText(),
                        student.get("status").asText(),
                        student.get("img").asText(),
                        student.get("email").asText(),
                        getHash(student.get("password").asText())
                )
        );
    }

    public JsonNode getSemester(String id) throws SQLException {
        Integer semester = gradeRepository.findCurrentSemester(id);
        return mapper.createObjectNode().put("term", semester);
    }

    public JsonNode getReport(String id) throws SQLException {
        HashMap<String, ArrayNode> reports = new HashMap<>();
        Integer semester = gradeRepository.findCurrentSemester(id);
        for (Integer i = 1; i < semester; i++) {
            List<Grade> grades = gradeRepository.findAllGradesByTerm(id, i);
            ArrayNode thisGrade = mapper.createArrayNode();
            reports.put(i.toString(), thisGrade);
            for (Grade g:grades) {
                reports.get(i.toString()).add(g.getGrade());
            }
        }
        return mapper.createObjectNode().setAll(reports);
    }

    public void logout() {
        submissionErrors = new ArrayList<>();
    }

    public JsonNode getSubmissionErrors() {
        ObjectNode jsonNode;
        ArrayNode array = mapper.createArrayNode();
        for(Exception e: submissionErrors) {
            jsonNode = mapper.createObjectNode();
            jsonNode.put("message", e.toString());
            array.add(jsonNode);
        }
        return mapper.createObjectNode().set("errors", array);
    }

    public void setSubmissionErrors(ArrayList<Exception> exceptions) { submissionErrors = exceptions; }

    public void setSearchKey(String key) { searchKey = key; }

    public String getSearchKey() { return searchKey; }

    public ScheduleItem addCourseToWeeklySchedule(String id, String courseCode, String classCode) throws SQLException, OfferingNotFound {
        submissionErrors = new ArrayList<>();
        Offering courseToBeAdded = courseRepository.findByCode(courseCode, classCode);
        if (courseToBeAdded == null) {
            throw new OfferingNotFound();
        }
        List<Enrolled> enrolled = enrolledRepository.findByType(id);
        List<Offering> schedule = new ArrayList<>();
        for (Enrolled e: enrolled) {
            schedule.add(courseRepository.findByCode(e.getCourseCode(), e.getClassCode()));
        }
        submissionErrors.addAll(courseToBeAdded.checkTimeConflicts(schedule));
        submissionErrors.addAll(courseToBeAdded.checkExamTimeConflicts(schedule));
        if (submissionErrors.size() != 0)
            return null;
        enrolledRepository.insert(
                new Enrolled(
                        id,
                        courseCode,
                        classCode,
                        courseToBeAdded.getName(),
                        courseToBeAdded.getInstructor(),
                        courseToBeAdded.getUnits()
                )
        );
        return new ScheduleItem(courseToBeAdded);
    }

    public ScheduleItem removeCourseFromWeeklySchedule(String id, String courseCode, String classCode) throws SQLException, OfferingNotFound {
        submissionErrors = new ArrayList<>();
        Offering course = courseRepository.findByCode(courseCode, classCode);
        if (course == null)
            throw new OfferingNotFound();

        Enrolled enrolled = enrolledRepository.findByStudentAndCode(id, courseCode, classCode);

        if (enrolled == null)
            throw new OfferingNotFound(courseCode, classCode);

        enrolledRepository.deleteEnrollment(id, courseCode, classCode);
        return new ScheduleItem(course);
    }


    public ArrayList<Exception> checkUnitsRange(List<Enrolled> schedule) {
        ArrayList<Exception> exceptions = new ArrayList<>();
        int sumOfUnits = 0;
        for (Enrolled item: schedule) {
            if (!(item.getStatus().equals("deleted"))) {
                sumOfUnits += item.getUnits();
            }
        }
        if (sumOfUnits > 20)
            exceptions.add(new MaximumUnitsError());
        if (sumOfUnits < 12)
            exceptions.add(new MinimumUnitsError());
        return exceptions;
    }

    public ArrayList<Exception> checkPrerequisites(List<Enrolled> schedule, String id) throws SQLException {
        ArrayList<Exception> exceptions = new ArrayList<>();
        for (Enrolled item: schedule) {
            List<String> notMet = prerequisiteRepository.hasPassedCourse(id, item.getCourseCode());
            for (String n:notMet) {
                exceptions.add(new PrerequisiteNotMet(item.getCourseCode(), n));
            }
        }
        return exceptions;
    }

    public ArrayList<Exception> checkExistingPassedCourses(List<Enrolled> schedule, String id) throws SQLException {
        ArrayList<Exception> exceptions = new ArrayList<>();
        for (Enrolled scheduleItem : schedule) {
            if (gradeRepository.hasPassedCourse(id, scheduleItem.getCourseCode()))
                exceptions.add(new ExistingPassedCourseError(scheduleItem.getCourseCode()));
        }
        return exceptions;
    }

    private ArrayList<Exception> checkCoursesCapacity(List<Enrolled> schedule) throws SQLException {
        ArrayList<Exception> exceptions = new ArrayList<>();
        int totalFinalizedUnits = 0;

        for (Enrolled scheduleItem : schedule) {
            if (scheduleItem.getStatus().equals("finalized"))
                totalFinalizedUnits += scheduleItem.getUnits();
            if (scheduleItem.getStatus().equals("non-finalized")) {
                Integer remaining = courseRepository.findRemainingCapacity(scheduleItem.getCourseCode(), scheduleItem.getClassCode());
                if (remaining > 0)
                    totalFinalizedUnits += scheduleItem.getUnits();
            }
        }
//        if (checkUnitsRange(schedule).size() != 0) {
//            return exceptions;
//        }
//        if (totalFinalizedUnits < 12) {
//            exceptions.add(new MinimumUnitsError());
//            return exceptions;
//        } else if (totalFinalizedUnits > 20) {
//            exceptions.add(new MaximumUnitsError());
//            return exceptions;
//        }

        for (Enrolled scheduleItem : schedule) {
            Integer remaining = courseRepository.findRemainingCapacity(scheduleItem.getCourseCode(), scheduleItem.getClassCode());
            if (remaining <= 0)
                if (scheduleItem.getStatus().equals("non-finalized"))
                    scheduleItem.setStatus("waiting");
        }
        return exceptions;
    }

    public ArrayList<Exception> finalizeSchedule(String id) throws SQLException {
        ArrayList<Exception> exceptions = new ArrayList<>();
        List<Enrolled> enrolled;
        enrolled = enrolledRepository.findByType(id);
        exceptions.addAll(checkUnitsRange(enrolled));
        exceptions.addAll(checkPrerequisites(enrolled, id));
        exceptions.addAll(checkExistingPassedCourses(enrolled, id));
        exceptions.addAll(checkCoursesCapacity(enrolled));
        if (exceptions.size() != 0)
            return exceptions;

//        exceptions.addAll(checkUnitsRange(scheduleItems));
//        if (exceptions.size() != 0)
//            return exceptions;
        for (Enrolled scheduleItem : enrolled) {
            switch (scheduleItem.getStatus()) {
                case "deleted" -> {
                    courseRepository.decrementRegisteredStudents(scheduleItem.getCourseCode(), scheduleItem.getClassCode());
                    enrolledRepository.deleteFinalizedEnrollment(id, scheduleItem.getCourseCode(), scheduleItem.getClassCode());
                }
                case "non-finalized" -> {
                    courseRepository.incrementRegisteredStudents(scheduleItem.getCourseCode(), scheduleItem.getClassCode());
                    enrolledRepository.registerEnrollment(id, scheduleItem.getCourseCode(), scheduleItem.getClassCode(), "non-finalized", "finalized");
                }
                case "waiting" -> enrolledRepository.registerEnrollment(id, scheduleItem.getCourseCode(), scheduleItem.getClassCode(), "non-finalized", "waiting");
            }
        }

        return exceptions;
    }

    public JsonNode finalizeWeeklySchedule(String id) throws SQLException {
        submissionErrors = new ArrayList<>();
        ObjectNode jsonNode = mapper.createObjectNode();
        List<Enrolled> enrolled;
        try {
            submissionErrors = finalizeSchedule(id);
        } catch (ConcurrentModificationException ignored) {}


        ObjectNode objectNode;
        ArrayNode array = mapper.createArrayNode();
        for(Exception e: submissionErrors) {
            objectNode = mapper.createObjectNode();
            objectNode.put("message", e.toString());
            array.add(objectNode);
        }
        jsonNode.set("errors", array);
        ArrayNode courses = mapper.createArrayNode();
        if (submissionErrors.size() == 0) {
            enrolled = enrolledRepository.findByType(id);
            for (Enrolled scheduleItem : enrolled) {
                courses.add(scheduleItem.getEnrolled());
            }
        }
        jsonNode.set("courses", courses);
        return jsonNode;
    }

    public JsonNode getTotalSelectedUnits(String id) throws SQLException{
        int totalUnits = enrolledRepository.getTotalSelectedUnits(id);
        return mapper.createObjectNode().put("totalUnits", totalUnits);
    }

    public ArrayNode getStudentCourses(String id) throws SQLException {
        ArrayNode courses = mapper.createArrayNode();
        List<Enrolled> enrolled = enrolledRepository.findByType(id);
        for (Enrolled e : enrolled) {
            courses.add(e.getEnrolled());
        }
        return courses;
    }

    public ArrayNode searchCourseByName(String key) throws SQLException {
        ArrayNode array = mapper.createArrayNode();
        List<Offering> courses = courseRepository.findByCourseName("%" + key + "%");
        if (courses == null)
            return array;
        for (Offering o: courses)
            array.add(o.getOffering());
        return array;
    }

    public ArrayNode searchCourseByType(String type) throws SQLException {
        ArrayNode array = mapper.createArrayNode();
        List<Offering> courses;
        if (type.equals("")) {
            courses = courseRepository.findAll();
        } else {
            courses = courseRepository.findByType(type);
        }

        for (Offering o: courses) {
            o.addPrerequisites(prerequisiteRepository.findByType(o.getCode()));
            array.add(o.getOffering());
        }

        return array;
    }

    public ArrayNode getStudentPlan(String id) throws SQLException {
        return this.getWeeklyTable(enrolledRepository.findByType(id));
    }

    public ArrayNode resetWeeklySchedule(String id) throws SQLException {
        submissionErrors = new ArrayList<>();
        enrolledRepository.resetEnrollment(id);
        return this.getStudentCourses(id);
    }

    public JsonNode getLoginProfile(String id) throws SQLException {
        submissionErrors = new ArrayList<>();
        Student loggedInUser = studentRepository.findById(id);
        loggedInUser.setTotalPassedUnits(gradeRepository.findTotalPassedUnits(id));
        loggedInUser.setGPA(gradeRepository.findGPA(id));
        return loggedInUser.getProfile();
    }

    public ArrayNode getWeeklyTable(List<Enrolled> schedule) throws SQLException {
        ArrayNode courses = mapper.createArrayNode();
        for (Enrolled scheduleItem : schedule) {
            if (!scheduleItem.getStatus().equals("finalized"))
                continue;
            ObjectNode jsonNode = mapper.createObjectNode();
            jsonNode.put("name", scheduleItem.getCourseName());
            Offering offering = courseRepository.findByCode(scheduleItem.getCourseCode(), scheduleItem.getClassCode());
            jsonNode.put("start", offering.getClassTime().getStart());
            jsonNode.put("end", offering.getClassTime().getEnd());
            switch (offering.getType()) {
                case "Asli" -> jsonNode.put("type", "اصلی");
                case "Takhasosi" -> jsonNode.put("type", "تخصصی");
                case "Paaye" -> jsonNode.put("type", "پایه");
                case "Umumi" -> jsonNode.put("type", "عمومی");
            }
            ArrayNode days = mapper.createArrayNode();
            for (String day : offering.getClassTime().getDays()) {
                days.add(day);
            }
            jsonNode.set("days", days);
            jsonNode.put("length", offering.getClassTime().getLength());
            courses.add(jsonNode);
        }
        return courses;
    }

    public void emptyAllWaitingLists() {
        try {
             courseRepository.emptyWaitingList();
             enrolledRepository.updateWaitingEnrollment();

        } catch(SQLException e) {
            System.out.println(e);
        }
    }

    public JsonNode restorePassword(String email) throws SQLException, IOException {
        System.out.println(email);
        Student std = studentRepository.findByEmail(email);
        ObjectNode jsonNode = mapper.createObjectNode();
        if (std != null) {
            String path = "";
            try {
                path = JWT.createToken(email, 10);
            }
            catch (Exception e) {
                System.out.println(e);
            }
            System.out.println(path);
            path = "http://87.247.185.122:31000/changePassword/" + path;
            String json = "{\n"
                    + "\"email\":" + "\"" + email + "\",\n"
                    + "\"url\":" + "\"" + path + "\"\n"
                    + "}";
            System.out.println(json);
            URL url = new URL ("http://138.197.181.131:5200/api/send_mail");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }

            jsonNode.put("response", "Recovery address sent to your email");
        } else {
            jsonNode.put("response", "Email does not exist");
        }
        return jsonNode;
    }

    public JsonNode changePassword(String token, String newPassword) throws Exception {
        String email = JWT.decodeToken(token).get("userId").asText();
        ObjectNode jsonNode = mapper.createObjectNode();
        try {
            studentRepository.changePassword(email, getHash(newPassword));
            jsonNode.put("response", "رمز عبور با موفقیت تغییر یافت");
            jsonNode.put("status", 200);
        } catch (SQLException e) {
            e.printStackTrace();
            jsonNode.put("response", "خطا");
            jsonNode.put("status", 403);
        }
        return jsonNode;
    }

}