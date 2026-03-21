package app.resolvekit.ui

import android.content.Context
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

fun interface ResolveKitRuntimeFactory {
    fun create(context: Context): ResolveKitRuntime
}

object ResolveKitChatHostRegistry {
    private val factories = ConcurrentHashMap<String, ResolveKitRuntimeFactory>()

    fun register(factory: ResolveKitRuntimeFactory): String {
        val id = UUID.randomUUID().toString()
        factories[id] = factory
        return id
    }

    fun resolve(id: String): ResolveKitRuntimeFactory? = factories[id]

    fun unregister(id: String) {
        factories.remove(id)
    }
}
