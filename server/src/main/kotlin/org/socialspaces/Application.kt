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
    val client = HttpClient(CIO) {
        install(io.ktor.client.plugins.HttpTimeout) {
            requestTimeoutMillis = 300000  // 5 minutes
            connectTimeoutMillis = 30000   // 30 seconds
            socketTimeoutMillis = 300000   // 5 minutes
        }
    }

    install(CORS) {
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
    install(ContentNegotiation) { json() }

    routing {
        get("/") {
            logger.info("Received request at root endpoint")
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        post("/set-adventure-settings") {
            try {
                val settings = call.receive<AdventureSettings>()
                logger.info("Received adventure settings: $settings")
                call.respond(ApiResponse(success = true, data = "Settings saved. Ready to start the adventure."))
            } catch (e: Exception) {
                logger.error("Exception in /set-adventure-settings", e)
                call.respond(HttpStatusCode.InternalServerError, ApiResponse<Any>(success = false, error = e.message))
            }
        }

        post("/start-adventure") {
            try {
                val settings = call.receive<AdventureSettings>()
                logger.info("Starting adventure with settings: $settings")
                val prompt = generateAdventurePrompt(settings)
                logger.info("Generated prompt: $prompt")
                val ollamaRequest = OllamaRequest(model = "deepseek-r1:14b", prompt = prompt)

                val response = client.post("http://localhost:11434/api/generate") {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(OllamaRequest.serializer(), ollamaRequest))
                }

                if (response.status.isSuccess()) {
                    val responseText = response.bodyAsText()
                    logger.info("LLM response: $responseText")
                    call.respondText(responseText)
                } else {
                    logger.error("Error calling Ollama: ${response.status}")
                    call.respond(HttpStatusCode.InternalServerError, "Error calling Ollama")
                }
            } catch (e: Exception) {
                logger.error("Exception in /start-adventure", e)
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}
