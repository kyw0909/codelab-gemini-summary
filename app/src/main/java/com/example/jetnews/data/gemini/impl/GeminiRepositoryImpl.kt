package com.example.jetnews.data.gemini.impl

import android.util.Log
import com.example.jetnews.data.gemini.GeminiRepository
import com.example.jetnews.model.Post
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.InvalidStateException
import com.google.ai.client.generativeai.type.PromptBlockedException
import com.google.ai.client.generativeai.type.SerializationException
import com.google.ai.client.generativeai.type.ServerException

class GeminiRepositoryImpl: GeminiRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = "AIzaSyAVlGMy5snFQfKACEiPysTfwuMgGq5cV08"
    )

    override suspend fun summarizePost(post: Post): String? {
        val prompt = "다음 뉴스를 한글로 한 문단으로 요약해줘: ${post.title}"

        Log.d("GeminiRepo", "요약 요청 시작. 모델: ${generativeModel.modelName}, 프롬프트: \"$prompt\"")

        return try {
            val response = generativeModel.generateContent(prompt)
            val summary = response.text

            if (summary.isNullOrBlank()) {
                Log.w("GeminiRepo", "요약 성공했으나, 응답 내용이 비어있습니다.")
                "요약 내용을 생성할 수 없습니다."
            } else {
                Log.i("GeminiRepo", "요약 성공. 내용: $summary")
                summary
            }

        } catch (e: Exception) {
            when (e) {
                is PromptBlockedException -> Log.e("GeminiRepo", "프롬프트가 차단되었습니다.", e)
                is SerializationException -> Log.e("GeminiRepo", "요청/응답 직렬화 오류.", e)
                is ServerException -> Log.e("GeminiRepo", "Gemini 서버 오류.", e)
                is InvalidStateException -> Log.e("GeminiRepo", "잘못된 요청 상태 오류.", e)
                else -> Log.e("GeminiRepo", "알 수 없는 오류 발생.", e)
            }
            "요약 중 오류가 발생했습니다: ${e.localizedMessage}"
        }
    }
}
