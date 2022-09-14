package com.enrollment.Bolbolestan.repository;

import com.enrollment.Bolbolestan.model.Enrolled;
import com.enrollment.Bolbolestan.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StudentRepository extends Repository<Student, String> {
    private static final String TABLE_NAME = "Students";
    private static StudentRepository instance = null;

    public static StudentRepository getInstance() {
        if (instance == null) {
            try {
                instance = new StudentRepository();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("error in BolbolRepository.create query.");
            }
        }
        return instance;
    }

    private StudentRepository() throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement createTableStatement = con.prepareStatement(
                String.format(
                        "CREATE TABLE IF NOT EXISTS %s" +
                                "(StudentID VARCHAR(15),\n" +
                                "FirstName VARCHAR(15) NOT NULL,\n" +
                                "SecondName VARCHAR(30) NOT NULL,\n" +
                                "BirthDate VARCHAR(15) NOT NULL,\n" +
                                "Level VARCHAR(15) NOT NULL,\n" +
                                "Faculty VARCHAR(30) NOT NULL,\n" +
                                "Field VARCHAR(30) NOT NULL,\n" +
                                "Status VARCHAR(30) NOT NULL,\n" +
                                "Image VARCHAR(100),\n" +
                                "Email VARCHAR(50),\n" +
                                "Password VARCHAR(256),\n" +
                                "PRIMARY KEY(StudentID)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;",
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

    protected String getFindByEmailStatement() {
        return String.format("SELECT* FROM %s std WHERE std.Email = ?;", TABLE_NAME);
    }

    protected String findByCredentialsStatement() {
        return String.format("SELECT* FROM %s std WHERE std.Email = ? AND std.Password = ?;", TABLE_NAME);
    }

    @Override
    protected String getFindByTypeStatement() {
        return String.format("SELECT* FROM %s std WHERE std.Type = ?;", TABLE_NAME);
    }

    protected String getUpdatePasswordStatement() {
        return String.format("UPDATE %s s SET s.Password = ? WHERE s.Email = ?;", TABLE_NAME);
    }

    @Override
    protected void fillFindByIdValues(PreparedStatement st, String id) throws SQLException {
        st.setString(1, id);
    }

    protected void fillChangePasswordValues(PreparedStatement st, String password, String email) throws SQLException {
        st.setString(1, password);
        st.setString(2, email);
    }

    protected void fillFindByCredentialsValues(PreparedStatement st, String email, String psw) throws SQLException {
        st.setString(1, email);
        st.setString(2, psw);
    }

    @Override
    protected Student getFindByTypeStatement(String type) {
        return null;
    }

    @Override
    protected void fillFindByTypeValues(PreparedStatement p, String s) { }


    @Override
    protected String getInsertStatement() {
        return String.format("INSERT INTO %s(StudentID, FirstName, SecondNAme, BirthDate, Level, Faculty, Field, Status, Image, Email, Password) VALUES(?,?,?,?,?,?,?,?,?,?,?)", TABLE_NAME);
    }

    @Override
    protected void fillInsertValues(PreparedStatement st, Student data) throws SQLException {
        st.setString(1, data.getId());
        st.setString(2, data.getName());
        st.setString(3, data.getSecondName());
        st.setString(4, data.getBirthDate());
        st.setString(5, data.getField());
        st.setString(6, data.getFaculty());
        st.setString(7, data.getLevel());
        st.setString(8, data.getStatus());
        st.setString(9, data.getImg());
        st.setString(10, data.getEmail());
        st.setString(11, data.getPassword());
    }

    @Override
    protected String getFindAllStatement() {
        return String.format("SELECT * FROM %s;", TABLE_NAME);
    }

    @Override
    protected Student convertResultSetToDomainModel(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getString(7),
                rs.getString(8),
                rs.getString(9),
                rs.getString(10),
                rs.getString(11));
    }

    public Student findByEmail(String email) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getFindByEmailStatement());
        fillFindByIdValues(st, email);
        try {
            ResultSet resultSet = st.executeQuery();
            if (!resultSet.next()) {
                st.close();
                con.close();
                return null;
            }
            Student result = convertResultSetToDomainModel(resultSet);
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

    public Student findByCredentials(String username, String password) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(findByCredentialsStatement());
        fillFindByCredentialsValues(st, username, password);
        try {
            ResultSet resultSet = st.executeQuery();
            if (!resultSet.next()) {
                st.close();
                con.close();
                return null;
            }
            Student result = convertResultSetToDomainModel(resultSet);
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

    @Override
    protected ArrayList<Student> convertResultSetToDomainModelList(ResultSet rs) throws SQLException {
        ArrayList<Student> students = new ArrayList<>();
        while (rs.next()) {
            students.add(this.convertResultSetToDomainModel(rs));
        }
        return students;
    }

    public void changePassword(String email, String password) throws SQLException {
        Connection con = ConnectionPool.getConnection();
        PreparedStatement st = con.prepareStatement(getUpdatePasswordStatement());
        fillChangePasswordValues(st, password, email);
        try {
            st.executeUpdate();
            st.close();
            con.close();
        } catch (Exception e) {
            st.close();
            con.close();
            System.out.println("error in Repository.update query.");
            e.printStackTrace();
            throw e;
        }
    }
}

