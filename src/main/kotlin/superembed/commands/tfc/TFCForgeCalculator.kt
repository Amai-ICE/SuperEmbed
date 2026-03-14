package uk.amaiice.superembed.commands.tfc

object TFCForgeCalculator {
    private val hammerPowers = listOf(-3, -6, -9, -15, 2, 7, 13, 16)

    // 大きい順に並べ替えたプラスのリスト
    val plusHammers = hammerPowers.filter { it > 0 }.sortedByDescending { it }

    fun calculate(data: TFCForgeData): Pair<String, List<Int>> {
        data.sequence.forEach {
            if (it !in hammerPowers) {
                return "${it}は無効な値です。再度入力し直してください。" to listOf()
            }
        }
        val targetValue = data.hammer - data.sequence.sum()

        val result = tailCalculation(targetValue)
        return if (result == listOf(-1)) {
            "10回試行しても解が見つかりませんでした。条件を変えて再度試してください。" to listOf()
        } else {
            "" to result
        }
    }

    private tailrec fun tailCalculation(
        targetValue: Int,
        loopCount: Int = 0,
        simulatedHammerInput: MutableList<Int> = mutableListOf(0, 0, 0, 0),
    ): List<Int> {
        val forgeResult: MutableList<Int> = mutableListOf(0, 0, 0, 0)
        if (loopCount == 10) {
            return listOf(-1)
        }
        var forgeTarget = targetValue
        plusHammers.forEachIndexed { index, plusHammer ->
            for (i in 0..<forgeTarget / plusHammer - simulatedHammerInput[index]) {
                forgeResult[index] += 1
                forgeTarget -= plusHammer
            }
        }

        for (i in 0..2) {
            if (0 <= plusHammers[i] - simulatedHammerInput[i]) {
                if (plusHammers[i + 1] <= plusHammers[i]) {
                    simulatedHammerInput[i] += 1
                    break
                }
            }
        }

        val sum = plusHammers.mapIndexed { index, plusHammer ->
            plusHammer * forgeResult[index]
        }.sum()

        return if (sum == targetValue) {
            forgeResult.toList()
        } else {
            tailCalculation(targetValue, loopCount + 1, simulatedHammerInput)
        }
    }
}