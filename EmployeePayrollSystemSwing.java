package Tech_Employee_Payroll_System;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class EmployeePayrollSystemSwing extends JFrame {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/Epayroll";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private JTextField nameField;
    private JTextField designationField;
    private JTextField basicSalaryField;
    private JTextField allowanceField;
    private JTextField deductionField;

    public EmployeePayrollSystemSwing() {
        super("Employee Payroll System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        createEmployeeTable();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2));

        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Designation:"));
        designationField = new JTextField();
        panel.add(designationField);

        panel.add(new JLabel("Basic Salary:"));
        basicSalaryField = new JTextField();
        panel.add(basicSalaryField);

        panel.add(new JLabel("Allowance:"));
        allowanceField = new JTextField();
        panel.add(allowanceField);

        panel.add(new JLabel("Deduction:"));
        deductionField = new JTextField();
        panel.add(deductionField);

        JButton addButton = new JButton("Add Employee");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEmployee();
                calculateAndDisplayPayroll();
            }
        });
        panel.add(addButton);

        JButton displayButton = new JButton("Display Payroll");
        displayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateAndDisplayPayroll();
            }
        });
        panel.add(displayButton);

        add(panel);
        setVisible(true);
    }

    private void createEmployeeTable() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS employees ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(100),"
                    + "designation VARCHAR(50),"
                    + "basic_salary DOUBLE,"
                    + "allowance DOUBLE,"
                    + "deduction DOUBLE,"
                    + "net_salary DOUBLE)";

            statement.executeUpdate(createTableSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addEmployee() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            String name = nameField.getText();
            String designation = designationField.getText();
            double basicSalary = Double.parseDouble(basicSalaryField.getText());
            double allowance = Double.parseDouble(allowanceField.getText());
            double deduction = Double.parseDouble(deductionField.getText());

            String insertSQL = "INSERT INTO employees (name, designation, basic_salary, allowance, deduction) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, designation);
                preparedStatement.setDouble(3, basicSalary);
                preparedStatement.setDouble(4, allowance);
                preparedStatement.setDouble(5, deduction);

                preparedStatement.executeUpdate();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int employeeId = generatedKeys.getInt(1);
                    calculateNetSalary(connection, employeeId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateNetSalary(Connection connection, int employeeId) {
        try {
            String selectEmployeeSQL = "SELECT * FROM employees WHERE id = ?";
            String updateNetSalarySQL = "UPDATE employees SET net_salary = ? WHERE id = ?";

            try (PreparedStatement selectStatement = connection.prepareStatement(selectEmployeeSQL)) {
                selectStatement.setInt(1, employeeId);
                ResultSet resultSet = selectStatement.executeQuery();

                if (resultSet.next()) {
                    double basicSalary = resultSet.getDouble("basic_salary");
                    double allowance = resultSet.getDouble("allowance");
                    double deduction = resultSet.getDouble("deduction");

                    double grossSalary = basicSalary + allowance;
                    double netSalary = grossSalary - deduction;

                    try (PreparedStatement updateStatement = connection.prepareStatement(updateNetSalarySQL)) {
                        updateStatement.setDouble(1, netSalary);
                        updateStatement.setInt(2, employeeId);
                        updateStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void calculateAndDisplayPayroll() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            String selectAllEmployeesSQL = "SELECT * FROM employees";
            ResultSet resultSet = statement.executeQuery(selectAllEmployeesSQL);

            while (resultSet.next()) {
                System.out.println("Employee ID: " + resultSet.getInt("id"));
                System.out.println("Name: " + resultSet.getString("name"));
                System.out.println("Designation: " + resultSet.getString("designation"));
                System.out.println("Basic Salary: " + resultSet.getDouble("basic_salary"));
                System.out.println("Allowance: " + resultSet.getDouble("allowance"));
                System.out.println("Deduction: " + resultSet.getDouble("deduction"));
                System.out.println("Net Salary: " + resultSet.getDouble("net_salary"));
                System.out.println("-----------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new EmployeePayrollSystemSwing();
            }
        });
    }
}
