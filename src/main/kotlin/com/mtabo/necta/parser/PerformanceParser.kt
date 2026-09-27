package com.mtabo.necta.parser

import com.mtabo.necta.models.Exam
import com.mtabo.necta.utils.TableUtils.getLegacyFtnaPerformanceTable
import com.mtabo.necta.utils.TableUtils.getPerformanceTable
import com.mtabo.necta.utils.TableUtils.getSfnaPeformanceTable
import com.mtabo.necta.utils.pick
import com.mtabo.necta.models.PerformanceItem
import com.mtabo.necta.models.SchoolPerformance
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object PerformanceParser {
    fun parsePerformance(
        doc: Document,
        exam: Exam,
        year: Int,
    ): SchoolPerformance {

        // Helper to get the relevant table for an exam type
        fun getTable(): List<Element>? {
            return when (exam) {
                Exam.ACSEE -> if (year <= 2019) null else getPerformanceTable(doc)
                Exam.CSEE -> if (year <= 2018) null else getPerformanceTable(doc)
                Exam.FTNA -> when {
                    year < 2016 -> null
                    year >= 2022 -> getPerformanceTable(doc)
                    else -> getLegacyFtnaPerformanceTable(doc)
                }

                Exam.PSLE -> if (year < 2019) null else getPerformanceTable(doc)
                Exam.SFNA -> if (year < 2019) null else getSfnaPeformanceTable(doc)
            }
        }

        // --- Parse the table or legacy rows ---
        val rawPerformanceItems: List<PerformanceItem> = getTable()?.let { rows ->
            parseModernSchoolPerformance(rows).performanceItems
        } ?: emptyList()

        // --- Prepend header if there is data ---
        return if (rawPerformanceItems.isEmpty()) {
            SchoolPerformance(emptyList())
        } else {
            val header = setGradeLabels(exam, year)
            SchoolPerformance(performanceItems = listOf(header) + rawPerformanceItems)
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

        val data = PerformanceItem(
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

    private fun setGradeLabels(exam: Exam, year: Int): PerformanceItem {
        val headers = when (exam) {
            Exam.ACSEE, Exam.CSEE, Exam.FTNA -> {
                if ((exam == Exam.CSEE && year == 2014) || (exam == Exam.ACSEE && year == 2015)) {
                    listOf("GPA", "DIST", "MERIT", "CREDIT", "PASS", "FAIL")
                } else {
                    listOf("DIV", "I", "II", "III", "IV", "0")
                }
            }

            Exam.SFNA, Exam.PSLE -> listOf("GREDI", "A", "B", "C", "D", "F")
        }

        return PerformanceItem(headers)
    }

    /**
     * Converts a list of table cells (tds) into a PerformanceRow.
     * Normalizes the first column to standard codes:
     *  - Girls → "WAS"
     *  - Boys  → "WAV"
     *  - Total → "JUMLA"
     * Other columns remain unchanged.
     */
    private fun List<Element>.toPerformanceRow(): PerformanceItem {
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

        return PerformanceItem(
            values = this.take(6).mapIndexed { index, td ->
                val text = td.text().trim()
                if (index == 0) normalizeGenderLabel(text) else text
            })
    }
}

