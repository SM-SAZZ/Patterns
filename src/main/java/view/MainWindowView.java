package view;

import org.sazz.controllers.StudentCreateController;
import org.sazz.controllers.StudentUpdateController;
import org.sazz.controllers.Student_list_controller;
import org.sazz.dto.StudentFilter;
import org.sazz.logger.SimpleLogger;
import org.sazz.pattern.student.Data_list_student_short;
import org.sazz.student.Student;
import org.sazz.student.Student_short;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainWindowView implements ViewInterface {

    /** ���������� � ������� ��������  **/
    private static final int PAGE_SIZE = 20;
    private static int currentPage = 1;

    /** ���������� **/
    private Student_list_controller controller;

    public void setController(Student_list_controller controller) {
        this.controller = controller;
    }

    private Data_list_student_short dataList;

    public void setDataList(Data_list_student_short dataList) {
        this.dataList = dataList;
    }

    /**
     * �������
     */
    private DefaultTableModel tableModel;

    /** ���� ���������� **/
    private final JTextField nameField = new JTextField();

    private final JComboBox<String> gitComboBox = new JComboBox<>(new String[] { "�� �����", "��", "���" });
    private final JTextField gitField = new JTextField();

    private final JTextField emailField = new JTextField();
    private final JComboBox<String> emailComboBox = new JComboBox<>(new String[] { "�� �����", "��", "���" });

    private final JTextField phoneField = new JTextField();
    private final JComboBox<String> phoneComboBox = new JComboBox<>(new String[] { "�� �����", "��", "���" });

    private final JTextField telegramField = new JTextField();
    private final JComboBox<String> telegramComboBox = new JComboBox<>(new String[] { "�� �����", "��", "���" });

    /** �������� ��������� **/
    private final JLabel pageInfoLabel = new JLabel("��������: 1 / ?");
    private final JButton prevPageButton = new JButton("����������");
    private final JButton nextPageButton = new JButton("���������");

    /** ������ ���������� **/
    private final JButton refreshButton = new JButton("��������");
    private final JButton addButton = new JButton("��������");
    private final JButton editButton = new JButton("��������");
    private final JButton deleteButton = new JButton("�������");

    public MainWindowView() {}

    public void create(Student_list_controller controller) {
        SimpleLogger.info("������������� ���� ���������� ����������...");
        setController(controller);
        controller.firstInitDataList();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Student Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.add("������ ���������", createStudentTab());

            frame.add(tabbedPane);
            frame.setVisible(true);
            SimpleLogger.info("���� ���������� ������� ����������������.");
            update();
        });
    }

    private JPanel createStudentTab() {
        SimpleLogger.info("�������� ������� '������ ���������'...");
        JPanel panel = new JPanel(new BorderLayout());
//        addFilters(panel);

        // ������� ���������
        String[] columnNames = dataList.getEntityFields().toArray(new String[0]);
        tableModel = new DefaultTableModel(columnNames, 0) {
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

        addButton.addActionListener(e -> {
            SimpleLogger.info("������ ������ '��������'.");
            StudentCreateController studentCreateController = new StudentCreateController(this.controller);
            StudentFormModal modal = new StudentFormModal();
            modal.controller = studentCreateController;
            modal.create(null, "������� ����� ������");
        });

        editButton.addActionListener(e -> {
            SimpleLogger.info("������ ������ '��������'.");
            StudentUpdateController studentUpdateController = new StudentUpdateController(this.controller);
            StudentFormModal modal = new StudentFormModal();
            modal.controller = studentUpdateController;
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                Student student = studentUpdateController.getStudentById(id);
                if (student == null) {
                    SimpleLogger.error("������� � ID " + id + " �� ������.");
                    showError("������ �� ���� �������!");
                }
                modal.create(student, "�������� ������");
            }
        });

        deleteButton.addActionListener(e -> {
            SimpleLogger.info("������ ������ '�������'.");
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
                        SimpleLogger.info("������� �������� �������� � ID: " + id);
                        if (!controller.deleteStudent(id)) {
                            SimpleLogger.error("�� ������� ������� �������� � ID: " + id);
                            success = false;
                        }
                    }

                    if (success) {
                        SimpleLogger.info("��������� �������� ������� �������.");
                        JOptionPane.showMessageDialog(panel, "��������� �������� �������!");
                    } else {
                        SimpleLogger.error("�� ������� ������� ��������� ���������.");
                        JOptionPane.showMessageDialog(panel, "�� ������� ������� ��������� ���������.", "������", JOptionPane.ERROR_MESSAGE);
                    }
                    controller.refresh_data();
                }
            }
        });

        nextPageButton.addActionListener(e -> {
            currentPage++;
            SimpleLogger.info("������� �� ��������� ��������: " + currentPage);
            controller.refresh_data(PAGE_SIZE, currentPage, getCurrentFilter());
        });

        prevPageButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                SimpleLogger.info("������� �� ���������� ��������: " + currentPage);
                controller.refresh_data(PAGE_SIZE, currentPage, getCurrentFilter());
            }
        });

        refreshButton.addActionListener(e -> controller.refresh_data(PAGE_SIZE, currentPage, null));

        // ��������� ������
        SimpleLogger.info("���������� ������ �������.");
        buttonPanel.add(pageInfoLabel); // ����� ��������
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(prevPageButton);
        buttonPanel.add(nextPageButton);
        buttonPanel.add(refreshButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        SimpleLogger.info("������� '������ ���������' ������� �������.");
        return panel;
    }

    @Override
    public void update() {
        SimpleLogger.info("���������� ������� ���������.");
        set_table_params();
        set_table_data();
    }

    private void set_table_params() {
        List<String> newColumnNames = dataList.getEntityFields();
        SimpleLogger.info("�������� ������ ��������� � �������.");
        tableModel.setColumnIdentifiers(newColumnNames.toArray());

        // �������� ���������� �������
        int lastPage = dataList.getPagination().getTotalPages();

        // ���� ��������� ���, ��� ������� �������� ������, ��� ���������, �� ���������� �������� � �������������
        if (lastPage < currentPage) {
            currentPage = lastPage;
            controller.refresh_data(PAGE_SIZE, currentPage, getCurrentFilter());
            return;
        }
        SimpleLogger.info("������ ��������� ������� ���������.");

        updatePageControls(lastPage);
    }

    private void updatePageControls(int lastPage) {

        // ���������� ������ ����� ��������
        pageInfoLabel.setText("��������: " + currentPage + " / " + lastPage);

        // ���������� ������ � ����������� �� ������� ��������
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < lastPage);
    }

    private StudentFilter getCurrentFilter() {
        return null;
    }

    private void set_table_data() {
        tableModel.setRowCount(0); // ������� �������

        // �������� ������ ��������� � ������ ��������
        List<Student_short> students = dataList.toList();

        for (Student_short student : students) {
            tableModel.addRow(new Object[] {
                    student.getId(),
                    student.getLastNameWithInitials(),
                    student.getGitInfo(),
                    student.getContactInfo(),
            });
        }
    }

    public void showError(String message) {
        JDialog dialog = new JDialog((Frame) null, "������", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new GridLayout(7, 2));
        JOptionPane.showMessageDialog(dialog, "��������� �������������� ������: " + message, "������", JOptionPane.ERROR_MESSAGE);
    }
}
