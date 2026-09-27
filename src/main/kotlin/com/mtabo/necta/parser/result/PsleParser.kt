package com.mtabo.necta.parser.result

import com.mtabo.necta.models.StudentResult
import com.mtabo.necta.utils.TableUtils.fetchPsleResultTable
import com.mtabo.necta.utils.SubjectUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object PsleParser {
    fun parseResults(doc: Document , year: Int): List<StudentResult.PsleAndSfnaResult> {

        val rows: List<Element> = fetchPsleResultTable(doc)
        val results = mutableListOf<StudentResult.PsleAndSfnaResult>()

        for (tr in rows) {
            val tds = tr.select("td")
            when {
                tds.size >= 4 && year > 2023 -> { // No candidate name
                    val (grade, subjects) = SubjectUtils.splitGradeFromSubjects(tds[3])
                    results.add(
                        StudentResult.PsleAndSfnaResult(
                            indexNo = tds[0].text().trim(),
                            premNo = tds[1].text().trim(),
                            sex = tds[2].text().trim(),
                            grade = grade,
                            subjectDetails = subjects,
                            name = null,
                        )
                    )
                }

                tds.size >= 5 -> { // Has premNo column
                    val (grade, subjects) = SubjectUtils.splitGradeFromSubjects(tds[4])
                    results.add(
                        StudentResult.PsleAndSfnaResult(
                            indexNo = tds[0].text().trim(),
                            premNo = tds[1].text().trim(),
                            sex = tds[2].text().trim(),
                            name = tds[3].text().trim(),
                            grade = grade,
                            subjectDetails = subjects
                        )
                    )
                }

                tds.size >= 4 -> { // No premNo column
                    val (grade, subjects) = SubjectUtils.splitGradeFromSubjects(tds[3])
                    results.add(
                        StudentResult.PsleAndSfnaResult(
                            premNo = null,
                            indexNo = tds[0].text().trim(),
                            sex = tds[1].text().trim(),
                            name = tds[2].text().trim(),
                            grade = grade,
                            subjectDetails = subjects
                        )
                    )
                }
            }
        }

        return results
    }
}
