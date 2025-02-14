package org.sazz.adapter

import org.sazz.dto.StudentFilter
import org.sazz.pattern.student.Data_list_student_short
import org.sazz.strategy.Student_list_file
import org.sazz.student.Student

class StudentListFileAdapter(
    private val studentListFile: Student_list_file,
    var studentFilter: StudentFilter? = null
) : StudentListInterface {

    override fun getStudentById(id: Int): Student? {
        return try {
            studentListFile.findById(id)
        } catch (e: NoSuchElementException) {
            null
        }
    }

    override fun getKNStudentShortList(k: Int, n: Int): Data_list_student_short {
        studentListFile.studentFilter = this.studentFilter
        return studentListFile.get_k_n_student_short_list(n=n,k=k) as Data_list_student_short
    }

    override fun addStudent(student: Student): Int {
        studentListFile.add(student)
        return student.id
    }

    override fun initStudentFilter(studentFilter: StudentFilter) {
        this.studentFilter = studentFilter
    }

    override fun updateStudent(student: Student): Boolean {
        getStudentById(student.id) ?: return false
        studentListFile.replaceById(student, student.id)
        return true
    }

    override fun deleteStudent(id: Int): Boolean {
        getStudentById(id) ?: return false
        studentListFile.removeById(id)
        return true
    }

    override fun getStudentCount(): Int {
        return studentListFile.get_student_short_count()
    }
}