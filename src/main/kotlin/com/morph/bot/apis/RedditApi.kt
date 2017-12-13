package com.morph.bot.apis

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

class RedditNewsDataResponse(
        val author: String,
        val title: String,
        val num_comments: Int,
        val created: Long,
        val thumbnail: String,
        val url: String
)

interface RedditService {
    @GET("/top.json")
    fun getTop(@Query("after") after: String,
               @Query("limit") limit: String)
    : Call<RedditNewsDataResponse>
}