package org.sazz.controllers

import org.sazz.strategy.Student_list
import org.sazz.strategy.Student_list_DB
import org.sazz.student.Student

class StudentCreateController(
    studentListController: Student_list_controller,
    studentList: Student_list
) : StudentFormController(studentListController,studentList) {

    constructor(studentListController: Student_list_controller): this(studentListController, studentListController.getStudentsList())

    override fun saveProcessedStudent(student: Student, id: Int?): String {
        val id = studentList.addStudent(student)
        if (id > 0) {
            studentListController.refresh_data()
            return "Студент добавлен!"
        } else {
            return "Ошибка при добавлении студента."
        }
    }

    override fun getAccessFields(): ArrayList<String> {
        return arrayListOf("Фамилия", "Имя", "Отчество", "Telegram", "GitHub", "Email")
    }
}