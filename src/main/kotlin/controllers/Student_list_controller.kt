package org.sazz.controllers

import org.sazz.adapter.StudentListInterface
import org.sazz.dto.StudentFilter
import org.sazz.pattern.student.Data_list_student_short
import org.sazz.strategy.Student_list
import view.MainWindowView

class Student_list_controller(studentSourceData: StudentListInterface, private var view: MainWindowView) {


    private var studentsList: Student_list = Student_list(studentSourceData);
    private var dataListStudentShort: Data_list_student_short? = null;

    fun setView(view: MainWindowView) {
        this.view = view
    }

    fun refresh_data(pageSize: Int, page: Int, studentFilter: StudentFilter) {
        dataListStudentShort = studentsList.getKNStudentShortList(k = pageSize, n = page, studentFilter = studentFilter);
        view.setDataList(dataListStudentShort)
        dataListStudentShort?.addObserver(view)
        dataListStudentShort?.notify()
    }

}