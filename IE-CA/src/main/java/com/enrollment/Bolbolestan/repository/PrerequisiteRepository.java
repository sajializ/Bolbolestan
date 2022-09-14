package com.enrollment.Bolbolestan.repository;

import com.enrollment.Bolbolestan.model.Prerequisite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PrerequisiteRepository extends Repository<Prerequisite, String> {
    private static final String TABLE_NAME = "CoursePrerequisites";
    private static PrerequisiteRepository instance = null;

    public static PrerequisiteRepository getInstance() {
        if (instance == null) {
            try {
                instance = new PrerequisiteRepository();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("error in BolbolRepository.create query.");
            }
        }
        return instance;
    }

    private PrerequisiteRepository() throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement createTableStatement = con.prepareStatement(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s" +
                                "(`CourseCode` varchar(10) NOT NULL,\n" +
                                "  `PrerequisiteCode` varchar(10) NOT NULL,\n" +
                                "  `PrerequisiteName` varchar(40),\n" +
                                "  PRIMARY KEY (`CourseCode`,`PrerequisiteCode`),\n" +
                                "  CONSTRAINT `FK_CoursePrerequisites_Courses` FOREIGN KEY (`CourseCode`) REFERENCES `Courses` (`CourseCode`),\n" +
                                "  CONSTRAINT `FK_CoursePrerequisites_PreCourses` FOREIGN KEY (`PrerequisiteCode`) REFERENCES `Courses` (`CourseCode`)\n" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;",
                        TABLE_NAME)
        );
        createTableStatement.executeUpdate();
        createTableStatement.close();
        con.close();
    }

    @Override
    protected String getExistingTableStatement() {
        return String.format("SELECT EXISTS(SELECT 1 FROM %s) AS Output;", TABLE_NAME);
    }

    @Override
    protected String getFindByIdStatement() {
        return String.format("SELECT* FROM %s std WHERE std.StudentID = ?;", TABLE_NAME);
    }

    @Override
    protected String getFindByTypeStatement() {
        return String.format("SELECT* FROM %s cp WHERE cp.CourseCode = ?;", TABLE_NAME);
    }

    @Override
    protected void fillFindByIdValues(PreparedStatement st, String id) throws SQLException {
        st.setString(1, id);
    }

    @Override
    protected Prerequisite getFindByTypeStatement(String type) {
        return null;
    }

    @Override
    protected void fillFindByTypeValues(PreparedStatement st, String code) throws SQLException {
        st.setString(1, code);
    }


    @Override
    protected String getInsertStatement() {
        return String.format("INSERT INTO %s(CourseCode, PrerequisiteCode, PrerequisiteName) VALUES(?,?,?)", TABLE_NAME);
    }

    @Override
    protected void fillInsertValues(PreparedStatement st, Prerequisite data) throws SQLException {
        st.setString(1, data.getCourseCode());
        st.setString(2, data.getPrerequisiteCode());
        st.setString(3, data.getPrerequisiteName());
    }

    @Override
    protected String getFindAllStatement() {
        return String.format("SELECT * FROM %s;", TABLE_NAME);
    }

    @Override
    protected Prerequisite convertResultSetToDomainModel(ResultSet rs) throws SQLException {
        return new Prerequisite(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3));
    }

    @Override
    protected ArrayList<Prerequisite> convertResultSetToDomainModelList(ResultSet rs) throws SQLException {
        ArrayList<Prerequisite> prerequisites = new ArrayList<>();
        while (rs.next()) {
            prerequisites.add(this.convertResultSetToDomainModel(rs));
        }
        return prerequisites;
    }

    protected String getHasPassedStatement() {
        return String.format(
                "SELECT DISTINCT c.PrerequisiteCode from %s c, Grades g \n" +
                        "                WHERE c.CourseCode = ? \n" +
                        "                AND ((g.StudentID = ?\n" +
                        "                AND c.PrerequisiteCode = g.CourseCode AND g.Grade < 10)\n" +
                        "                OR (c.PrerequisiteCode NOT IN\n" +
                        "                        (SELECT g2.CourseCode from Grades g2\n" +
                        "                        WHERE g2.StudentID = ?)\n" +
                        "));", TABLE_NAME);
    }

    protected void fillHasPassedValues(PreparedStatement st, String studentId, String courseCode) throws SQLException {
        st.setString(1, courseCode);
        st.setString(2, studentId);
        st.setString(3, studentId);
    }

    public List<String> hasPassedCourse(String studentId, String code) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getHasPassedStatement());
        fillHasPassedValues(st, studentId, code);
        try {
            ResultSet resultSet = st.executeQuery();
            if (resultSet == null) {
                st.close();
                con.close();
                return null;
            }
            List<String> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
            st.close();
            con.close();
            return result;
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.findAll query.");
            e.printStackTrace();
            throw e;
        }
    }
}

