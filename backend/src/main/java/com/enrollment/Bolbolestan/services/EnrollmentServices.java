package com.enrollment.Bolbolestan.services;

import com.enrollment.Bolbolestan.model.Exceptions.OfferingNotFound;
import com.enrollment.Bolbolestan.model.ScheduleItem;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import com.enrollment.Bolbolestan.model.EnrollmentSystem;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Objects;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class }, scanBasePackages = {"com.enrollment.Bolbolestan.filter"})
@CrossOrigin(origins = "http://87.247.185.122:31000")
@RestController
@EnableScheduling
public class EnrollmentServices {
    public final int WAITING_TIME = 3;

    EnrollmentSystem system = EnrollmentSystem.getInstance();

    ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(EnrollmentServices.class, args);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonNode loginService(@RequestParam(value = "email") String username, @RequestParam(value = "password") String password, HttpServletResponse response) {
        try {
            JsonNode result = system.login(username, password);
            if (!result.has("token")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            return result;
        } catch(Exception e) {
            return null;
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object logoutService() {
        system.logout();
        return system.getSubmissionErrors();
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonNode registerService(@RequestBody JsonNode profile) {
        System.out.println(profile);
        try {
            return system.register(profile);
        } catch(Exception e) {
            return null;
        }
    }


    @RequestMapping(value = "students/me/reports", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object reportService(@RequestAttribute("id") String id) {
        try {
            return system.getReport(id);
        } catch(SQLException e) {
            return this.getSubmissionError(e);
        }

    }

    public JsonNode getSubmissionError(Exception e) {
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("message", e.toString());
        return mapper.createObjectNode().set("errors", jsonNode);
    }


    @RequestMapping(value = "students/me/schedule/courses/{course_code}/{class_code}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object addToScheduleService(
            @RequestAttribute("id") String id,
            @PathVariable(value = "course_code") String code,
            @PathVariable(value = "class_code") String classCode)
    {
        try {
            ScheduleItem res = system.addCourseToWeeklySchedule(id, code, classCode);
            if (res == null)
                return system.getSubmissionErrors();
            return res.getScheduleItem();
        } catch(OfferingNotFound e) {
            return this.getSubmissionError(e);
        } catch(SQLException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @RequestMapping(value = "students/me/schedule/courses/{course_code}/{class_code}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object removeFromScheduleService(
            @RequestAttribute("id") String id,
            @PathVariable(value = "course_code") String code,
            @PathVariable(value = "class_code") String classCode)
    {
//        Offering course = Database.offerings.get(code + classCode);
//        if (course == null)
//            return new ResponseEntity(HttpStatus.NOT_FOUND);
        try {
            ScheduleItem res = system.removeCourseFromWeeklySchedule(id, code, classCode);
            if (res == null)
                return system.getSubmissionErrors();
            return res.getScheduleItem();
        } catch(OfferingNotFound | SQLException e) {
            return this.getSubmissionError(e);
        }
    }

    @RequestMapping(value = "/courses/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ArrayNode searchCourse(@RequestParam(required = false, name = "keyword", defaultValue = "") String key) {
        try {
            return system.searchCourseByName(key);
        } catch (SQLException e) {
            return mapper.createArrayNode();
        }
    }

    @RequestMapping(value = "/courses/clear_search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ArrayNode clearSearchCourse() {
        try {
            return system.searchCourseByName("");
        } catch (SQLException e) {
            return mapper.createArrayNode();
        }
    }

    @RequestMapping(value = "students/me/schedule/courses/reset", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object resetScheduleService(@RequestAttribute("id") String id) {
        try {
            return system.resetWeeklySchedule(id);
        } catch (SQLException e) {
            return this.getSubmissionError(e);
        }
    }


    @RequestMapping(value = "/courses", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonNode getFilteredCoursesService(@RequestParam(name = "type", defaultValue = "") String type) {
        try {
            return system.searchCourseByType(Objects.requireNonNullElse(type, ""));
        } catch(SQLException e) {
            return this.getSubmissionError(e);
        }
    }

    @RequestMapping(value = "students/me/plan", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getPlanService(@RequestAttribute("id") String id) {
        EnrollmentSystem system = EnrollmentSystem.getInstance();
        try {
            return system.getStudentPlan(id);
        } catch (SQLException e) {
            return getSubmissionError(e);
        }
    }

    @RequestMapping(value = "students/me/profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getProfileService(@RequestAttribute("id") String id) {
        try {
            return system.getLoginProfile(id);
        }
        catch(SQLException e) {
            return getSubmissionError(e);
        }
    }

    @RequestMapping(value = "students/me/schedule", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getStudentCoursesService(@RequestAttribute("id") String id) {
        try {
            return system.getStudentCourses(id);
        } catch (SQLException e) {
            return this.getSubmissionError(e);
        }
    }

    @RequestMapping(value = "students/me/totalPassedUnits", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getTotalUnitsService(@RequestAttribute("id") String id) {
        try {
            return system.getTotalSelectedUnits(id);
        } catch (SQLException e) {
            return this.getSubmissionError(e);
        }
    }

    @RequestMapping(value = "students/me/schedule/courses/finalize", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object finalizeService(@RequestAttribute("id") String id) {
        try {
            return system.finalizeWeeklySchedule(id);
        } catch(SQLException e) {
            return this.getSubmissionError(e);
        }
    }

    @RequestMapping(value = "students/me/term", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object getTermService(@RequestAttribute("id") String id) {
        try {
            return system.getSemester(id);
        } catch(SQLException e) {
            return this.getSubmissionError(e);
        }
    }

    @RequestMapping(value = "/restorePassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonNode restorePasswordService(@RequestParam(value = "email") String email) {
        try {
            return system.restorePassword(email);
        }
        catch (Exception e) {
            return null;
        }
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonNode changePasswordService(@RequestBody JsonNode token) {
        try {
            return system.changePassword(token.get("token").asText(), token.get("password").asText());
        }
        catch (Exception e) {
            return null;
        }
    }

    @Scheduled(fixedRate = WAITING_TIME * 60000)
    public void addToWaitingList() {
        system.emptyAllWaitingLists();
    }
}
