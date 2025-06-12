package dev.jsinco.luma.lumaitems.util.dialogue

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import java.util.concurrent.TimeUnit
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player

class DialogueText(
    val player: Player,
    var ifAbsentColor: TextColor = NamedTextColor.WHITE,
    var textSpeed: Long = 60,
    var voicePitch: Float = 1.0f,
    var voiceVolume: Float = 0.75f,
) {

    private val queue: MutableList<String> = mutableListOf()
    private var running: Boolean = false

    fun queueText(text: String) {
        if (this.running) return
        queue.add(text)
    }

    fun queueText(text: Array<out String>) {
        if (this.running) return
        queue.addAll(text.toList())
    }

    fun queueText(text: List<String>) {
        if (this.running) return
        queue.addAll(text)
    }


    fun sendQueuedText(callback: Runnable?) {
        if (queue.isEmpty() || this.running) {
            return
        }

        val currentChar = intArrayOf(0)
        val text: StringBuilder = StringBuilder(queue.first())

        Bukkit.getAsyncScheduler().runAtFixedRate(LumaItems.getInstance(), { task ->
            this.running = true
            val totalChars = text.length
            // If it's a < let's increment until we find a >
            if (currentChar[0] < totalChars && text[currentChar[0]] == '<') {
                while (currentChar[0] < totalChars && text[currentChar[0]] != '>') {
                    currentChar[0]++
                }
                currentChar[0]++
            }


            currentChar[0]++


            // If it's a space, let's jump to the next character
            if (currentChar[0] < totalChars && text[currentChar[0]] == ' ') {
                currentChar[0]++
            }
            if (currentChar[0] <= totalChars) {
                sendActionBar(text.substring(0, currentChar[0]))
            } else {
                queue.removeFirst() // Remove the first element from the queue
                currentChar[0] = 0 // Reset the current character index
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }

                if (queue.isNotEmpty()) {
                    text.delete(0, text.length)
                    text.append(queue.first())
                    currentChar[0] = 0 // Reset the current character index
                    return@runAtFixedRate
                } else {
                    this.running = false
                    task.cancel()
                }
                try {
                    callback?.run()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }, 0, this.textSpeed, TimeUnit.MILLISECONDS)
    }

    fun sendActionBar(text: String) {
        player.sendActionBar(MiniMessageUtil.mm(text).colorIfAbsent(this.ifAbsentColor))
        player.playSound(player.location, Sound.UI_HUD_BUBBLE_POP, this.voiceVolume, this.voicePitch)
    }
}