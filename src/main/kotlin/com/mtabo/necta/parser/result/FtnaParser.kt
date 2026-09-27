package com.mtabo.necta.parser.result

import com.mtabo.necta.models.StudentResult
import com.mtabo.necta.utils.TableUtils.fetchCseeResultTable
import com.mtabo.necta.utils.TableUtils.fetchLegacyFtnaResultTable
import com.mtabo.necta.utils.TableUtils.getLegacyTableHeaders
import com.mtabo.necta.utils.SubjectUtils
import com.mtabo.necta.utils.SubjectUtils.combineSubjectAndScore
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object FtnaParser {

    /**
     * Parses candidate results for FTNA exam.
     * Handles both cases: when Candidate Name column exists or not.
     */
    fun parseResults(
        doc: Document,
        year: Int
    ): List<StudentResult.FtnaStudentResult> =
        if (year in 2014..2021) {
            val headers = getLegacyTableHeaders(doc)
            val rows = fetchLegacyFtnaResultTable(doc)

            parseLegacyResults(rows, headers)
        } else {
            val rows = fetchCseeResultTable(doc)

            parseModernResults(rows)
        }

    fun parseModernResults(rows: List<Element>): List<StudentResult.FtnaStudentResult> {
        val candidates = mutableListOf<StudentResult.FtnaStudentResult>()
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.isEmpty()) continue

            if (tds.size >= 7) {

                val subjects = tds.getOrNull(6)?.text()?.trim() ?: ""
                val subjectList = SubjectUtils.splitSubjectString(subjects)

                candidates.add(
                    StudentResult.FtnaStudentResult(
                        premNo = tds.getOrNull(1)?.text()?.trim(),
                        indexNo = tds.getOrNull(0)?.text()?.trim() ?: "",
                        name = tds.getOrNull(2)?.text()?.trim() ?: "",
                        sex = tds.getOrNull(3)?.text()?.trim() ?: "",
                        points = tds.getOrNull(4)?.text()?.trim() ?: "",
                        division = tds.getOrNull(5)?.text()?.trim() ?: "",
                        subjectDetails = subjectList
                    )
                )
            } else if (tds.size >= 6) {

                val subjects = tds.getOrNull(5)?.text()?.trim() ?: ""
                val subjectList = SubjectUtils.splitSubjectString(subjects)

                candidates.add(
                    StudentResult.FtnaStudentResult(
                        premNo = tds.getOrNull(1)?.text()?.trim(),
                        indexNo = tds.getOrNull(0)?.text()?.trim() ?: "",
                        name = null,
                        sex = tds.getOrNull(2)?.text()?.trim() ?: "",
                        points = tds.getOrNull(3)?.text()?.trim() ?: "",
                        division = tds.getOrNull(4)?.text()?.trim() ?: "",
                        subjectDetails = subjectList
                    )
                )
            }
        }

        return candidates
    }

    fun parseLegacyResults(rows: List<Element>, headers: List<String>): List<StudentResult.FtnaStudentResult> {
        val candidates = mutableListOf<StudentResult.FtnaStudentResult>()

        var startParsing = false

        for (row in rows) {
            val cells = row.select("td")
            if (cells.isEmpty() || cells.size < headers.size) continue

            val rowText = row.text().trim().uppercase()

            // Detect the header start
            if (!startParsing && "CNO" in rowText) {
                startParsing = true
                continue
            }

            if (!startParsing) continue
            if ("CNO" in rowText) break // stop if another header found

            // Normalize all cell texts once, to remove redudant .....
            //val normalized = cells.map { it.text().replace(Regex("\\s+"), " ").replace(".", "").trim() }
            val normalized = cells.map { cell ->
                val raw = cell.text().replace(Regex("\\s+"), " ").trim()

                if (raw.matches(Regex("""\d+(\.\d+)?"""))) {
                    // It's a number like 4 or 4.4 → keep as is
                    raw
                } else {
                    // Likely header or subject → remove dots
                    raw.replace(".", "")
                }
            }

            if (normalized.isEmpty() || normalized[0].isEmpty()) continue

            val grades = combineSubjectAndScore(headers, normalized)

            candidates.add(
                StudentResult.FtnaStudentResult(
                    premNo = null,
                    indexNo = normalized.getOrNull(0) ?: "",
                    name = normalized.getOrNull(2) ?: "",
                    sex = normalized.getOrNull(3) ?: "",
                    points = normalized.getOrNull(normalized.size - 2) ?: "",
                    division = normalized.getOrNull(normalized.size - 1) ?: "",
                    subjectDetails = grades
                )
            )
        }

        return candidates
    }
}

/**
 *
 * there is a case where student result rows in table are incomplete compared to others
 * s0015 form two index number 2
 */