package com.codespitz.programming.chapter6

data class Track(
    val trackId: Int,
    val title: String?,
    val artistName: String? = null
) : JsonSerializable {
    override fun toJsonString(): String {
        return "Track($trackIdHead${stringify(trackId)}, $titleHead${stringify(title)}, $artistNameHead${stringify(artistName)})"
    }

    class TrackElementParser : ElementParser by JsonSerializableElementParser(
        entireRegex = "\\s*Track\\(.*\\)\\s*".toRegex(),
        map = { (elementString, elementValue) ->
            ElementParseResult.Success(elementString, Track(
                trackId = trackIdRegex.find(elementValue.toString())
                    ?.value
                    ?.replaceFirst(trackIdHead, newValue = "")
                    ?.toInt()
                    ?: 0,
                title = titleRegex.find(elementValue.toString())
                    ?.value
                    ?.replaceFirst(titleHead, newValue = "")
                    ?.let { stringOrNull(it) },
                artistName = artistNameRegex.find(elementValue.toString())
                    ?.value
                    ?.replaceFirst(artistNameHead, newValue = "")
                    ?.let { stringOrNull(it) }

            ))
        }
    ) {
        companion object {
            private val trackIdRegex: Regex = "$trackIdHead[0-9]+".toRegex()
            private val titleRegex: Regex = "$titleHead$stringOrNullRegex".toRegex()
            private val artistNameRegex: Regex = "$artistNameHead((null)|(\".*?((\"\")|([^\\\\\"](\")))))".toRegex()
        }
    }

    companion object {
        private const val trackIdHead = "trackId="
        private const val titleHead = "title="
        private const val artistNameHead = "artistName="
    }
}