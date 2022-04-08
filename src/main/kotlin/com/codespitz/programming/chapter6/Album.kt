package com.codespitz.programming.chapter6

class Album(
    val albumId: Int,
    val title: String,
    val tracks: Array<Track>
) : JsonSerializable {
    override fun toJsonString(): String =
        "Album($albumIdHead${stringify(albumId)}, $titleHead${stringify(title)}, $tracksHead${stringify(tracks.map { it.toJsonString() })})"

    class AlbumElementParser : ElementParser by JsonSerializableElementParser(
        entireRegex = "\\s*Album\\(.*\\)\\s*".toRegex(),
        map = { (elementString, elementValue) ->
            ElementParseResult.Success(elementString, Album(
                albumId = albumIdRegex.find(elementValue.toString())
                    ?.value
                    ?.replaceFirst(albumIdHead, newValue = "")
                    ?.toInt()
                    ?: 0,
                title = titleRegex.find(elementValue.toString())
                    ?.value
                    ?.replaceFirst(titleHead, newValue = "")
                    ?.let { stringOrNull(it) }
                    ?: "",
                tracks = tracksRegex.find(elementValue.toString())
                    ?.value
                    ?.replaceFirst(tracksHead, newValue = "")
                    ?.let { str ->
                        parseArray(str, Track.TrackElementParser()).filterIsInstance<Track>().toTypedArray()
                    }
                    ?: emptyArray()
            ))
        }
    ) {
        companion object {
            private val albumIdRegex: Regex = "${albumIdHead}[0-9]+".toRegex()
            private val titleRegex: Regex = "$titleHead$stringOrNullRegex".toRegex()
            private val tracksRegex: Regex = "$tracksHead\\[.*]".toRegex()
        }
    }

    companion object {
        private const val albumIdHead = "albumId="
        private const val titleHead = "title="
        private const val tracksHead = "tracks="
    }
}