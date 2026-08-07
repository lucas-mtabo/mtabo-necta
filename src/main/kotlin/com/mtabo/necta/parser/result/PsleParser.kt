package com.mtabo.necta.parser

import com.mtabo.necta.models.StudentResult
import necta.utils.SubjectUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Element

object PsleParser {
    fun parseResults(rows: List<Element>, year: Int): Flow<StudentResult.PrimaryStudentResult> = flow {
        for (tr in rows) {
            val tds = tr.select("td")
            when {
                tds.size >= 4 && year > 2023 -> { // No candidate name
                    val (grade, subjects) = SubjectUtils.splitGradeFromSubjects(tds[3])
                    emit(
                        StudentResult.PrimaryStudentResult(
                            indexNo = tds[0].text().trim(),
                            premNo = tds[1].text().trim(),
                            sex = tds[2].text().trim(),
                            grade = grade,
                            subjects = subjects,
                            name = null,
                        )
                    )
                }

                tds.size >= 5 -> { // Has premNo column
                    val (grade, subjects) = SubjectUtils.splitGradeFromSubjects(tds[4])
                    emit(
                        StudentResult.PrimaryStudentResult(
                            indexNo = tds[0].text().trim(),
                            premNo = tds[1].text().trim(),
                            sex = tds[2].text().trim(),
                            name = tds[3].text().trim(),
                            grade = grade,
                            subjects = subjects
                        )
                    )
                }

                tds.size >= 4 -> { // No premNo column
                    val (grade, subjects) = SubjectUtils.splitGradeFromSubjects(tds[3])
                    emit(
                        StudentResult.PrimaryStudentResult(
                            premNo = null,
                            indexNo = tds[0].text().trim(),
                            sex = tds[1].text().trim(),
                            name = tds[2].text().trim(),
                            grade = grade,
                            subjects = subjects
                        )
                    )
                }
            }
        }
    }
}
