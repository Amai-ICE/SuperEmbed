package uk.amaiice.superembed.util

object SiFormatter {
    fun Int.toSiFormat(): String {
        val siMap = mutableMapOf<String, Int>(
            "B" to 1_000_000_000,
            "M" to 1_000_000,
            "K" to 1_000,
        )

        siMap.forEach { (si, value) ->
            if (this >= value) {
                val formatted = String.format("%.2f", this.toDouble() / value)
                return "$formatted$si"
            }
        }
        return this.toString()
    }
}