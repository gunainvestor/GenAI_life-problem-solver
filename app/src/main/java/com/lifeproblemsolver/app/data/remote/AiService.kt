package com.lifeproblemsolver.app.data.remote

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import com.google.gson.annotations.SerializedName

interface AiService {
    @POST("generate-solution")
    suspend fun generateSolution(@Body request: SolutionRequest): SolutionResponse
}

data class SolutionRequest(
    val problem: String,
    val context: String = "",
    val category: String = "General"
)

data class SolutionResponse(
    val solution: String,
    val frameworks: List<String> = emptyList(),
    val confidence: Double = 0.8
)

// Mock implementation for now - in a real app, this would connect to OpenAI, Claude, or similar
class MockAiService : AiService {
    override suspend fun generateSolution(request: SolutionRequest): SolutionResponse {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)
        
        val solutions = mapOf(
            "schedule" to SolutionResponse(
                solution = "Here's a comprehensive approach to scheduling your week:\n\n" +
                    "1. **Time Blocking Method**: Allocate specific time blocks for different activities\n" +
                    "2. **Priority Matrix**: Use Eisenhower's Urgent/Important matrix\n" +
                    "3. **Batch Similar Tasks**: Group similar activities together\n" +
                    "4. **Buffer Time**: Leave 20% buffer for unexpected tasks\n" +
                    "5. **Weekly Review**: Set aside time each Sunday to plan the next week\n\n" +
                    "**Recommended Tools**:\n" +
                    "• Google Calendar for time blocking\n" +
                    "• Notion for task management\n" +
                    "• Forest app for focus sessions",
                frameworks = listOf("Time Blocking", "Eisenhower Matrix", "Pomodoro Technique", "Getting Things Done"),
                confidence = 0.9
            ),
            "project" to SolutionResponse(
                solution = "Starting a side project while working full-time requires careful planning:\n\n" +
                    "1. **Assess Your Capacity**: Start with 5-10 hours per week\n" +
                    "2. **Choose the Right Project**: Pick something you're passionate about\n" +
                    "3. **Set Realistic Goals**: Break down into small, achievable milestones\n" +
                    "4. **Create a Dedicated Schedule**: Block specific times (early morning, weekends)\n" +
                    "5. **Use Productivity Techniques**: Pomodoro, time blocking, deep work\n" +
                    "6. **Track Progress**: Use tools like GitHub, Trello, or Notion\n\n" +
                    "**Success Framework**:\n" +
                    "• MVP (Minimum Viable Product) approach\n" +
                    "• Agile methodology with 2-week sprints\n" +
                    "• Regular retrospectives to improve process",
                frameworks = listOf("MVP", "Agile", "Scrum", "Lean Startup"),
                confidence = 0.85
            ),
            "work" to SolutionResponse(
                solution = "Managing full-time work effectively involves several strategies:\n\n" +
                    "1. **Set Clear Boundaries**: Define work hours and stick to them\n" +
                    "2. **Prioritize Tasks**: Use the 80/20 rule - focus on high-impact activities\n" +
                    "3. **Optimize Your Environment**: Create a distraction-free workspace\n" +
                    "4. **Take Regular Breaks**: Follow the 52/17 rule or Pomodoro technique\n" +
                    "5. **Continuous Learning**: Dedicate time for skill development\n" +
                    "6. **Health First**: Maintain work-life balance and physical health\n\n" +
                    "**Productivity Frameworks**:\n" +
                    "• Deep Work by Cal Newport\n" +
                    "• Getting Things Done (GTD)\n" +
                    "• The One Thing methodology",
                frameworks = listOf("Deep Work", "Getting Things Done", "The One Thing", "Pomodoro"),
                confidence = 0.88
            )
        )
        
        val problemLower = request.problem.lowercase()
        return when {
            problemLower.contains("schedule") || problemLower.contains("time") -> solutions["schedule"]!!
            problemLower.contains("project") || problemLower.contains("side") -> solutions["project"]!!
            problemLower.contains("work") || problemLower.contains("job") -> solutions["work"]!!
            else -> SolutionResponse(
                solution = "Based on your problem: \"${request.problem}\"\n\n" +
                    "Here's a structured approach to solve this:\n\n" +
                    "1. **Define the Problem Clearly**: Write down exactly what you're trying to solve\n" +
                    "2. **Break It Down**: Divide the problem into smaller, manageable parts\n" +
                    "3. **Research Solutions**: Look for existing frameworks and methodologies\n" +
                    "4. **Create an Action Plan**: Develop a step-by-step approach\n" +
                    "5. **Implement and Iterate**: Start small and improve based on results\n" +
                    "6. **Track Progress**: Monitor your progress and adjust as needed\n\n" +
                    "**General Problem-Solving Framework**:\n" +
                    "• PDCA (Plan-Do-Check-Act) cycle\n" +
                    "• 5 Whys technique\n" +
                    "• Design Thinking process",
                frameworks = listOf("PDCA", "5 Whys", "Design Thinking", "Root Cause Analysis"),
                confidence = 0.75
            )
        }
    }
}

class OpenAiService(private val apiKey: String) : AiService {
    private val api: OpenAiApi
    private val TAG = "OpenAiService"

    init {
        Log.d(TAG, "Initializing OpenAiService with API key: ${apiKey.take(10)}...")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(OpenAiApi::class.java)
        Log.d(TAG, "Retrofit client created successfully")
    }

    override suspend fun generateSolution(request: SolutionRequest): SolutionResponse {
        Log.d(TAG, "Starting generateSolution for problem: ${request.problem}")
        
        try {
            val prompt = buildPrompt(request)
            Log.d(TAG, "Built prompt: $prompt")
            
            val openAiRequest = OpenAiRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    OpenAiMessage(role = "system", content = "You are a helpful assistant that gives structured, actionable solutions to life problems, referencing well-known frameworks where possible."),
                    OpenAiMessage(role = "user", content = prompt)
                ),
                max_tokens = 512,
                temperature = 0.7
            )
            Log.d(TAG, "Created OpenAI request with model: ${openAiRequest.model}")
            
            Log.d(TAG, "Making API call to OpenAI...")
            val response = api.chatCompletions(
                apiKey = "Bearer $apiKey",
                request = openAiRequest
            )
            Log.d(TAG, "Received response from OpenAI")
            
            val content = response.choices.firstOrNull()?.message?.content ?: "No solution generated."
            Log.d(TAG, "Extracted content length: ${content.length}")
            Log.d(TAG, "Content preview: ${content.take(100)}...")
            
            return SolutionResponse(
                solution = content,
                frameworks = emptyList(), // Optionally, parse frameworks from content
                confidence = 0.95
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error calling OpenAI API", e)
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            
            // Return a fallback response with error information
            return SolutionResponse(
                solution = "Sorry, I couldn't generate a solution due to an error: ${e.message}. Please check your internet connection and try again.",
                frameworks = emptyList(),
                confidence = 0.0
            )
        }
    }

    private fun buildPrompt(request: SolutionRequest): String {
        return buildString {
            append("Problem: ")
            append(request.problem)
            if (request.context.isNotBlank()) {
                append("\nContext: ")
                append(request.context)
            }
            if (request.category.isNotBlank()) {
                append("\nCategory: ")
                append(request.category)
            }
            append("\n\nPlease provide a structured, actionable solution, referencing any well-known frameworks or methodologies that could help.")
        }
    }

    interface OpenAiApi {
        @POST("chat/completions")
        suspend fun chatCompletions(
            @Header("Authorization") apiKey: String,
            @Body request: OpenAiRequest
        ): OpenAiResponse
    }

    data class OpenAiRequest(
        val model: String,
        val messages: List<OpenAiMessage>,
        val max_tokens: Int = 512,
        val temperature: Double = 0.7
    )

    data class OpenAiMessage(
        val role: String,
        val content: String
    )

    data class OpenAiResponse(
        val choices: List<OpenAiChoice>
    )

    data class OpenAiChoice(
        val message: OpenAiMessage
    )
} 