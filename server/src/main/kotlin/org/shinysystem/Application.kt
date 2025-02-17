package org.shinysystem

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

// Add this configuration class at the top level
@Serializable
data class ApplicationConfig(
    val ollamaModel: String = "deepseek-r1:14b" // Default model
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
    // Initialize config from environment variable or use default
    val config = ApplicationConfig(
        ollamaModel = System.getenv("OLLAMA_MODEL") ?: "deepseek-r1:14b"
    )

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
        allowHeader(HttpHeaders.CacheControl)
        allowHeader(HttpHeaders.ContentType)
        exposeHeader(HttpHeaders.ContentType)
        exposeHeader("X-Session-ID")
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
                    model = config.ollamaModel, // Use the configured model
                    prompt = prompt,
                    stream = false  // Explicitly set to false
                )

                val response = client.post("http://localhost:11434/api/generate") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(OllamaRequest.serializer(), ollamaRequest))
                }

                val ollamaResponseText = response.bodyAsText()
                logger.info("Ollama API Raw Response: $ollamaResponseText")

                // Parse the response and extract just the content
                val combinedResponse = ollamaResponseText
                    .split("\n")
                    .mapNotNull { line ->
                        try {
                            json.decodeFromString<OllamaResponse>(line).response
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .joinToString("")

                call.respond(ApiResponse(success = true, data = combinedResponse))

            } catch (e: Exception) {
                logger.error("Error in /start-adventure", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<String>(success = false, error = e.message ?: "Unknown error")
                )
            }
        }

        // Add a new endpoint to check current configuration
        get("/config") {
            call.respond(ApiResponse(success = true, data = config))
        }

        // Add this to your existing routing block in module()

        post("/action") {
            try {
                val actionRequest = call.receive<ActionRequest>()
                logger.info("Received action request: ${actionRequest.action}")

                // Get session ID from header (if you're using session management)
                val sessionId = call.request.header("X-Session-ID")

                // Construct the prompt for the action
                val prompt = """
            Continue the adventure based on the player's action:
            "${actionRequest.action}"
            
            Guidelines:
            1. Describe the result of the action vividly but concisely
            2. Include relevant consequences and changes to the environment
            3. Present 2-3 new possible actions based on the new situation
            4. Keep descriptions atmospheric and engaging
            5. End with "What would you like to do?"
            
            Limit the response to 250 words.
        """.trimIndent()

                val ollamaRequest = OllamaRequest(
                    model = config.ollamaModel,
                    prompt = prompt,
                    stream = false
                )

                val response = client.post("http://localhost:11434/api/generate") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(OllamaRequest.serializer(), ollamaRequest))
                }

                val ollamaResponseText = response.bodyAsText()
                logger.info("Ollama API Raw Response: $ollamaResponseText")

                // Parse the response and extract just the content
                val combinedResponse = ollamaResponseText
                    .split("\n")
                    .mapNotNull { line ->
                        try {
                            json.decodeFromString<OllamaResponse>(line).response
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .joinToString("")

                call.respond(ApiResponse(success = true, data = combinedResponse))

            } catch (e: Exception) {
                logger.error("Error in /action", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<String>(success = false, error = e.message ?: "Unknown error")
                )
            }
        }

        get("/stream-adventure") {
            logger.info("Streaming adventure invoked")
            try {
                val sessionId = call.request.header("X-Session-ID")
                val settings = if (sessionId != null) {
                    sessions[sessionId]?.let { json.decodeFromString<AdventureSettings>(it) }
                } else {
                    AdventureSettings()
                } ?: AdventureSettings()

                logger.info("Starting streaming adventure with settings: $settings")

                val prompt = generateAdventurePrompt(settings)
                val ollamaRequest = OllamaRequest(
                    model = config.ollamaModel,
                    prompt = prompt,
                    stream = true
                )

                // Set required headers for SSE
                call.response.headers.append(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
                call.response.headers.append(HttpHeaders.CacheControl, "no-cache")
                call.response.headers.append(HttpHeaders.Connection, "keep-alive")
                call.response.headers.append("X-Accel-Buffering", "no")
                call.response.headers.append("Access-Control-Allow-Origin", "*") // Or your specific origin
                call.response.headers.append("Access-Control-Allow-Credentials", "true")
                call.response.headers.append("Access-Control-Allow-Headers", "X-Session-ID")


                // Create channel for streaming
                val channel = Channel<String>()

                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    write("event: connected\n")
                    write("data: {\"status\":\"connected\"}\n\n")
                    flush()

                    try {
                        val response = client.post("http://localhost:11434/api/generate") {
                            contentType(ContentType.Application.Json)
                            setBody(json.encodeToString(OllamaRequest.serializer(), ollamaRequest))
                        }

                        val responseChannel = response.bodyAsChannel()
                        var isFirstChunk = true

                        while (!responseChannel.isClosedForRead) {
                            val line = responseChannel.readUTF8Line() ?: break
                            try {
                                val ollamaResponse = json.decodeFromString<OllamaResponse>(line)

                                // Send data event
                                if (isFirstChunk) {
                                    write("event: connected\ndata: SSE connection established\n\n")
                                    isFirstChunk = false
                                }

                                write("event: ${if (ollamaResponse.done) "done" else "message"}\n")
                                write("data: ${ollamaResponse.response}\n\n")
                                flush()

                                if (ollamaResponse.done) {
                                    break
                                }
                            } catch (e: Exception) {
                                logger.error("Error parsing streaming response", e)
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("Error in streaming", e)
                        write("event: error\ndata: ${e.message}\n\n")
                    } finally {
                        write("event: done\ndata: Stream complete\n\n")
                        flush()
                    }
                }
            } catch (e: Exception) {
                logger.error("Error in /stream-adventure", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<String>(success = false, error = e.message ?: "Unknown error")
                )
            }
        }

        post("/stream-action") {
            try {
                val actionRequest = call.receive<ActionRequest>()
                logger.info("Received streaming action request: ${actionRequest.action}")

                val prompt = """
                    Continue the adventure based on the player's action:
                    "${actionRequest.action}"
                    
                    Guidelines:
                    1. Describe the result of the action vividly but concisely
                    2. Include relevant consequences and changes to the environment
                    3. Present 2-3 new possible actions based on the new situation
                    4. Keep descriptions atmospheric and engaging
                    5. End with "What would you like to do?"
                    
                    Limit the response to 250 words.
                """.trimIndent()

                val ollamaRequest = OllamaRequest(
                    model = config.ollamaModel,
                    prompt = prompt,
                    stream = true
                )

                call.response.cacheControl(CacheControl.NoCache(null))

                call.response.cacheControl(CacheControl.NoCache(null))
                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    val response = client.post("http://localhost:11434/api/generate") {
                        contentType(ContentType.Application.Json)
                        setBody(json.encodeToString(OllamaRequest.serializer(), ollamaRequest))
                    }

                    val channel = response.bodyAsChannel()
                    while (!channel.isClosedForRead) {
                        val line = channel.readUTF8Line() ?: break
                        try {
                            val ollamaResponse = json.decodeFromString<OllamaResponse>(line)
                            write("event: ${if (ollamaResponse.done) "done" else "message"}\n")
                            write("data: ${ollamaResponse.response}\n\n")
                            flush()
                        } catch (e: Exception) {
                            logger.error("Error parsing streaming response", e)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Error in /stream-action", e)
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
