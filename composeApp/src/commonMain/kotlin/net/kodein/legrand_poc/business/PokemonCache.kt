package net.kodein.legrand_poc.business

import io.ktor.util.reflect.*


object PokemonCache {

    private val cache = mutableMapOf<PokemonRef<*>, Any>()

    suspend fun <T : Any> get(ref: PokemonRef<T>, typeInfo: TypeInfo): T {
        val cachedValue = cache[ref]
        if (cachedValue != null) {
            @Suppress("UNCHECKED_CAST")
            if (cachedValue::class == typeInfo.type) return cachedValue as T
            else throw ClassCastException("Cached value is not of the expected type (cached ${cachedValue::class} != expected ${typeInfo.type})")
        }

        val value = httpClient.getFromPokemonRef(ref, typeInfo)
        cache[ref] = value
        return value
    }

    suspend inline fun <reified T : Any> get(ref: PokemonRef<T>): T = get(ref, typeInfo<T>())
}
