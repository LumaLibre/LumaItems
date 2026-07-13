package dev.lumas.lumaitems.util

object CanvasCompat {

    private val entityTeleportAsyncEvent: Class<*>? = runCatching {
        Class.forName(
            "io.canvasmc.canvas.event.EntityTeleportAsyncEvent",
            false,
            CanvasCompat::class.java.classLoader
        )
    }.getOrNull()

    val isCanvas: Boolean
        get() = entityTeleportAsyncEvent != null

    fun isEntityTeleportAsync(event: Any): Boolean =
        entityTeleportAsyncEvent?.isInstance(event) == true
}