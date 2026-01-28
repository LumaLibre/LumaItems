package dev.lumas.lumaitems.hooks

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.lumas.lumaitems.registry.Identifier
import dev.lumas.lumaitems.registry.StringIdentifier

class ProtocolLibHook : Hook {
    override fun identifier(): Identifier {
        return StringIdentifier.of("ProtocolLib")
    }

    fun getProtocolManager(): ProtocolManager? {
        return if (this.isWith()) ProtocolLibrary.getProtocolManager() else null
    }
}