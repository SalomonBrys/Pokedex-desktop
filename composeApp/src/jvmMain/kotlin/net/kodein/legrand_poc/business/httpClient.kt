package net.kodein.legrand_poc.business

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.json.Json


val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

suspend fun <T : Any> HttpClient.getFromPokemonRef(ref: PokemonRef<T>, typeInfo: TypeInfo): T =
    this.get(ref.url).body(typeInfo)

suspend inline fun <reified T : Any> HttpClient.getFromPokemonRef(ref: PokemonRef<T>): T =
    getFromPokemonRef(ref, typeInfo<T>())
