package com.mtabo.necta.parser


import com.mtabo.necta.models.StudentResult
import necta.utils.SubjectUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Element

object CseeAcseeParser {
    fun parseResults(rows: List<Element>): Flow<StudentResult.SecondaryStudentResult> = flow {
        for (tr in rows) {
            val tds = tr.select("td")
            when {
                tds.size >= 6 -> { // Has Candidate Name column
                    emit(
                        StudentResult.SecondaryStudentResult(
                            indexNo = tds[0].text().trim(),
                            sex = tds[1].text().trim(),
                            name = tds[2].text().trim(),
                            points = tds[3].text().trim(),
                            division = tds[4].text().trim(),
                            subjects = SubjectUtils.splitSubjectString(tds[5].text())
                        )
                    )
                }

                tds.size >= 5 -> { // Normal structure (no name column)
                    emit(
                        StudentResult.SecondaryStudentResult(
                            indexNo = tds[0].text().trim(),
                            sex = tds[1].text().trim(),
                            name = null,
                            points = tds[2].text().trim(),
                            division = tds[3].text().trim(),
                            subjects = SubjectUtils.splitSubjectString(tds[4].text())
                        )
                    )
                }
            }
        }
    }
}