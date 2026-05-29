package com.mtabo.necta.parser

import com.mtabo.necta.models.ExamType
import com.mtabo.necta.models.PerformanceRow
import com.mtabo.necta.models.SchoolPerformance
import com.mtabo.necta.utils.TableUtils.getLegacyFtnaPerformanceTable
import com.mtabo.necta.utils.TableUtils.getPerformanceTable
import com.mtabo.necta.utils.TableUtils.getSfnaPeformanceTable
import necta.utils.pick
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object PerformanceParser {
    fun parsePerformance(
        doc: Document,
        examType: ExamType,
        year: Int,
    ): SchoolPerformance {

        // Helper to get the relevant table for an exam type
        fun getTable(): List<Element>? {
            return when (examType) {
                ExamType.ACSEE -> if (year <= 2019) null else getPerformanceTable(doc)
                ExamType.CSEE -> if (year <= 2018) null else getPerformanceTable(doc)
                ExamType.FTNA -> when {
                    year < 2016 -> null
                    year >= 2022 -> getPerformanceTable(doc)
                    else -> getLegacyFtnaPerformanceTable(doc)
                }

                ExamType.PSLE -> if (year < 2019) null else getPerformanceTable(doc)
                ExamType.SFNA -> if (year < 2019) null else getSfnaPeformanceTable(doc)
            }
        }

        // --- Parse the table or legacy rows ---
        val rawPerformanceRows: List<PerformanceRow> = getTable()?.let { rows ->
            parseModernSchoolPerformance(rows).performanceRows
        } ?: emptyList()

        // --- Prepend header if there is data ---
        return if (rawPerformanceRows.isEmpty()) {
            SchoolPerformance(emptyList())
        } else {
            val header = setGradeLabels(examType, year)
            SchoolPerformance(performanceRows = listOf(header) + rawPerformanceRows)
        }
    }

    /**
     * Parses a list of table row elements into a SchoolPerformance.
     * Each row is converted into a PerformanceRow with the first column normalized.
     */
    fun parseModernSchoolPerformance(rows: List<Element>): SchoolPerformance {
        val performanceRowList = rows.mapNotNull { tr ->
            val tds = tr.select("td, th")
            if (tds.size >= 6) tds.toPerformanceRow() else null
        }

        return SchoolPerformance(performanceRowList)
    }

    // ───────────────────────────────────────────────
    // Legacy Parser
    // ───────────────────────────────────────────────
    fun parseLegacySchoolPerformance(doc: Document): SchoolPerformance {
        val summaryElement = doc.select("h3").firstOrNull {
            val text = it.text().uppercase()
            text.contains("DIV-I") || text.contains("MERIT") || text.contains("DISTINCTION")
        } ?: return SchoolPerformance(emptyList())

        val text = summaryElement.text().uppercase()
        val regex = Regex("""([A-Z0-9\-]+)\s*=\s*(\d+)""")

        val divisions = regex.findAll(text).associate { match ->
            match.groupValues[1] to match.groupValues[2]
        }

        val data = PerformanceRow(
            values = listOf(
                "T",
                divisions.pick("DIV-I", "DISTINCTION"),
                divisions.pick("DIV-II", "MERIT"),
                divisions.pick("DIV-III", "CREDIT"),
                divisions.pick("DIV-IV", "PASS"),
                divisions.pick("DIV-0", "FLD", "FAIL")
            )
        )

        return SchoolPerformance(listOf(data))
    }

    private fun setGradeLabels(examType: ExamType, year: Int): PerformanceRow {
        val headers = when (examType) {
            ExamType.ACSEE, ExamType.CSEE, ExamType.FTNA -> {
                if ((examType == ExamType.CSEE && year == 2014) || (examType == ExamType.ACSEE && year == 2015)) {
                    listOf("GPA", "DIST", "MERIT", "CREDIT", "PASS", "FAIL")
                } else {
                    listOf("DIV", "I", "II", "III", "IV", "0")
                }
            }

            ExamType.SFNA, ExamType.PSLE -> listOf("GREDI", "A", "B", "C", "D", "F")
        }

        return PerformanceRow(headers)
    }

    /**
     * Converts a list of table cells (tds) into a PerformanceRow.
     * Normalizes the first column to standard codes:
     *  - Girls → "WAS"
     *  - Boys  → "WAV"
     *  - Total → "JUMLA"
     * Other columns remain unchanged.
     */
    private fun List<Element>.toPerformanceRow(): PerformanceRow {
        // Helper function to normalize the first cell
        fun normalizeGenderLabel(label: String): String {
            val lower = label.lowercase()
            return when {
                listOf("jumla", "jum", "t", "total").any { lower.contains(it) } -> "JUMLA"
                listOf("wasichana", "was", "f", "girls").any { lower.contains(it) } -> "WAS"
                listOf("wavulana", "wav", "m", "boys").any { lower.contains(it) } -> "WAV"
                else -> label // fallback
            }
        }

        return PerformanceRow(
            values = this.take(6).mapIndexed { index, td ->
                val text = td.text().trim()
                if (index == 0) normalizeGenderLabel(text) else text
            })
    }
}

