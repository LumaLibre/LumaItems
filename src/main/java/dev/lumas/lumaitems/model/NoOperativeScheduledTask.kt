package dev.lumas.lumaitems.model

import dev.lumas.lumaitems.LumaItems
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.plugin.Plugin

class NoOperativeScheduledTask : ScheduledTask {

    companion object {
        fun create(): NoOperativeScheduledTask {
            return NoOperativeScheduledTask()
        }
    }


    override fun getOwningPlugin(): Plugin {
        return LumaItems.getInstance()
    }

    override fun isRepeatingTask(): Boolean {
        return false
    }

    override fun cancel(): ScheduledTask.CancelledState {
        return ScheduledTask.CancelledState.CANCELLED_ALREADY
    }

    override fun getExecutionState(): ScheduledTask.ExecutionState {
        return ScheduledTask.ExecutionState.CANCELLED
    }
}