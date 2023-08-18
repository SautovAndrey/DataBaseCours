package org.example;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EmployeeApp {

    private final JFrame frame;
    private JTextField idField, nameField, ageField, positionField;
    private JTable table;
    private DefaultTableModel tableModel;

    public EmployeeApp() {
        frame = new JFrame("Employee Management");
        JPanel panel = new JPanel(new GridLayout(7, 2));

        panel.add(new JLabel("ID:"));
        idField = new JTextField(15);
        panel.add(idField);

        panel.add(new JLabel("Name:"));
        nameField = new JTextField(15);
        panel.add(nameField);

        panel.add(new JLabel("Age:"));
        ageField = new JTextField(15);
        panel.add(ageField);

        panel.add(new JLabel("Job_title:"));
        positionField = new JTextField(15);
        panel.add(positionField);

        JButton addButton = new JButton("Add employee");
        addButton.addActionListener(e -> {
            addEmployee();
            updateTable();
        });
        panel.add(addButton);

        JButton deleteButton = new JButton("Drop");
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int idToDelete = (int) tableModel.getValueAt(selectedRow, 0);
                deleteEmployee(idToDelete);
                updateTable();
            } else {
                JOptionPane.showMessageDialog(frame, "Select the line to delete.");
            }
        });
        panel.add(deleteButton);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "Job_title"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        updateTable();

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                Object newValue = tableModel.getValueAt(row, column);
                int idToUpdate = (int) tableModel.getValueAt(row, 0);
                updateEmployee(idToUpdate, column, newValue);
            }
        });
    }

    private void addEmployee() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/employee", "root", "root")) {
            String sql = "INSERT INTO employee (id, name, age, position) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, Integer.parseInt(idField.getText()));
                preparedStatement.setString(2, nameField.getText());
                preparedStatement.setInt(3, Integer.parseInt(ageField.getText()));
                preparedStatement.setString(4, positionField.getText());

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(frame, "Employee successfully added!");
                    idField.setText("");
                    nameField.setText("");
                    ageField.setText("");
                    positionField.setText("");
                } else {
                    JOptionPane.showMessageDialog(frame, "Could not add employee.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An error occurred while adding an employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/employee", "root", "root")) {
            String sql = "SELECT * FROM employee";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    String position = resultSet.getString("position");

                    tableModel.addRow(new Object[]{id, name, age, position});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An error occurred while updating the table.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEmployee(int id) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/employee", "root", "root")) {
            String sql = "DELETE FROM employee WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, id);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected == 0) {
                    JOptionPane.showMessageDialog(frame, "Could not find employee with provided ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An error occurred while deleting an employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmployee(int id, int column, Object value) {
        String columnName = tableModel.getColumnName(column);

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/employee", "root", "root")) {
            String sql = "UPDATE employee SET " + columnName + " = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                if (value instanceof String) {
                    preparedStatement.setString(1, (String) value);
                } else if (value instanceof Integer) {
                    preparedStatement.setInt(1, (Integer) value);
                }
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An error occurred while updating an employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmployeeApp::new);
    }
}
