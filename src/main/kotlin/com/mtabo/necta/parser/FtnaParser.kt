package com.mtabo.necta.parser

import com.mtabo.necta.models.StudentResult
import com.mtabo.necta.utils.TableUtils.fetchCseeResultTable
import com.mtabo.necta.utils.TableUtils.fetchLegacyFtnaResultTable
import com.mtabo.necta.utils.TableUtils.getLegacyTableHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import necta.utils.SubjectUtils
import necta.utils.SubjectUtils.combineSubjectAndScore
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
    ): Flow<StudentResult.FtnaResult> = flow {
        if (year in 2014..2021) {
            val headers = getLegacyTableHeaders(doc)
            val rows = fetchLegacyFtnaResultTable(doc)
            for (row in parseLegacyResults(rows, headers)) emit(row)
        } else {
            val rows = fetchCseeResultTable(doc)
            for (row in parseModernResults(rows)) emit(row)
        }
    }

    fun parseModernResults(rows: List<Element>): List<StudentResult.FtnaResult> {
        val candidates = mutableListOf<StudentResult.FtnaResult>()
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.isEmpty()) continue

            if (tds.size >= 7) {
                val cno = tds.getOrNull(0)?.text()?.trim() ?: ""
                val premNo = tds.getOrNull(1)?.text()?.trim()
                val name = tds.getOrNull(2)?.text()?.trim() ?: ""
                val sex = tds.getOrNull(3)?.text()?.trim() ?: ""
                val pts = tds.getOrNull(4)?.text()?.trim() ?: ""
                val div = tds.getOrNull(5)?.text()?.trim() ?: ""
                val subjectsRaw = tds.getOrNull(6)?.text()?.trim() ?: ""

                val subjectList = SubjectUtils.splitSubjectString(subjectsRaw)

                candidates.add(
                    StudentResult.FtnaResult(
                        premNo = premNo,
                        indexNo = cno,
                        name = name,
                        sex = sex,
                        points = pts,
                        division = div,
                        subjects = subjectList
                    )
                )
            } else if (tds.size >= 6) {
                val cno = tds.getOrNull(0)?.text()?.trim() ?: ""
                val premNo = tds.getOrNull(1)?.text()?.trim()
                val sex = tds.getOrNull(2)?.text()?.trim() ?: ""
                val pts = tds.getOrNull(3)?.text()?.trim() ?: ""
                val div = tds.getOrNull(4)?.text()?.trim() ?: ""
                val subjectsRaw = tds.getOrNull(5)?.text()?.trim() ?: ""

                val subjectList = SubjectUtils.splitSubjectString(subjectsRaw)

                candidates.add(
                    StudentResult.FtnaResult(
                        premNo = premNo,
                        indexNo = cno,
                        name = null,
                        sex = sex,
                        points = pts,
                        division = div,
                        subjects = subjectList
                    )
                )
            }
        }

        return candidates
    }

    fun parseLegacyResults(rows: List<Element>, headers: List<String>): List<StudentResult.FtnaResult> {
        val candidates = mutableListOf<StudentResult.FtnaResult>()

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

            val cno = normalized.getOrNull(0) ?: ""
            val name = normalized.getOrNull(2) ?: ""
            val sex = normalized.getOrNull(3) ?: ""
            val pts = normalized.getOrNull(normalized.size - 2) ?: ""
            val division = normalized.getOrNull(normalized.size - 1) ?: ""
            val grades = combineSubjectAndScore(headers, normalized)

            candidates.add(
                StudentResult.FtnaResult(
                    premNo = null,
                    indexNo = cno,
                    name = name,
                    sex = sex,
                    points = pts,
                    division = division,
                    subjects = grades
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