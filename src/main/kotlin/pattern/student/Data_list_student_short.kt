package org.sazz.pattern.student

import org.sazz.logger.SimpleLogger
import org.sazz.observer.ObserveSubject
import org.sazz.observer.Observer
import org.sazz.pattern.Data_list
import org.sazz.student.Student
import org.sazz.student.Student_short

class Data_list_student_short(students: List<Student_short>) : Data_list<Student_short>(students), ObserveSubject {

    constructor(studentsList: List<Student>, count: Int) : this(studentsList.map { Student_short(it) }) {
        SimpleLogger.info(studentsList.toString())
    }

    override val observers: MutableList<Observer> = mutableListOf()

    override fun getEntityFields(): List<String> {
        return listOf("ID", "Имя", "Гит", "Контакт")
    }

    override fun getDataRow(entity: Student_short): List<Any> {
        return listOf(entity.id, entity.lastNameInitials, entity.git, entity.contact) as List<Any>
    }

}