package com.example.jetnews.data.gemoni.impl

import com.example.jetnews.data.Result
import com.example.jetnews.data.posts.PostsRepository
import com.example.jetnews.model.Post
import com.example.jetnews.model.PostsFeed
import com.example.jetnews.utils.addOrRemove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.jetnews.BuildConfig
import com.example.jetnews.model.Metadata
import com.example.jetnews.model.PostAuthor
import com.example.jetnews.R
import com.example.jetnews.model.Paragraph
import com.example.jetnews.model.ParagraphType

class NaverPostsRepository : PostsRepository {

    private val favorites = MutableStateFlow<Set<String>>(setOf())
    private val postsFeed = MutableStateFlow<PostsFeed?>(null)

    override suspend fun getPost(postId: String?): Result<Post> {
        return withContext(Dispatchers.IO) {
            val post = postsFeed.value?.allPosts?.find { it.id == postId }
            if (post == null) {
                Result.Error(IllegalArgumentException("Post not found"))
            } else {
                Result.Success(post)
            }
        }
    }

    override suspend fun getPostsFeed(): Result<PostsFeed> {
        return withContext(Dispatchers.IO) {
            try {
                val clientId = "GB5e1KFo7O26sUVzzjli"
                val clientSecret = "cZ0FJjz1D1"
                val apiURL = "https://openapi.naver.com/v1/search/news.json?query=ai"
                val url = URL(apiURL)
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "GET"
                con.setRequestProperty("X-Naver-Client-Id", clientId)
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret)

                val responseCode = con.responseCode
                val br: BufferedReader
                if (responseCode == 200) { // 정상 호출
                    br = BufferedReader(InputStreamReader(con.inputStream))
                } else {  // 에러 발생
                    br = BufferedReader(InputStreamReader(con.errorStream))
                }
                var inputLine: String?
                val response = StringBuffer()
                while (br.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                br.close()

                val jsonResponse = JSONObject(response.toString())
                val items = jsonResponse.getJSONArray("items")
                val posts = mutableListOf<Post>()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    posts.add(
                        Post(
                            id = item.getString("link"),
                            title = item.getString("title").replace(Regex("<.*?>"), ""),
                            subtitle = item.getString("description").replace(Regex("<.*?>"), ""),
                            url = item.getString("link"),
                            metadata = Metadata(
                                author = PostAuthor( "Naver News",null),
                                date = item.getString("pubDate"),
                                readTimeMinutes = 5 // Placeholder
                            ),
                            imageId = R.drawable.post_1, // Placeholder
                            imageThumbId = R.drawable.post_1_thumb, // Placeholder
                            paragraphs = listOf(
                                Paragraph(
                                    type = ParagraphType.Text,
                                    text = item.getString("description").replace(Regex("<.*?>"), ""),
                                    content = item.getString("description").replace(Regex("<.*?>"), "")
                                )
                            )
                        )
                }
                val feed = PostsFeed(
                    highlightedPost = posts.first(),
                    recommendedPosts = posts.subList(1, minOf(posts.size, 5)),
                    popularPosts = posts.subList(minOf(posts.size, 5), minOf(posts.size, 10)),
                    recentPosts = posts.subList(minOf(posts.size, 10), posts.size)
                )
                postsFeed.update { feed }
                Result.Success(feed)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override fun observeFavorites(): Flow<Set<String>> = favorites
    override fun observePostsFeed(): Flow<PostsFeed?> = postsFeed

    override suspend fun toggleFavorite(postId: String) {
        favorites.update {
            it.addOrRemove(postId)
        }
    }
}