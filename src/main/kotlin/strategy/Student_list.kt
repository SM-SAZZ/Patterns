package org.sazz.strategy

import org.sazz.adapter.StudentListInterface
import org.sazz.dto.StudentFilter
import org.sazz.pattern.student.Data_list_student_short
import org.sazz.student.Student

class Student_list(private val studentSource: StudentListInterface) {

    var studentFilter: StudentFilter? = null

    fun getStudentById(id: Int): Student? {
        return studentSource.getStudentById(id)
    }

    fun getKNStudentShortList(k: Int, n: Int, studentFilter: StudentFilter? = null): Data_list_student_short {
        this.studentFilter = studentFilter
        if (studentFilter != null) {
            this.studentSource.initStudentFilter(studentFilter)
        }
        return studentSource.getKNStudentShortList(k, n)
    }

    fun addStudent(student: Student): Int {
        return studentSource.addStudent(student)
    }

    fun updateStudent(student: Student): Boolean {
        return studentSource.updateStudent(student)
    }

    fun deleteStudent(id: Int): Boolean {
        return studentSource.deleteStudent(id)
    }

    fun getStudentCount(): Int {
        return studentSource.getStudentCount()
    }
}