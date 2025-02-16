package org.socialspaces

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.socialspaces.Greeting
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class OllamaRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)

@Serializable
data class OllamaResponse(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean,
    val context: List<Int> = emptyList(),
    val total_duration: Long = 0,
    val load_duration: Long = 0,
    val prompt_eval_count: Int = 0,
    val eval_count: Int = 0,
    val eval_duration: Long = 0
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class AdventureSettings(
    val setting: String = "medieval fantasy",
    val genre: String = "fantasy",
    val playerCharacter: String = "adventurer",
    val theme: String = "heroic",
    val toneStyle: String = "classic fantasy",
    val additionalDetails: String = ""
)

@Serializable
data class ActionRequest(
    val action: String
)

val logger = LoggerFactory.getLogger("KtorServer")

fun generateAdventurePrompt(settings: AdventureSettings): String {
    return """
    Act as a text adventure game engine. Create the opening scene for an interactive story with the following settings:
    
    Setting: ${settings.setting}
    Genre: ${settings.genre}
    Player Character: ${settings.playerCharacter}
    Theme: ${settings.theme}
    Tone/Style: ${settings.toneStyle}
    Additional Details: ${settings.additionalDetails}
    
    Follow these guidelines:
    1. Start with a vivid description of the opening scene
    2. Establish the player's immediate situation and motivation
    3. Present 3-4 clear initial choices for the player
    4. Keep descriptions concise but atmospheric
    5. Include relevant sensory details
    6. End with "What would you like to do?"
    
    Limit to 300 words
    """.trimIndent()
}

val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Store active sessions
    val sessions = ConcurrentHashMap<String, String>()
    val client = HttpClient(CIO) {
        install(io.ktor.client.plugins.HttpTimeout) {
            requestTimeoutMillis = 300000  // 5 minutes
            connectTimeoutMillis = 30000   // 30 seconds
            socketTimeoutMillis = 300000   // 5 minutes
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader("X-Session-ID")
        anyHost()
    }

    install(ContentNegotiation) {
        json(json)
    }

    routing {
        get("/") {
            logger.info("Received request at root endpoint")
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        get("/health") {
            call.respond(ApiResponse(success = true, data = "Server is running"))
        }

        // Endpoint for checking if server is running
        get("/health") {
            call.respond(ApiResponse(success = true, data = "Server is running"))
        }

        // Match the frontend route exactly
        post("/set-adventure-settings") {
            try {
                val settings = call.receive<AdventureSettings>()
                logger.info("Received adventure settings: $settings")

                // Store settings in memory (you might want to use a proper database in production)
                val sessionId = java.util.UUID.randomUUID().toString()
                sessions[sessionId] = json.encodeToString(AdventureSettings.serializer(), settings)

                call.response.headers.append("X-Session-ID", sessionId)
                call.respond(ApiResponse(success = true, data = "Settings configured successfully"))
            } catch (e: Exception) {
                logger.error("Error in /set-adventure-settings", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<String>(success = false, error = e.message ?: "Unknown error")
                )
            }
        }

        // Modified approach to handle Ollama API streaming response
        post("/start-adventure") {
            try {
                val settings = call.receive<AdventureSettings>()
                logger.info("Starting adventure with settings: $settings")

                val prompt = generateAdventurePrompt(settings)
                val ollamaRequest = OllamaRequest(
                    model = "deepseek-r1:14b",
                    prompt = prompt,
                    stream = false  // Explicitly set to false
                )

                val response = client.post("http://localhost:11434/api/generate") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(OllamaRequest.serializer(), ollamaRequest))
                }

                val ollamaResponseText = response.bodyAsText()
                logger.info("Ollama API Raw Response: $ollamaResponseText")

                // Split the response by lines and process

//                // Find the last complete JSON object (or the one with the most complete response)
//                val lastCompleteResponse = responseLines
//                    .mapNotNull { line ->
//                        try {
//                            json.decodeFromString<OllamaResponse>(line)
//                        } catch (e: Exception) {
//                            null
//                        }
//                    }
//                    .lastOrNull { it.response.isNotBlank() }

//                if (lastCompleteResponse != null) {
//                    // logger.info("Error in /start-adventure")
                    call.respond(ApiResponse(success = true, data = ollamaResponseText))
//                } else {
//                    // Fallback if no valid response found
//                    logger.error("No valid response found in Ollama API output")
//                    call.respond(
//                        HttpStatusCode.InternalServerError,
//                        ApiResponse<String>(success = false, error = "Unable to parse Ollama API response")
//                    )
//                }
            } catch (e: Exception) {
                logger.error("Error in /start-adventure", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<String>(success = false, error = e.message ?: "Unknown error")
                )
            }
        }


        post("/start-adventure2") {
            try {
                val settings = call.receive<AdventureSettings>()
                logger.info("Starting adventure with settings: $settings")

                val prompt = generateAdventurePrompt(settings)
                val ollamaRequest = OllamaRequest(
                    model = "deepseek-r1",
                    prompt = prompt,
                    stream = false
                )

                val response = client.post("http://localhost:11434/api/generate") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(OllamaRequest.serializer(), ollamaRequest))
                }

                val ollamaResponseText = response.bodyAsText()  // Get the raw response text
                logger.info("Ollama API Response: $ollamaResponseText")  // Log the raw response

                // Try to decode the response into the expected object
                try {
                    val ollamaResponse = json.decodeFromString<OllamaResponse>(ollamaResponseText)
                    call.respond(ApiResponse(success = true, data = ollamaResponse.response))
                } catch (jsonException: Exception) {
                    // If decoding fails, log the issue and return the raw response text
                    logger.error("Error decoding response: ${jsonException.message}")
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<String>(success = false, error = "Error parsing response from Ollama API: $ollamaResponseText")
                    )
                }
            } catch (e: Exception) {
                logger.error("Error in /start-adventure", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<String>(success = false, error = e.message ?: "Unknown error")
                )
            }
        }


        get("/placeholder/{width}/{height}") {
            try {
                val width = call.parameters["width"]?.toIntOrNull() ?: 400
                val height = call.parameters["height"]?.toIntOrNull() ?: 300

                val svg = """
                    <svg width="$width" height="$height" xmlns="http://www.w3.org/2000/svg">
                        <rect width="100%" height="100%" fill="#1a1a1a"/>
                        <text x="50%" y="50%" font-family="Arial" font-size="24" fill="#666" text-anchor="middle" dy=".3em">
                            ${width}x${height}
                        </text>
                    </svg>
                """.trimIndent()

                call.response.header(HttpHeaders.ContentType, "image/svg+xml")
                call.respondText(svg)
            } catch (e: Exception) {
                logger.error("Error generating placeholder", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<String>(success = false, error = "Error generating placeholder")
                )
            }
        }
    }
}
