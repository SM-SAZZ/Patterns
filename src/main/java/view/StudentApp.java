package view;

import org.sazz.dto.StudentFilter;
import org.sazz.enums.SearchParam;
import org.sazz.exceptions.ValidateException;
import org.sazz.strategy.Student_list_DB;
import org.sazz.student.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StudentApp {
    private static final int PAGE_SIZE = 20;
    private static int currentPage = 1;
    private static final Student_list_DB studentDB = new Student_list_DB();

    /** ���� ���������� **/
    private static final JTextField nameField = new JTextField();

    private static final JComboBox<String> gitComboBox = new JComboBox<>(new String[] { "�� �����", "��", "���" });
    private static final JTextField gitField = new JTextField();

    private static final JTextField emailField = new JTextField();
    private static final JComboBox<String> emailComboBox = new JComboBox<>(new String[] { "�� �����", "��", "���" });

    private static final JTextField phoneField = new JTextField();
    private static final JComboBox<String> phoneComboBox = new JComboBox<>(new String[] { "�� �����", "��", "���" });

    private static final JTextField telegramField = new JTextField();
    private static final JComboBox<String> telegramComboBox = new JComboBox<>(new String[] { "�� �����", "��", "���" });

    /** �������� ��������� **/
    private static final JLabel pageInfoLabel = new JLabel("��������: 1 / ?");
    private static final JButton prevPageButton = new JButton("����������");
    private static final JButton nextPageButton = new JButton("���������");

    /** ������ ���������� **/
    private static final JButton refreshButton = new JButton("��������");
    private static final JButton addButton = new JButton("��������");
    private static final JButton editButton = new JButton("��������");
    private static final JButton deleteButton = new JButton("�������");


    public static void create() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Student Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.add("������ ���������", createStudentTab());
            tabbedPane.add("������� 2", new JLabel("���������� ������� 2"));
            tabbedPane.add("������� 3", new JLabel("���������� ������� 3"));

            frame.add(tabbedPane);
            frame.setVisible(true);
        });
    }

    private static JPanel createStudentTab() {
        JPanel panel = new JPanel(new BorderLayout());

        addFilters(panel);

        // ������� ���������
        String[] columnNames = { "ID", "������� � ��������", "���", "Email", "�������", "Telegram" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // ������ ��������������
            }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // ������ ����������
        JPanel buttonPanel = new JPanel();

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            int selectedRowCount = table.getSelectedRowCount();
            editButton.setEnabled(selectedRowCount == 1); // "��������" �������� ������ ��� ��������� ����� ������
            deleteButton.setEnabled(selectedRowCount > 0); // "�������" �������� ��� ��������� ����� ��� ����� �����
        });

        // �������� ������ � �������
        refreshInfo(tableModel);

        // ����������� ������
        addButton.addActionListener(e -> {
            showStudentForm(null, "�������� ��������", student -> {
                int id = studentDB.addStudent(student);
                if (id > 0) {
                    JOptionPane.showMessageDialog(panel, "������� ��������!");
                    refreshInfo(tableModel);
                } else {
                    JOptionPane.showMessageDialog(panel, "������ ��� ���������� ��������.");
                }
            });
        });


        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                Student student = studentDB.getStudentById(id);
                if (student != null) {
                    showStudentForm(student, "������������� ��������", updatedStudent -> {
                        if (studentDB.updateStudent(updatedStudent)) {
                            JOptionPane.showMessageDialog(panel, "������� ��������!");
                            refreshInfo(tableModel);
                        } else {
                            JOptionPane.showMessageDialog(panel, "������ ��� ���������� ��������.");
                        }
                    });
                }
            }
        });


        deleteButton.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length > 0) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "�� �������, ��� ������ ������� ��������� ���������?",
                        "������������� ��������",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = true;

                    // ������� ��������� �� �� ID
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        int id = (int) tableModel.getValueAt(selectedRows[i], 0);
                        if (!studentDB.deleteStudent(id)) {
                            success = false;
                        }
                    }

                    if (success) {
                        JOptionPane.showMessageDialog(panel, "��������� �������� �������!");
                    } else {
                        JOptionPane.showMessageDialog(panel, "�� ������� ������� ��������� ���������.", "������", JOptionPane.ERROR_MESSAGE);
                    }

                    refreshInfo(tableModel);
                }
            }
        });

        nextPageButton.addActionListener(e -> {
            currentPage++;
            refreshInfo(tableModel);
        });

        prevPageButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshInfo(tableModel);
            }
        });

        refreshButton.addActionListener(e -> refreshInfo(tableModel));

        // ��������� ������
        buttonPanel.add(pageInfoLabel); // ����� ��������
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(prevPageButton);
        buttonPanel.add(nextPageButton);
        buttonPanel.add(refreshButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private static void addFilters(JPanel panel) {
        // ������ ����������
        JPanel filterPanel = new JPanel(new GridLayout(5, 3));
        filterPanel.setBorder(BorderFactory.createTitledBorder("�������"));

        // ��������� ��������
        setupFilter(gitComboBox, gitField);
        setupFilter(emailComboBox, emailField);
        setupFilter(phoneComboBox, phoneField);
        setupFilter(telegramComboBox, telegramField);

        // ��������� �������� ��������
        filterPanel.add(new JLabel("������� � ��������:"));
        filterPanel.add(nameField);
        filterPanel.add(new JLabel()); // �����������

        filterPanel.add(new JLabel("GitHub:"));
        filterPanel.add(gitComboBox);
        filterPanel.add(gitField);

        filterPanel.add(new JLabel("Email:"));
        filterPanel.add(emailComboBox);
        filterPanel.add(emailField);

        filterPanel.add(new JLabel("�������:"));
        filterPanel.add(phoneComboBox);
        filterPanel.add(phoneField);

        filterPanel.add(new JLabel("Telegram:"));
        filterPanel.add(telegramComboBox);
        filterPanel.add(telegramField);

        panel.add(filterPanel, BorderLayout.NORTH);
    }


    private static void setupFilter(JComboBox<String> comboBox, JTextField textField) {
        textField.setEnabled(false); // �� ��������� ���� ���������
        comboBox.addActionListener(e -> {
            // ���� �������� ������ ���� ������ "��"
            textField.setEnabled(Objects.equals(comboBox.getSelectedItem(), "��"));
        });
    }


    private static void refreshInfo(DefaultTableModel tableModel) {
        String nameFilter = nameField.getText().trim();
        SearchParam gitSearch = SearchParam.create(
                (String) Objects.requireNonNull(gitComboBox.getSelectedItem())
        );
        String gitFilter = gitField.getText().trim();
        SearchParam emailSearch = SearchParam.create(
                (String) Objects.requireNonNull(emailComboBox.getSelectedItem())
        );
        String emailFilter = emailField.getText().trim();

        SearchParam phoneSearch = SearchParam.create(
                (String) Objects.requireNonNull(phoneComboBox.getSelectedItem())
        );
        String phoneFilter = phoneField.getText().trim();

        SearchParam telegramSearch = SearchParam.create(
                (String) Objects.requireNonNull(telegramComboBox.getSelectedItem())
        );
        String telegramFilter = telegramField.getText().trim();

        StudentFilter studentFilter = new StudentFilter(
                nameFilter,
                gitFilter,
                emailFilter,
                phoneFilter,
                telegramFilter,
                gitSearch,
                phoneSearch,
                telegramSearch,
                emailSearch
        );

        // �������� ����� ���������� �������
        int totalItems = studentDB.getFilteredStudentCount(studentFilter);
        int lastPage = calculateLastPage(totalItems);
        // ���� ��������� ���, ��� ������� �������� ������, ��� ���������, �� ���������� �������� � �������������
        if (lastPage < currentPage) {
            currentPage = lastPage;
            refreshInfo(tableModel);
            return;
        }
        loadStudents(tableModel, studentFilter);

        // ��������� ��������� ������ � ����� ��������
        updatePageControls(lastPage);
    }



    private static void loadStudents(
            DefaultTableModel tableModel,
            StudentFilter studentFilter
    ) {
        tableModel.setRowCount(0); // ������� �������

        // �������� ������ ��������� � ������ ��������
        List<Student> students = studentDB.getFilteredStudentList(
                currentPage, PAGE_SIZE,
                studentFilter
        );

        for (Student student : students) {
            tableModel.addRow(new Object[] {
                    student.getId(),
                    student.getLastNameWithInitials(),
                    student.getGitInfo(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getTelegram(),
            });
        }
    }

    private static void updatePageControls(int lastPage) {

        // ���������� ������ ����� ��������
        pageInfoLabel.setText("��������: " + currentPage + " / " + lastPage);

        // ���������� ������ � ����������� �� ������� ��������
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < lastPage);
    }


    private static int calculateLastPage(int totalItems) {
        int page = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        return page == 0 ? 1 : page;
    }

    private static void showStudentForm(Student existingStudent, String title, Consumer<Student> onSave) {
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(7, 2));

        // ���� ��� ����� ������
        JTextField lastNameField = new JTextField(existingStudent != null ? existingStudent.getLastName() : "");
        JTextField firstNameField = new JTextField(existingStudent != null ? existingStudent.getFirstName() : "");
        JTextField middleNameField = new JTextField(existingStudent != null ? existingStudent.getMiddleName() : "");
        JTextField telegramField = new JTextField(existingStudent != null && existingStudent.getTelegram() != null ? existingStudent.getTelegram() : "");
        JTextField gitField = new JTextField(existingStudent != null && existingStudent.getGit() != null ? existingStudent.getGit() : "");
        JTextField emailField = new JTextField(existingStudent != null && existingStudent.getEmail() != null ? existingStudent.getEmail() : "");

        // ��������� ����������
        dialog.add(new JLabel("�������:"));
        dialog.add(lastNameField);

        dialog.add(new JLabel("���:"));
        dialog.add(firstNameField);

        dialog.add(new JLabel("��������:"));
        dialog.add(middleNameField);

        dialog.add(new JLabel("Telegram:"));
        dialog.add(telegramField);

        dialog.add(new JLabel("GitHub:"));
        dialog.add(gitField);

        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);

        // ������
        JButton saveButton = new JButton("���������");
        JButton cancelButton = new JButton("������");

        dialog.add(saveButton);
        dialog.add(cancelButton);

        // ����������� ������
        saveButton.addActionListener(e -> {
            // ������� ���������
            String lastName = lastNameField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String middleName = middleNameField.getText().trim();

            if (lastName.isEmpty() || firstName.isEmpty() || middleName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "�������, ��� � �������� ����������� ��� ����������!", "������", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ������� ��� ��������� ������ Student
            try {
                Student student = existingStudent != null ? existingStudent : new Student();
                student.setLastName(lastName);
                student.setFirstName(firstName);
                student.setMiddleName(middleName);
                student.setTelegram(telegramField.getText().trim());
                student.setGit(gitField.getText().trim());
                student.setEmail(emailField.getText().trim());
                student.validate();

                onSave.accept(student);
                dialog.dispose();
            } catch (ValidateException exception) {
                JOptionPane.showMessageDialog(
                        dialog,
                        exception.getMessage(),
                        "������",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }


}
