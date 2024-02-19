package be.hize.nes.features.update

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import moe.nea.libautoupdate.CurrentVersion

class SemanticVersion(val major: Int, val minor: Int, val patch: Int) : CurrentVersion,
    Comparable<SemanticVersion> {

    companion object {
        fun fromString(semverString: String): SemanticVersion? {

            val match = semverRegex.matchEntire(semverString) ?: return null
            val (_, major, minor, patch) = match.groupValues
            return SemanticVersion(
                major.toIntOrNull() ?: return null,
                minor.toIntOrNull() ?: return null,
                patch.toIntOrNull() ?: return null,
            )
        }

        private val semverRegex = "([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:\\+.*)?".toRegex()

        private val comparator = Comparator.comparingInt(SemanticVersion::major)
            .thenComparingInt(SemanticVersion::minor)
            .thenComparing(SemanticVersion::patch)
    }

    override fun display(): String {
        return "$major.$minor.$patch"
    }

    override fun isOlderThan(element: JsonElement?): Boolean {
        val stringVersion = (element as? JsonPrimitive)?.asString ?: return true
        val semverVersion = fromString(stringVersion) ?: return false
        return this < semverVersion
    }

    override fun compareTo(other: SemanticVersion): Int {
        return comparator.compare(this, other)
    }
}
