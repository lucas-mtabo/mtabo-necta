package com.mtabo.necta.parser

import com.mtabo.necta.models.District
import com.mtabo.necta.models.Region
import org.jsoup.nodes.Document

fun parseRegions(doc: Document): List<Region> {
    return doc.select("a[href*=\"reg_\"]") // Match all region links
        .mapNotNull { a ->
            val href = a.attr("href")

            // Match both "reg_10" and "reg_ps10" (case-insensitive)
            val match = Regex("reg_(?:ps)?(\\d+)", RegexOption.IGNORE_CASE).find(href)

            val code = match?.groupValues?.get(1)
            val name = a.text().trim().ifEmpty { null }

            if (code != null && name != null) {
                Region(code, name)
            } else null
        }
}

fun parseDistricts(doc: Document): List<District> {
    return doc.select("a[href*=\"distr_\"]") // Match all district links
        .mapNotNull { a ->
            val href = a.attr("href")

            // Match both "distr_0204" and "distr_ps0206" (case-insensitive)
            val match = Regex("distr_(?:ps)?(\\d+)", RegexOption.IGNORE_CASE).find(href)

            val code = match?.groupValues?.get(1)
            val name = a.text().trim().ifEmpty { null }

            if (code != null && name != null) {
                District(code, name)
            } else null
        }
}




