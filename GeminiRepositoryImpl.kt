import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.example.jetnews.model.Post
import com.example.jetnews.data.gemini.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepositoryImpl : GeminiRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyCdL_cBKpMagGUMRQT9WcvNtWDnlK1vXc0"
    )

    override suspend fun summarizePost(post: Post): String? = withContext(Dispatchers.IO) {
        try {
            val textToSummarize = buildString {
                append("제목: ${post.title}\n")
                append("내용:\n")
                post.paragraphs.forEach {
                    append(it.text).append("\n")
                }
                append("위의 내용을 3문장 이내로 요약해줘.")
            }

            val response = generativeModel.generateContent(
                content{(textToSummarize)}
            )

            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
