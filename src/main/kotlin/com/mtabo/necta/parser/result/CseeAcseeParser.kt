package com.mtabo.necta.parser.result


import com.mtabo.necta.models.StudentResult
import com.mtabo.necta.utils.TableUtils
import necta.utils.SubjectUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


object CseeAcseeParser {

    fun parseResults(doc: Document): List<StudentResult.CseeOrAcseeResult> {

        val rows: List<Element> = TableUtils.fetchCseeResultTable(doc)
        val results = mutableListOf<StudentResult.CseeOrAcseeResult>()

        for (tr in rows) {
            val tds = tr.select("td")
            when {
                tds.size >= 6 -> { // Has Candidate Name column
                    results.add(
                        StudentResult.CseeOrAcseeResult(
                            indexNo = tds.textAt( 0),
                            sex = tds.textAt( 1),
                            name = tds.textAt( 2),
                            points = tds.textAt( 3),
                            division = tds.textAt( 4),
                            subjectDetails = SubjectUtils.splitSubjectString(tds[5].text())
                        )
                    )
                }

                tds.size >= 5 -> { // Normal structure (no name column)
                    results.add(
                        StudentResult.CseeOrAcseeResult(
                            indexNo = tds.textAt( 0),
                            sex = tds.textAt( 1),
                            name = null,
                            points = tds.textAt( 2),
                            division = tds.textAt( 3),
                            subjectDetails = SubjectUtils.splitSubjectString(tds[4].text())
                        )
                    )
                }
            }
        }

        return results
    }

    fun Elements.textAt(index: Int): String =
        this[index].text().trim()
}