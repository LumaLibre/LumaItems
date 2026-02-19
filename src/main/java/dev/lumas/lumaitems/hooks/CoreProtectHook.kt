package dev.lumas.lumaitems.hooks

import dev.lumas.lumaitems.registry.Identifier
import dev.lumas.lumaitems.registry.StringIdentifier
import net.coreprotect.CoreProtect
import net.coreprotect.CoreProtectAPI

class CoreProtectHook : Hook {
    override fun identifier(): Identifier {
        return StringIdentifier.of("CoreProtect")
    }

    fun getCoreProtectAPI(): CoreProtectAPI? {
        return if (this.isWith()) CoreProtect.getInstance().api else null
    }
}