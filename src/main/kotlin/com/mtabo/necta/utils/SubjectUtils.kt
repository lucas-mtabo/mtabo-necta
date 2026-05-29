package necta.utils

import com.mtabo.necta.models.Subject
import org.jsoup.nodes.Element


/**
 * Utility object for parsing subject strings into structured results.
 */
object SubjectUtils {

    /**
     * Parses a raw subject string (like "CIV-'A' GEO-'B' ENG-'C'")
     * into a list of SubjectResult objects.
     *
     * Example:
     * Input:  "CIV-'A' GEO-'B' ENG-'C'"
     * Output: [SubjectResult("CIV", "A"), SubjectResult("GEO", "B"), SubjectResult("ENG", "C")]
     */
    fun splitSubjectString(subjectsRaw: String?): List<Subject> {
        if (subjectsRaw.isNullOrBlank()) return emptyList()

        val subjectDetails = mutableListOf<Subject>()

        // Handle two formats:
        // 1. "CIV-'A', ENG-'B'"
        // 2. "Kiswahili - C, English - E"
        val pattern = Regex("""([A-Za-z0-9/&.\s]+)-\s*'?([A-Z])'?""")
        //Regex("""(.+?)-\s*'?([A-Z])'?""") Very flexible — matches anything before -

        pattern.findAll(subjectsRaw).forEach { match ->
            var subjectName = match.groupValues[1].trim()
            val grade = match.groupValues[2].trim().uppercase()

            // Normalize: capitalize first letters of words
            subjectName = subjectName.split(" ").joinToString(" ")
            subjectDetails.add(Subject(subjectName, grade))
        }

        return subjectDetails
    }

    /**
     * For PSLE and SFNA
     * Safely extracts the final grade and the subject list from subjectDetails.
     * Detailed subject string contains average grade inside as last element
     * Prevents crashes if subjectDetails is empty.
     */
    fun splitGradeFromSubjects(subjects: Element): Pair<String, List<Subject>> {
        val subjectDetails = splitSubjectString(subjects.text())
        return if (subjectDetails.isNotEmpty()) {
            subjectDetails.last().grade to subjectDetails.dropLast(1)
        } else {
            "" to emptyList()
        }
    }

    /**
     * For Legacy FTNA results
     * Extract and clean subject names header (5th column to 2nd from last)
     * Then combines respective subject score to name from result rows
     */
    fun combineSubjectAndScore(tableHeaders: List<String>, resultRows: List<String>): List<Subject> {
        val subjectNames = tableHeaders.drop(4).dropLast(2).map {
            it.replace(Regex("\\s+"), " ")
                .replace(".", "").trim()
        }

        return resultRows
            .drop(4)
            .dropLast(2)
            .mapIndexed { i, grade ->
                Subject(subjectNames.getOrElse(i) { "SUBJECT$i" }, grade.trim())
            }
            .filter { it.grade.isNotEmpty() } // removes subject result without score
    }
}
