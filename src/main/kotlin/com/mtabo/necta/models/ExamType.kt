package com.mtabo.necta.models

enum class ExamType(val code: String, val minYear: Int) {
    ACSEE("ACSEE", 2005),
    CSEE("CSEE", 2003),
    FTNA("FTNA", 2014),
    PSLE("PSLE", 2013),
    SFNA("SFNA", 2015);
}