/**
 * Parse date from various formats
 * Handles:
 * - ISO-8601 strings: "2025-10-31T18:12:54"
 * - Array format (from old backend): [2025, 10, 31, 18, 12, 54, 276860000]
 * - Invalid dates: returns new Date() as fallback
 */
export function parseDate(date) {
    if (!date) return new Date();

    // If it's already a Date object
    if (date instanceof Date) return date;

    // If it's a string (ISO-8601)
    if (typeof date === 'string') {
        const parsed = new Date(date);
        return isNaN(parsed.getTime()) ? new Date() : parsed;
    }

    // If it's an array [year, month, day, hour, minute, second, nanosecond]
    if (Array.isArray(date) && date.length >= 3) {
        // month is 0-indexed in JavaScript Date
        return new Date(date[0], date[1] - 1, date[2], date[3] || 0, date[4] || 0, date[5] || 0);
    }

    // Fallback to current date
    return new Date();
}
