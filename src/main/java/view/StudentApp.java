package view;

import org.sazz.dto.StudentFilter;
import org.sazz.enums.SearchParam;
import org.sazz.strategy.Student_list_DB;
import org.sazz.student.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class StudentApp {
    private static final int PAGE_SIZE = 20;
    private static int currentPage = 1;
    private static final Student_list_DB studentDB = new Student_list_DB();

    /** Ïîëÿ ôèëüòðàöèè **/
    private static final JTextField nameField = new JTextField();

    private static final JComboBox<String> gitComboBox = new JComboBox<>(new String[] { "Íå âàæíî", "Äà", "Íåò" });
    private static final JTextField gitField = new JTextField();

    private static final JTextField emailField = new JTextField();
    private static final JComboBox<String> emailComboBox = new JComboBox<>(new String[] { "Íå âàæíî", "Äà", "Íåò" });

    private static final JTextField phoneField = new JTextField();
    private static final JComboBox<String> phoneComboBox = new JComboBox<>(new String[] { "Íå âàæíî", "Äà", "Íåò" });

    private static final JTextField telegramField = new JTextField();
    private static final JComboBox<String> telegramComboBox = new JComboBox<>(new String[] { "Íå âàæíî", "Äà", "Íåò" });

    /** Ýëåìåíòû ïàãèíàöèè **/
    private static final JLabel pageInfoLabel = new JLabel("Ñòðàíèöà: 1 / ?");
    private static final JButton prevPageButton = new JButton("Ïðåäûäóùàÿ");
    private static final JButton nextPageButton = new JButton("Ñëåäóþùàÿ");

    /** Êíîïêè óïðàâëåíèÿ **/
    private static final JButton refreshButton = new JButton("Îáíîâèòü");
    private static final JButton addButton = new JButton("Äîáàâèòü");
    private static final JButton editButton = new JButton("Èçìåíèòü");
    private static final JButton deleteButton = new JButton("Óäàëèòü");


    public static void create() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Student Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.add("Ñïèñîê ñòóäåíòîâ", createStudentTab());
            tabbedPane.add("Âêëàäêà 2", new JLabel("Ñîäåðæèìîå âêëàäêè 2"));
            tabbedPane.add("Âêëàäêà 3", new JLabel("Ñîäåðæèìîå âêëàäêè 3"));

            frame.add(tabbedPane);
            frame.setVisible(true);
        });
    }

    private static JPanel createStudentTab() {
        JPanel panel = new JPanel(new BorderLayout());

        addFilters(panel);

        // Òàáëèöà ñòóäåíòîâ
        String[] columnNames = { "ID", "Ôàìèëèÿ è èíèöèàëû", "Ãèò", "Email", "Òåëåôîí", "Telegram" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Çàïðåò ðåäàêòèðîâàíèÿ
            }
        };
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Ïàíåëü óïðàâëåíèÿ
        JPanel buttonPanel = new JPanel();

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean rowSelected = table.getSelectedRow() >= 0;
            editButton.setEnabled(rowSelected);
            deleteButton.setEnabled(rowSelected);
        });

        // Çàãðóçêà äàííûõ â òàáëèöó
        refreshInfo(tableModel);

        // Îáðàáîò÷èêè êíîïîê
        addButton.addActionListener(e -> {
            // Ìîêîâàÿ ôîðìà äîáàâëåíèÿ ñòóäåíòà
            Student student = new Student(0, "Èâàí", "Èâàíîâ", "Èâàíîâè÷", "@ivan", "git", "123-456", "ivan@mail.com");
            int id = studentDB.addStudent(student);
            if (id > 0) {
                JOptionPane.showMessageDialog(panel, "Ñòóäåíò äîáàâëåí!");
                refreshInfo(tableModel);
            } else {
                JOptionPane.showMessageDialog(panel, "Îøèáêà ïðè äîáàâëåíèè ñòóäåíòà.");
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                Student student = studentDB.getStudentById(id);
                if (student != null) {
                    student.setFirstName("Îáíîâëåíî");
                    if (studentDB.updateStudent(student)) {
                        JOptionPane.showMessageDialog(panel, "Ñòóäåíò îáíîâëåí!");
                        refreshInfo(tableModel);
                    } else {
                        JOptionPane.showMessageDialog(panel, "Îøèáêà ïðè îáíîâëåíèè ñòóäåíòà.");
                    }
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                if (studentDB.deleteStudent(id)) {
                    JOptionPane.showMessageDialog(panel, "Ñòóäåíò óäàëåí!");
                    refreshInfo(tableModel);
                } else {
                    JOptionPane.showMessageDialog(panel, "Îøèáêà ïðè óäàëåíèè ñòóäåíòà.");
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

        // Äîáàâëÿåì êíîïêè
        buttonPanel.add(pageInfoLabel); // Ìåòêà ñòðàíèöû
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
        // Ïàíåëü ôèëüòðàöèè
        JPanel filterPanel = new JPanel(new GridLayout(5, 3));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Ôèëüòðû"));

        // Íàñòðîéêà ôèëüòðîâ
        setupFilter(gitComboBox, gitField);
        setupFilter(emailComboBox, emailField);
        setupFilter(phoneComboBox, phoneField);
        setupFilter(telegramComboBox, telegramField);

        // Äîáàâëÿåì ýëåìåíòû ôèëüòðîâ
        filterPanel.add(new JLabel("Ôàìèëèÿ è èíèöèàëû:"));
        filterPanel.add(nameField);
        filterPanel.add(new JLabel()); // Çàïîëíèòåëü

        filterPanel.add(new JLabel("GitHub:"));
        filterPanel.add(gitComboBox);
        filterPanel.add(gitField);

        filterPanel.add(new JLabel("Email:"));
        filterPanel.add(emailComboBox);
        filterPanel.add(emailField);

        filterPanel.add(new JLabel("Òåëåôîí:"));
        filterPanel.add(phoneComboBox);
        filterPanel.add(phoneField);

        filterPanel.add(new JLabel("Telegram:"));
        filterPanel.add(telegramComboBox);
        filterPanel.add(telegramField);

        panel.add(filterPanel, BorderLayout.NORTH);
    }


    private static void setupFilter(JComboBox<String> comboBox, JTextField textField) {
        textField.setEnabled(false); // Ïî óìîë÷àíèþ ïîëå âûêëþ÷åíî
        comboBox.addActionListener(e -> {
            // Ïîëå äîñòóïíî òîëüêî åñëè âûáðàí "Äà"
            textField.setEnabled(Objects.equals(comboBox.getSelectedItem(), "Äà"));
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

        // Çàãðóæàåì äàííûå è ïîëó÷àåì îáùåå êîëè÷åñòâî çàïèñåé
        int totalItems = studentDB.getFilteredStudentCount(studentFilter);
        loadStudents(tableModel, studentFilter);

        // Îáíîâëÿåì ñîñòîÿíèå êíîïîê è ìåòêè ñòðàíèöû
        updatePageControls(totalItems);
    }



    private static void loadStudents(
            DefaultTableModel tableModel,
            StudentFilter studentFilter
    ) {
        tableModel.setRowCount(0); // Î÷èùàåì òàáëèöó

        // Ïîëó÷àåì ñïèñîê ñòóäåíòîâ ñ ó÷åòîì ôèëüòðîâ
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

    private static void updatePageControls(int totalItems) {
        int lastPage = calculateLastPage(totalItems);

        // Îáíîâëåíèå òåêñòà ìåòêè ñòðàíèöû
        pageInfoLabel.setText("Ñòðàíèöà: " + currentPage + " / " + lastPage);

        // Îòêëþ÷åíèå êíîïîê â çàâèñèìîñòè îò òåêóùåé ñòðàíèöû
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < lastPage);
    }


    private static int calculateLastPage(int totalItems) {
        int page = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        return page == 0 ? 1 : page;
    }


}