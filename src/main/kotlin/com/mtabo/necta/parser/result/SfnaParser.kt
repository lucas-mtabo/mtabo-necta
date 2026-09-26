package com.mtabo.necta.parser.result


import com.mtabo.necta.models.StudentResult
import com.mtabo.necta.utils.TableUtils.fetchPsleResultTable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import necta.utils.SubjectUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object SfnaParser {
    fun parseResults(
        doc: Document,
        year: Int
    ): List<StudentResult.PsleAndSfnaResult> {
        val rows = fetchPsleResultTable(doc)

        return if (year in 2015..2021) {
            parseLegacySfna(rows)
        } else {
            parseModernSfna(rows)
        }
    }

    fun parseModernSfna(rows:  List<Element>): List<StudentResult.PsleAndSfnaResult> {
        val candidates = mutableListOf<StudentResult.PsleAndSfnaResult>()
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.size >= 5) {
                val (grade, subjects) = SubjectUtils.splitGradeFromSubjects(tds[4])
                candidates.add(
                    StudentResult.PsleAndSfnaResult(
                        indexNo = tds[0].text().trim(),
                        premNo = tds[1].text().trim(),
                        sex = tds[2].text().trim(),
                        name = tds[3].text().trim(),
                        grade = grade,
                        subjectDetails = subjects
                    )
                )
            } else if (tds.size >= 4) {
                // does not have candidate name column
                val (grade, subjects) = SubjectUtils.splitGradeFromSubjects(tds[3])
                candidates.add(
                    StudentResult.PsleAndSfnaResult(
                        indexNo = tds[0].text().trim(),
                        premNo = tds[1].text().trim(),
                        sex = tds[2].text().trim(),
                        name = null,
                        grade = grade,
                        subjectDetails = subjects
                    )
                )
            }
        }
        return candidates
    }
    fun parseLegacySfna(rows:  List<Element>): List<StudentResult.PsleAndSfnaResult> {
        val candidates = mutableListOf<StudentResult.PsleAndSfnaResult>()
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.size >= 5) {
                // Has separate average grade column
                candidates.add(
                    StudentResult.PsleAndSfnaResult(
                        premNo = null,
                        indexNo = tds[0].text().trim(),
                        sex = tds[1].text().trim(),
                        name = tds[2].text().trim(),
                        subjectDetails = SubjectUtils.splitSubjectString(tds[3].text()),
                        grade = tds[4].text().trim()
                    )
                )
            }
        }
        return candidates
    }
}

