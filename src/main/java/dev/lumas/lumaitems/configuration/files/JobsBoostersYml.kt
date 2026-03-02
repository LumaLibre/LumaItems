package dev.lumas.lumaitems.configuration.files

import dev.lumas.lumaitems.annotations.File
import dev.lumas.lumaitems.configuration.OkaeriFile
import eu.okaeri.configs.annotation.CustomKey

@File("jobs-boosters.yml")
class JobsBoostersYml : OkaeriFile() {

    @CustomKey("exp-boost-commands")
    var expBoostCommands = listOf(
        "say <#F7FFC9>{player} <#E2E2E2>has redeemed a {item}<#E2E2E2>! <s:entity.player.levelup>",
        "say Use [/luma hidebar] to toggle boost visibility.",
        "jobs boost all exp {duration} {value}",
        "lumautilities actionbartimer {duration} Exp Boost"
    )

    @CustomKey("money-boost-commands")
    var moneyBoostCommands = listOf(
        "say <#F7FFC9>{player} <#E2E2E2>has redeemed a {item}<#E2E2E2>! <s:entity.player.levelup>",
        "say Use [/luma hidebar] to toggle boost visibility.",
        "jobs boost all money {duration} {value}",
        "lumautilities actionbartimer {duration} Money Boost"
    )

    @CustomKey("exp-and-money-boost-commands")
    var expAndMoneyBoostCommands = listOf(
        "say <#F7FFC9>{player} <#E2E2E2>has redeemed a {item}<#E2E2E2>! <s:entity.player.levelup>",
        "say Use [/luma hidebar] to toggle boost visibility.",
        "jobs boost all exp {duration} {value}",
        "jobs boost all money {duration} {value}",
        "lumautilities actionbartimer {duration} Money & Exp Boost"
    )
}