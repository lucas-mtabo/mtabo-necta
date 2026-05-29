package necta.utils

/**
 * Returns the first non-null value from the map for the given keys.
 *
 * Iterates through the provided keys in order and returns the value
 * of the first key that exists in the map. If none of the keys are found,
 * it returns an empty string.
 *
 * Useful for handling multiple possible keys that represent the same data.
 *
 * Example:
 * map.pick("DIV-0", "FLD", "FAIL")
 * → returns the first available value among those keys
 */
fun Map<String, String>.pick(vararg keys: String): String {
    return keys.firstNotNullOfOrNull { this[it] } ?: ""
}