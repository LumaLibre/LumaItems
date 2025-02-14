package dev.jsinco.luma.lumaitems.items.nests

enum class Reward(val finley: EncapsulatedReward, val axie: EncapsulatedReward) {

    // 10-20 Reward commands.
    REWARD_1(
        EncapsulatedReward.of(0.5, "lumaitems relic %s core astral 1"),
        EncapsulatedReward.of(0.5, "lumaitems relic %s core astral 1")
    )

    ;
}

data class EncapsulatedReward(val command: String, val chance: Double) {
    companion object {
        fun of(chance: Double, cmd: String) = EncapsulatedReward(cmd, chance)
        fun of(cmd: String, chance: Double) = EncapsulatedReward(cmd, chance)
    }
}