package com.codespitz.programming.chapter6

data class Track(
    val trackId: Int,
    val title: String?,
    val artistName: String? = null
) : JsonSerializable {
    override fun toJsonString(): String {
        return "Track(trackId=$trackId, title=$title, artistName=$artistName)"
    }
}