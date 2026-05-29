package com.mtabo.necta.utils

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

object TableUtils {
    fun fetchPsleResultTable(doc: Document): List<Element> {
        val tables = doc.select("table")
        val resultTable = tables.find {
            val header = it.select("tr").firstOrNull()?.text()?.uppercase() ?: ""
            header.contains("SUBJECTS") || header.contains("MASOMO")
        } ?: return emptyList()

        return resultTable.select("tr").drop(1)
    }

    fun fetchCseeResultTable(doc: Document): List<Element> {
        val tables = doc.select("table")
        val resultTable = tables.find {
            val header = it.select("tr").firstOrNull()?.text()?.uppercase() ?: ""
            header.contains("CNO") || header.contains("AGGT") || header.contains("PTS")
        } ?: return emptyList()

        return resultTable.select("tr").drop(1)
    }

    fun fetchLegacyFtnaResultTable(doc: Document): List<Element> {
        val table = doc.selectFirst("table") ?: return emptyList()
        return table.select("tr")
    }

    /**
     * clean and join the table headers.
     * this headers are formed by combining several rows to single column.
     */
    fun getLegacyTableHeaders(doc: Document): List<String> {
        val tables = doc.select("table")
        if (tables.isEmpty()) return emptyList()

        val firstTable = tables.first()!!
        val headerRows = firstTable.select("tr").filter { row ->
            row.select("td").any { it.attr("style").contains("font-weight:bold", ignoreCase = true) }
        }

        if (headerRows.isEmpty()) return emptyList()

        val columnCount = headerRows.maxOf { it.select("td").size }
        val combinedHeaders = MutableList(columnCount) { "" }

        headerRows.forEach { row ->
            val cells = row.select("td")
            cells.forEachIndexed { i, cell ->
                if (i >= columnCount) return@forEachIndexed
                val cellText = cell.text()
                    .replace(Regex("\\s+"), " ")
                    .replace(".", "")
                    .trim()

                if (cellText.isNotEmpty()) {
                    combinedHeaders[i] = buildString {
                        append(combinedHeaders[i])
                        append(cellText)
                    }
                }
            }
        }
        return combinedHeaders
    }


    /**
     * Fetch modern performance tables
     */
    fun getPerformanceTable(doc: Document): List<Element> {
        val table = doc.select("table").firstOrNull() ?: return emptyList()
        val rows = table.select("tr")
        return rows.drop(1)
    }

    /**
     * Fetch SFNA performance table
     */
    fun getSfnaPeformanceTable(doc: Document): List<Element>{
        val table = doc.selectFirst("table.tbl") ?: return emptyList()
        val rows = table.select("tr")
        return rows.drop(1)
    }

    /**
     * Extracts the FTNA school performance table from legacy (older format) result pages.
     *
     * This method:
     *  - Selects the first <table> element in the document.
     *  - Iterates through its rows while filtering out header or invalid rows.
     *  - Collects data rows until it reaches the "TOT" (Total) row, which marks the end.
     *
     * @param doc Jsoup Document representing the fetched HTML page
     * @return A list of <tr> elements containing the relevant performance rows,
     *         ending with the row whose first <td> contains "TOT".
     */
    fun getLegacyFtnaPerformanceTable(doc: Document): List<Element> {
        // Select the first table element in the HTML document
        val table = doc.selectFirst("table") ?: return emptyList()

        // Select all rows (<tr>) inside the table
        val rows = table.select("tr")

        // Create a mutable list to hold the filtered data rows
        val filteredRows = mutableListOf<Element>()

        // Loop through each table row
//        for (tr in rows) {
//            // Select all cells (both <td> and <th>) in the row
//            val tds = tr.select("td, th")
//
//            // Skip rows that are:
//            // - Empty (no cells)
//            // - Contain colspan attributes (likely header/merged cells)
//            // - Have "DIVISION" text in the first cell (also header rows)
//            if (
//                tds.isEmpty() ||
//                tds.any { it.hasAttr("colspan") } ||
//                tds.first().text().contains("DIVISION", ignoreCase = true)
//            ) continue
//
//            // Add this valid data row to the filtered list
//            filteredRows.add(tr)
//
//            // Stop collecting once we reach the row whose first <td> contains "TOT"
//            // This usually marks the end of the performance section
//            if (tds.first().text().contains("TOT", ignoreCase = true)) break
//        }
        for (tr in rows) {

            val tds = tr.select("td, th")

            if (tds.isEmpty()) continue

            val firstCellText = tds.firstOrNull()?.text() ?: continue

            if (
                tds.any { it.hasAttr("colspan") } ||
                firstCellText.contains("DIVISION", ignoreCase = true)
            ) continue

            filteredRows.add(tr)

            if (firstCellText.contains("TOT", ignoreCase = true)) break
        }

        // Return all valid data rows up to and including the "TOT" row
        return filteredRows
    }

//    fun getLegacyFtnaPerformanceTable(doc: Document): List<Element> {
//
//        val table = doc.selectFirst("table") ?: return emptyList()
//        val rows = table.select("tr")
//
//        val result = mutableListOf<Element>()
//
//        for (tr in rows) {
//
//            val tds = tr.select("td, th")
//            val firstText = tds.firstOrNull()?.text() ?: continue
//
//            if (
//                tds.isEmpty() ||
//                tds.any { it.hasAttr("colspan") } ||
//                firstText.contains("DIVISION", true)
//            ) continue
//
//            result.add(tr)
//
//            if (firstText.contains("TOT", true)) break
//        }
//
//        return result
//    }

}
