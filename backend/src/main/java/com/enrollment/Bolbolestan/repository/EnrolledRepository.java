package com.enrollment.Bolbolestan.repository;

import com.enrollment.Bolbolestan.model.Enrolled;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class EnrolledRepository extends Repository<Enrolled, String> {
    private static final String TABLE_NAME = "Enrolled";
    private static EnrolledRepository instance = null;

    public static EnrolledRepository getInstance() {
        if (instance == null) {
            try {
                instance = new EnrolledRepository();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("error in BolbolRepository.create query.");
            }
        }
        return instance;
    }



    private EnrolledRepository() throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement createTableStatement = con.prepareStatement(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s" +
                                "( `StudentID` varchar(15) NOT NULL,\n" +
                                "  `CourseCode` varchar(10) NOT NULL,\n" +
                                "  `ClassCode` varchar(2) NOT NULL,\n" +
                                "  `CourseName` varchar(40) NOT NULL,\n" +
                                "  `Instructor` varchar(30) NOT NULL,\n" +
                                "  `Units` int NOT NULL,\n" +
                                "  `CourseStatus` varchar(20) DEFAULT 'non-finalized',\n" +
                                "  PRIMARY KEY (`StudentID`, `CourseCode`, `ClassCode`),\n" +
                                "  CONSTRAINT `FK_Enrolled_Students` FOREIGN KEY (`StudentID`) REFERENCES `Students` (`StudentID`),\n" +
                                "  CONSTRAINT `FK_Enrolled_Courses` FOREIGN KEY (`CourseCode`) REFERENCES `Courses` (`CourseCode`),\n" +
                                "  CONSTRAINT `FK_Enrolled_Courses_ClassCode` FOREIGN KEY (`ClassCode`) REFERENCES `Courses` (`ClassCode`)\n" +
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
        return String.format("SELECT* FROM %s e WHERE e.StudentID = ?;", TABLE_NAME);
    }

    protected String getFindAllByStudentIdStatement() {
        return String.format("SELECT* FROM %s e WHERE e.StudentID = ?;", TABLE_NAME);
    }

    protected String getFindByStudentAndCodeStatement() {
        return String.format("SELECT* FROM %s e WHERE e.StudentID = ? AND e.CourseCode=? AND e.ClassCode=?;", TABLE_NAME);
    }

    protected String deleteByStudentAndCodeStatement() {
        return String.format("DELETE e FROM %s e WHERE e.StudentID = ? AND e.CourseCode=? AND e.ClassCode=? AND (e.CourseStatus=? OR e.CourseStatus=?);", TABLE_NAME);
    }

    protected String deleteFinalizedByStudentAndCodeStatement() {
        return String.format("DELETE e FROM %s e WHERE e.StudentID = ? AND e.CourseCode=? AND e.ClassCode=?;", TABLE_NAME);
    }

    protected String updateByStudentAndCodeStatement() {
        return String.format("UPDATE %s e SET e.CourseStatus = ? WHERE e.StudentID = ? AND e.CourseCode=? AND e.ClassCode=? AND e.CourseStatus=?;", TABLE_NAME);
    }

    protected String resetEnrolledByStudentIdStatement() {
        return String.format("DELETE e FROM %s e WHERE StudentID = ? AND e.CourseStatus=?;", TABLE_NAME);
    }

    protected String resetEnrolledByStudentIdStatementAlt() {
        return String.format("UPDATE %s e SET e.CourseStatus = ? WHERE e.StudentID = ? AND e.CourseStatus=?;", TABLE_NAME);
    }

    protected String getUpdateEnrollmentStatusStatement() {
        return String.format("UPDATE %s e SET e.CourseStatus = ? WHERE e.CourseStatus=?;", TABLE_NAME);
    }

    protected String getFindTotalUnitsByStudentIdStatement() {
        return String.format("SELECT SUM(e.Units) FROM %s e WHERE e.StudentID = ?;", TABLE_NAME);
    }

    @Override
    protected String getFindByTypeStatement() {
        return String.format("SELECT* FROM %s e WHERE e.StudentID = ?;", TABLE_NAME);
    }

    @Override
    protected void fillFindByIdValues(PreparedStatement st, String id) throws SQLException {
        st.setString(1, id);
    }

    @Override
    protected void fillFindByTypeValues(PreparedStatement st, String studentId) throws SQLException {
        st.setString(1, studentId);
    }

    protected void fillResetFinalizedEnrollmentValues(PreparedStatement st, String studentId) throws SQLException {
        st.setString(1, "finalized");
        st.setString(2, studentId);
        st.setString(3, "deleted");
    }
    protected void fillFindByStudentAndCodeValues(PreparedStatement st, String studentId, String code, String classCode) throws SQLException {
        st.setString(1, studentId);
        st.setString(2, code);
        st.setString(3, classCode);
    }

    protected void fillAltFindByStudentAndCodeValues(PreparedStatement st, String studentId, String code, String classCode) throws SQLException {
        st.setString(2, studentId);
        st.setString(3, code);
        st.setString(4, classCode);
    }

    protected void fillUpdateEnrollmentStatusStatement(PreparedStatement st, String finalState, String initialState) throws SQLException {
        st.setString(1, finalState);
        st.setString(2, initialState);
    }

    @Override
    protected String getInsertStatement() {
        return String.format("INSERT INTO %s(StudentID, CourseCode, ClassCode, CourseName, Instructor, Units, CourseStatus) VALUES(?,?,?,?,?,?,?)", TABLE_NAME);
    }

    @Override
    protected void fillInsertValues(PreparedStatement st, Enrolled data) throws SQLException {
        st.setString(1, data.getStudentID());
        st.setString(2, data.getCourseCode());
        st.setString(3, data.getClassCode());
        st.setString(4, data.getCourseName());
        st.setString(5, data.getInstructor());
        st.setInt(6, data.getUnits());
        st.setString(7, data.getStatus());
    }

    @Override
    protected String getFindAllStatement() {
        return String.format("SELECT * FROM %s;", TABLE_NAME);
    }


    @Override
    protected Enrolled convertResultSetToDomainModel(ResultSet rs) throws SQLException {
        return new Enrolled(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getInt(6),
                rs.getString(7)
        );
    }

    @Override
    protected ArrayList<Enrolled> convertResultSetToDomainModelList(ResultSet rs) throws SQLException {
        ArrayList<Enrolled> enrolledCourses = new ArrayList<>();
        while (rs.next()) {
            enrolledCourses.add(this.convertResultSetToDomainModel(rs));
        }
        return enrolledCourses;
    }

    private Enrolled executeStatement(Connection con, PreparedStatement st) throws SQLException {
        try {
            ResultSet resultSet = st.executeQuery();
            if (!resultSet.next()) {
                st.close();
                con.close();
                return null;
            }
            Enrolled result = convertResultSetToDomainModel(resultSet);
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

    public Enrolled findByStudentAndCode(String studentId, String courseCode, String classCode) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindByStudentAndCodeStatement());
        fillFindByStudentAndCodeValues(st, studentId, courseCode, classCode);
        return executeStatement(con, st);
    }

    public void deleteEnrollment(String studentId, String courseCode, String classCode) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(deleteByStudentAndCodeStatement());
        PreparedStatement fst = con.prepareStatement(updateByStudentAndCodeStatement());
        fillFindByStudentAndCodeValues(st, studentId, courseCode, classCode);
        fillAltFindByStudentAndCodeValues(fst, studentId, courseCode, classCode);
        fst.setString(1, "deleted");
        st.setString(4, "non-finalized");
        st.setString(5,"waiting");
        fst.setString(5, "finalized");
        try {
            st.executeUpdate();
            st.close();
            fst.executeUpdate();
            fst.close();
            con.close();
        } catch (Exception e) {
            st.close();
            fst.close();
            con.close();
            System.out.println("error in Repository.delete query.");
            e.printStackTrace();
            throw e;
        }
    }

    public void resetEnrollment(String studentId) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(resetEnrolledByStudentIdStatement());
        PreparedStatement fst = con.prepareStatement(resetEnrolledByStudentIdStatementAlt());
        fillFindByTypeValues(st, studentId);
        fillResetFinalizedEnrollmentValues(fst, studentId);
        st.setString(2, "non-finalized");
        try {
            st.executeUpdate();
            st.close();
            fst.executeUpdate();
            fst.close();
            con.close();
        } catch (Exception e) {
            st.close();
            fst.close();
            con.close();
            System.out.println("error in Repository.reset query.");
            e.printStackTrace();
            throw e;
        }
    }

    public void registerEnrollment(String studentId, String courseCode, String classCode, String initialStatus, String finalStatus) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(updateByStudentAndCodeStatement());
        fillAltFindByStudentAndCodeValues(st, studentId, courseCode, classCode);
        st.setString(1, finalStatus);
        st.setString(5, initialStatus);
        try {
            st.executeUpdate();
            st.close();
            con.close();
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.register query.");
            e.printStackTrace();
            throw e;
        }
    }

    public void deleteFinalizedEnrollment(String studentId, String courseCode, String classCode) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(deleteFinalizedByStudentAndCodeStatement());
        fillFindByStudentAndCodeValues(st, studentId, courseCode, classCode);
        try {
            st.executeUpdate();
            st.close();
            con.close();
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.delete query.");
            e.printStackTrace();
            throw e;
        }
    }

    public void updateWaitingEnrollment() throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getUpdateEnrollmentStatusStatement());
        fillUpdateEnrollmentStatusStatement(st, "finalized", "waiting");
        try {
            st.executeUpdate();
            st.close();
            con.close();
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.register query.");
            e.printStackTrace();
            throw e;
        }
    }

    public Integer getTotalSelectedUnits(String studentId) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindTotalUnitsByStudentIdStatement());
        fillFindByTypeValues(st, studentId);
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


