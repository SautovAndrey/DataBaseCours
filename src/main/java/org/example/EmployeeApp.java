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

    private static final String DB_URL = "jdbc:mysql://localhost:3306/employee";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private final JFrame frame;
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField ageField;
    private final JTextField positionField;
    private final JTable table;
    private final DefaultTableModel tableModel;

    public EmployeeApp() {
        frame = new JFrame("Employee Management");

        Font regularFont = new Font("Arial", Font.PLAIN, 15);
        Font boldFont = new Font("Arial", Font.BOLD, 15);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.5;
        constraints.insets = new Insets(10, 10, 10, 10);

        addComponent(panel, createLabel("ID:", boldFont), constraints, 0, 0);
        idField = createTextField(regularFont);
        addComponent(panel, idField, constraints, 1, 0);

        addComponent(panel, createLabel("Name:", boldFont), constraints, 0, 1);
        nameField = createTextField(regularFont);
        addComponent(panel, nameField, constraints, 1, 1);

        addComponent(panel, createLabel("Age:", boldFont), constraints, 0, 2);
        ageField = createTextField(regularFont);
        addComponent(panel, ageField, constraints, 1, 2);

        addComponent(panel, createLabel("Job_title:", boldFont), constraints, 0, 3);
        positionField = createTextField(regularFont);
        addComponent(panel, positionField, constraints, 1, 3);

        JButton addButton = new JButton("Add employee");
        addButton.setFont(boldFont);
        addButton.addActionListener(e -> {
            addEmployee();
            updateTable();
        });
        addComponent(panel, addButton, constraints, 1, 4);

        JButton deleteButton = createDeleteButton(boldFont);
        addComponent(panel, deleteButton, constraints, 1, 5);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "Job_title"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        table = new JTable(tableModel);
        table.setFont(regularFont);
        table.getTableHeader().setFont(boldFont);
        table.setRowHeight(25);
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

    private JButton createDeleteButton(Font boldFont) {
        JButton deleteButton = new JButton("Drop");
        deleteButton.setFont(boldFont);
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
        return deleteButton;
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }

    private JTextField createTextField(Font font) {
        JTextField textField = new JTextField(15);
        textField.setFont(font);
        return textField;
    }

    private void addComponent(JPanel panel, Component component, GridBagConstraints constraints, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        panel.add(component, constraints);
    }

    private void addEmployee() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
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

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
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
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
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

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE employee SET " + columnName + " = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                if (value instanceof String) {
                    preparedStatement.setString(1, (String) value);
                } else if (value instanceof Integer) {
                    preparedStatement.setInt(1, (Integer) value);
                }

                preparedStatement.setInt(2, id);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected == 0) {
                    JOptionPane.showMessageDialog(frame, "Could not update employee.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "An error occurred while updating an employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new EmployeeApp();
    }
}
