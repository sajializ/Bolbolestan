package com.enrollment.Bolbolestan.repository;

import com.enrollment.Bolbolestan.model.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class GradeRepository extends Repository<Grade, String> {
    private static final String TABLE_NAME = "Grades";
    private static GradeRepository instance = null;

    public static GradeRepository getInstance() {
        if (instance == null) {
            try {
                instance = new GradeRepository();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("error in BolbolRepository.create query.");
            }
        }
        return instance;
    }

    private GradeRepository() throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement createTableStatement = con.prepareStatement(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s" +
                                "(`StudentID` varchar(15) NOT NULL,\n" +
                                "  `CourseCode` varchar(10) NOT NULL,\n" +
                                "  `CourseName` varchar(40) NOT NULL,\n" +
                                "  `Units` int NOT NULL,\n" +
                                "  `Grade` integer DEFAULT NULL,\n" +
                                "  `Term` integer NOT NULL,\n" +
                                "  PRIMARY KEY (`StudentID`, `CourseCode`, `Term`),\n" +
                                "  CONSTRAINT `FK_Grades_Students` FOREIGN KEY (`StudentID`) REFERENCES `Students` (`StudentID`),\n" +
                                "  CONSTRAINT `FK_Grades_Courses` FOREIGN KEY (`CourseCode`) REFERENCES `Courses` (`CourseCode`)\n" +
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
        return String.format("SELECT* FROM %s g WHERE g.StudentID = ?;", TABLE_NAME);
    }

    @Override
    protected void fillFindByIdValues(PreparedStatement st, String id) throws SQLException {
        st.setString(1, id);
    }

    @Override
    protected Grade getFindByTypeStatement(String type) {
        return null;
    }

    protected String getFindByTermStatement() {
        return String.format("SELECT* FROM %s g WHERE g.StudentID = ? AND g.Term = ?;", TABLE_NAME);
    }

    @Override
    protected void fillFindByTypeValues(PreparedStatement st, String id) throws SQLException {
        st.setString(1, id);
    }

    protected void fillFindByTermValue(PreparedStatement st, String studentId, Integer term) throws SQLException {
        st.setString(1, studentId);
        st.setInt(2, term);
    }

    protected void fillHasPassedValue(PreparedStatement st, String studentId, String code) throws SQLException {
        st.setString(1, studentId);
        st.setString(2, code);
    }

    protected void fillFindCurrentTermValue(PreparedStatement st, String studentId) throws SQLException {
        st.setString(1, studentId);
    }


    @Override
    protected String getInsertStatement() {
        return String.format("INSERT INTO %s(StudentID, CourseCode, CourseName, Units, Grade, Term) VALUES(?,?,?,?,?,?)", TABLE_NAME);
    }

    @Override
    protected void fillInsertValues(PreparedStatement st, Grade data) throws SQLException {
        st.setString(1, data.getStudentId());
        st.setString(2, data.getCode());
        st.setString(3, data.getCourseName());
        st.setInt(4, data.getUnits());
        st.setInt(5, data.getScore());
        st.setInt(6, data.getTerm());

    }

    @Override
    protected String getFindAllStatement() {
        return String.format("SELECT * FROM %s;", TABLE_NAME);
    }

    protected String getFindGPA() {
        return String.format("SELECT SUM(g.Units*g.Grade)/SUM(g.Units) FROM %s g WHERE g.StudentID=?;", TABLE_NAME);
    }

    protected String getFindCurrentTerm() {
        return String.format("SELECT MAX(g.Term) AS recentTerm FROM %s g WHERE g.StudentID=?;", TABLE_NAME);
    }

    protected String getFindTotalPassedUnits() {
        return String.format("SELECT SUM(g.Units) AS total FROM %s g WHERE g.StudentID=?;", TABLE_NAME);
    }

    @Override
    protected Grade convertResultSetToDomainModel(ResultSet rs) throws SQLException {
        return new Grade(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4),
                rs.getInt(5),
                rs.getInt(6));
    }


    @Override
    protected ArrayList<Grade> convertResultSetToDomainModelList(ResultSet rs) throws SQLException {
        ArrayList<Grade> prerequisites = new ArrayList<>();
        while (rs.next()) {
            prerequisites.add(this.convertResultSetToDomainModel(rs));
        }
        return prerequisites;
    }

    public Integer findCurrentSemester(String studentId) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindCurrentTerm());
        fillFindCurrentTermValue(st, studentId);
        try {
            ResultSet resultSet = st.executeQuery();
            if (resultSet == null) {
                st.close();
                con.close();
                return 1;
            }
            int result = 1;
            while (resultSet.next()) {
                result = resultSet.getInt(1) + 1;
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

    public Integer findTotalPassedUnits(String studentId) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindTotalPassedUnits());
        fillFindCurrentTermValue(st, studentId);
        try {
            ResultSet resultSet = st.executeQuery();
            if (resultSet == null) {
                st.close();
                con.close();
                return 0;
            }
            int result = 0;
            while (resultSet.next()) {
                result = resultSet.getInt(1);
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

    public float findGPA(String studentId) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindGPA());
        fillFindCurrentTermValue(st, studentId);
        try {
            ResultSet resultSet = st.executeQuery();
            if (resultSet == null) {
                st.close();
                con.close();
                return 0;
            }
            float result = 0;
            while (resultSet.next()) {
                result = resultSet.getFloat(1);
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



    public List<Grade> findAllGradesByTerm(String studentId, Integer term) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindByTermStatement());
        fillFindByTermValue(st, studentId, term);
        try {
            ResultSet resultSet = st.executeQuery();
            if (resultSet == null) {
                st.close();
                con.close();
                return new ArrayList<>();
            }
            List<Grade> result = convertResultSetToDomainModelList(resultSet);
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

    protected String getHasPassedStatement() {
        return String.format("SELECT EXISTS(SELECT * from %s g WHERE g.StudentID = ? AND g.CourseCode=? AND g.Grade > 10);", TABLE_NAME);
    }

    public boolean hasPassedCourse(String studentId, String code) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getHasPassedStatement());
        fillHasPassedValue(st, studentId, code);
        try {
            ResultSet resultSet = st.executeQuery();
            if (resultSet == null) {
                st.close();
                con.close();
                return false;
            }
            float result = 0;
            while (resultSet.next()) {
                result = resultSet.getInt(1);
            }
            st.close();
            con.close();
            return (result == 1);
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.findAll query.");
            e.printStackTrace();
            throw e;
        }
    }
}

