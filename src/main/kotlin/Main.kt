package org.sazz

import org.sazz.pattern.student.Data_list_student_short
import org.sazz.strategy.Student_list
import org.sazz.strategy.studentFileProcessor.StudentJsonFileProcessor
import org.sazz.strategy.studentFileProcessor.StudentYamlFileProcessor
import org.sazz.student.Student;
import org.sazz.student.Student_short

fun main() {
//    dataTableTest();
    studentTest();
}

fun studentTest() {
    val filePath = "src/files/students.yaml"
    val studentList = Student_list(filePath, StudentYamlFileProcessor())

    studentList.add(Student(0, "NewJohn", "John", "JOGN", email = "jhon@hmail.ru"))
    studentList.fileProcessor = StudentJsonFileProcessor()
    studentList.write_to_file("src/files", "students.json")
}

fun checkValidStudent(student: Student) {
    val studentName = student.lastName
    if (student.validate()) {
        println("Student $studentName is valid");
    } else {
        println("Student $studentName is not valid");
    }
}

fun dataTableTest() {
    val students = listOf(
        Student_short(1, "Иванов И.И.", "https://github.com/ivanov/Patterns", "Telegram: @ivanich"),
        Student_short(2, "Петров П.П.", "https://github.com/petrov/Patterns", "Email: pudge@gmail.com"),
        Student_short(3, "Сидоров С.С.", "https://github.com/sidorov/Patterns", "Phone: +7 (999) 222-11-11")
    )

    val studentList = Data_list_student_short(students)

    println("Названия столбцов: ${studentList.getNames()}")

    studentList.select(0)
    studentList.select(2)
    println("Выбранные элементы: ${studentList.getSelected()}")

    val dataTable = studentList.getData()
    println("Количество строк: ${dataTable.getRowCount()}")
    println("Количество столбцов: ${dataTable.getColumnCount()}")

// Вывод данных таблицы
    for (i in 0 until dataTable.getRowCount()) {
        for (j in 0 until dataTable.getColumnCount()) {
            print("${dataTable.getElement(i, j)} ")
        }
        println()
    }
}