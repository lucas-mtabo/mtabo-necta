package com.mtabo.necta.models

/**
 * Supported NECTA examination types.
 *
 * @property code Official examination code used by NECTA.
 * @property availableFrom The earliest year for which examination results are available.
 */
enum class Exam(
    val code: String,
    val availableFrom: Int
) {
    ACSEE("ACSEE", 2005),
    CSEE("CSEE", 2003),
    FTNA("FTNA", 2014),
    PSLE("PSLE", 2013),
    SFNA("SFNA", 2015);
}