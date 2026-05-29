package com.mtabo.necta.parser

import com.mtabo.necta.models.ExamType
import com.mtabo.necta.models.School
import org.jsoup.nodes.Document


fun parseSchools(doc: Document, examType: ExamType): List<School> =
    when (examType) {
        ExamType.ACSEE, ExamType.CSEE -> parseSecondarySchools(doc)
        ExamType.FTNA -> parseFtnaSchools(doc)
        ExamType.PSLE, ExamType.SFNA -> parsePrimarySchool(doc)
    }


// Extracts Acsee and Csee without problem
fun parseSecondarySchools(doc: Document): List<School> {
    val validIdRegex = Regex("^[A-Z]\\d{4}$") // e.g. P0136, S0452, C1234

    return doc.select("a[href$=.htm], a[href$=.html]")
        .mapNotNull { a ->
            val text = a.text().trim()
            val parts = text.split(" ", limit = 2)

            if (parts.size == 2) {
                val schoolId = parts[0].trim().uppercase()
                val schoolName = parts[1].trim()

                if (validIdRegex.matches(schoolId)) {
                    School(schoolId, schoolName)
                } else null
            } else null
        }
}

fun parseFtnaSchools(doc: Document): List<School> {
    val validIdRegex = Regex("[A-Z]\\d{4}") // e.g. matches S0136 inside any string

    return doc.select("a[href$=.htm], a[href$=.html]")
        .mapNotNull { a ->
            val href = a.attr("href").trim()
            val schoolIdPart = href.substringBefore(".").uppercase()
                .substringAfterLast("RESULTS/") // trims schoolId in years starting 2024

            val schoolName = a.text().trim().replace(validIdRegex, "").trim()

            if (validIdRegex.containsMatchIn(schoolIdPart)) {
                School(schoolIdPart, schoolName)
            } else null
        }
}


fun parsePrimarySchool(doc: Document): List<School> {
    val validIdRegex = Regex("[A-Z]{1,2}\\d{7}")    // - P0905233, PS0905233 (primary)

    return doc.select("a[href$=.htm], a[href$=.html]")
        .mapNotNull { a ->
            val href = a.attr("href").trim()
            val schoolIdPart = href.substringBefore(".").uppercase()

            // Extract the actual ID from href (in case there are prefixes/suffixes like 05_P1367-0)
            val idMatch = validIdRegex.find(schoolIdPart)
            val schoolId = idMatch?.value ?: return@mapNotNull null

            // Clean school name by removing any embedded ID text
            val schoolName = a.text().trim()
                .replace(validIdRegex, "")
                .replace("-", " ")
                .trim()

            School(schoolId, schoolName)
        }
}
