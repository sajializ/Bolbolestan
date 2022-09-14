package com.enrollment.Bolbolestan.repository;

import com.enrollment.Bolbolestan.model.Offering;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository extends Repository<Offering, String> {
    private static final String TABLE_NAME = "Courses";
    private static CourseRepository instance = null;

    public static CourseRepository getInstance() {
        if (instance == null) {
            try {
                instance = new CourseRepository();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("error in BolbolRepository.create query.");
            }
        }
        return instance;
    }

    private CourseRepository() throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement createTableStatement = con.prepareStatement(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s" +
                                "( `CourseCode` varchar(10) NOT NULL,\n" +
                                "  `ClassCode` varchar(2) NOT NULL,\n" +
                                "  `CourseName` varchar(40) NOT NULL,\n" +
                                "  `Units` integer NOT NULL,\n" +
                                "  `CourseType` varchar(10) NOT NULL,\n" +
                                "  `Instructor` varchar(20) NOT NULL,\n" +
                                "  `Capacity` integer DEFAULT 0,\n" +
                                "  `ClassDays` mediumtext NOT NULL,\n" +
                                "  `ClassTime` varchar(15) NOT NULL,\n" +
                                "  `ExamStart` varchar(24) NOT NULL,\n" +
                                "  `ExamEnd` varchar(24) NOT NULL,\n" +
                                "  `RegisteredStudents` int NOT NULL DEFAULT 0,\n" +
                                "  PRIMARY KEY (`CourseCode`, `ClassCode`),\n" +
                                "  KEY `CourseCode` (`CourseCode`),\n" +
                                "  KEY `ClassCode` (`ClassCode`)\n" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n", TABLE_NAME)
        );
        createTableStatement.executeUpdate();
        createTableStatement.close();
        con.close();
    }

    @Override
    protected String getFindByIdStatement() {
        return String.format("SELECT* FROM %s c WHERE c.CourseCode = ?;", TABLE_NAME);
    }

    @Override
    protected String getExistingTableStatement() {
        return String.format("SELECT EXISTS(SELECT 1 FROM %s) AS Output;", TABLE_NAME);
    }

    protected String getFindByCourseCodeStatement() {
        return String.format("SELECT* FROM %s c WHERE c.CourseCode = ?;", TABLE_NAME);
    }

    protected String getFindByCodeStatement() {
        return String.format("SELECT* FROM %s c WHERE c.CourseCode = ? and c.ClassCode = ?;", TABLE_NAME);
    }

    protected String getFindByNameStatement() {
        return String.format("SELECT* FROM %s WHERE CourseName LIKE ?;", TABLE_NAME);
    }

    protected  String getIncrementTotalRegisteredStudentsStatement() {
        return String.format("UPDATE %s c SET c.RegisteredStudents = c.RegisteredStudents + 1 WHERE c.CourseCode=? AND c.ClassCode=?;", TABLE_NAME);
    }

    protected  String getDecrementTotalRegisteredStudentsStatement() {
        return String.format("UPDATE %s c SET c.RegisteredStudents = c.RegisteredStudents - 1 WHERE c.CourseCode=? AND c.ClassCode=?;", TABLE_NAME);
    }

    protected String getAddRegisteredStudentsStatement() {
        return String.format("UPDATE %s c \n" +
                "SET c.RegisteredStudents = " +
                "c.RegisteredStudents + (SELECT COUNT(*) AS cnt FROM Enrolled e WHERE e.CourseCode = c.CourseCode AND e.ClassCode = c.ClassCode AND e.CourseStatus = ?);", TABLE_NAME);
    }

    @Override
    protected String getFindByTypeStatement() {
        return String.format("SELECT* FROM %s c WHERE c.CourseType = ?;", TABLE_NAME);
    }

    @Override
    protected void fillFindByIdValues(PreparedStatement st, String id) throws SQLException {
        st.setString(1, id);
    }

    protected void fillWaitingListValues(PreparedStatement st, String courseCode, String classCode) throws SQLException {
        st.setString(1, "waiting");
        st.setString(2, courseCode);
        st.setString(3, classCode);
    }

    protected void fillFindByCodeValues(PreparedStatement st, String code, String classCode) throws SQLException {
        st.setString(1, code);
        st.setString(2, classCode);
    }


    @Override
    protected void fillFindByTypeValues(PreparedStatement st, String type) throws SQLException {
        st.setString(1, type);
    }

    @Override
    protected String getInsertStatement() {
        return String.format("INSERT INTO %s(CourseCode, ClassCode, CourseName, Units, CourseType, Instructor, Capacity, ClassDays, ClassTime, ExamStart, ExamEnd) VALUES(?,?,?,?,?,?,?,?,?,?,?)", TABLE_NAME);
    }

    @Override
    protected void fillInsertValues(PreparedStatement st, Offering data) throws SQLException {
        st.setString(1, data.getCode());
        st.setString(2, data.getClassCode());
        st.setString(3, data.getName());
        st.setInt(4, data.getUnits());
        st.setString(5, data.getType());
        st.setString(6, data.getInstructor());
        st.setInt(7, data.getCapacity());
        st.setString(8, data.getDays());
        st.setString(9, data.getTime());
        st.setString(10, data.getExamStartTime());
        st.setString(11, data.getExamEndTime());
    }

    @Override
    protected String getFindAllStatement() {
        return String.format("SELECT * FROM %s;", TABLE_NAME);
    }

    @Override
    protected Offering convertResultSetToDomainModel(ResultSet rs) throws SQLException {
        return new Offering(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getInt(4),
                rs.getString(5),
                rs.getString(6),
                rs.getInt(7),
                rs.getString(8),
                rs.getString(9),
                rs.getString(10),
                rs.getString(11),
                rs.getInt(12)
                );
    }

    @Override
    protected ArrayList<Offering> convertResultSetToDomainModelList(ResultSet rs) throws SQLException {
        ArrayList<Offering> courses = new ArrayList<>();
        while (rs.next()) {
            courses.add(this.convertResultSetToDomainModel(rs));
        }
        return courses;
    }

    public Offering findByCode(String courseCode, String classCode) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindByCodeStatement());
        fillFindByCodeValues(st, courseCode, classCode);

        try {
            ResultSet resultSet = st.executeQuery();
            if (!resultSet.next()) {
                st.close();
                con.close();
                return null;
            }
            Offering result = convertResultSetToDomainModel(resultSet);
            st.close();
            con.close();
            return result;
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.find query.");
            e.printStackTrace();
            throw e;
        }
    }

    public Offering findByCourseCode(String courseCode) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindByCourseCodeStatement());
        fillFindByIdValues(st, courseCode);
        try {
            ResultSet resultSet = st.executeQuery();
            if (!resultSet.next()) {
                st.close();
                con.close();
                return null;
            }
            Offering result = convertResultSetToDomainModel(resultSet);
            st.close();
            con.close();
            return result;
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.find query.");
            e.printStackTrace();
            throw e;
        }
    }

    public List<Offering> findByCourseName(String searchKey) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindByNameStatement());
        fillFindByIdValues(st, searchKey);
        try {
            ResultSet resultSet = st.executeQuery();
            if (resultSet == null) {
                st.close();
                con.close();
                return new ArrayList<>();
            }
            List<Offering> result = convertResultSetToDomainModelList(resultSet);
            st.close();
            con.close();
            return result;
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.find query.");
            e.printStackTrace();
            throw e;
        }
    }


    public void incrementRegisteredStudents(String courseCode, String classCode) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getIncrementTotalRegisteredStudentsStatement());
        fillFindByCodeValues(st, courseCode, classCode);
        try {
            st.executeUpdate();
            st.close();
            con.close();
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.find query.");
            e.printStackTrace();
            throw e;
        }
    }

    public void decrementRegisteredStudents(String courseCode, String classCode) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getDecrementTotalRegisteredStudentsStatement());
        fillFindByCodeValues(st, courseCode, classCode);
        try {
            st.executeUpdate();
            st.close();
            con.close();
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.find query.");
            e.printStackTrace();
            throw e;
        }
    }

    public void emptyWaitingList() throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getAddRegisteredStudentsStatement());
        st.setString(1, "waiting");
        try {
            st.executeUpdate();
            st.close();
            con.close();
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.find query.");
            e.printStackTrace();
            throw e;
        }
    }

    protected String getFindRemainingCapacityStatement() {
        return String.format("SELECT Capacity - RegisteredStudents AS r FROM %s WHERE CourseCode = ? AND ClassCode = ?;", TABLE_NAME);
    }

    public Integer findRemainingCapacity(String courseCode, String classCode) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindRemainingCapacityStatement());
        fillFindByCodeValues(st, courseCode, classCode);
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
            System.out.println("error in Repository.register query.");
            e.printStackTrace();
            throw e;
        }
    }
}