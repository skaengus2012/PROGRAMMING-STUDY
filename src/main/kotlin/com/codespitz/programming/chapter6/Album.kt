package com.codespitz.programming.chapter6

class Album(
    val albumId: Int,
    val title: String,
    val tracks: Array<Track>
) : JsonSerializable {
    override fun toJsonString(): String = "Album(albumId=$albumId, title='$title', tracks=${stringify(tracks.map { it.toJsonString() })})"
}