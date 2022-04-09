package com.codespitz.programming.chapter7

import com.codespitz.programming.chapter6.*

// Custom 객체에 대한 리뷰.
data class Track(
    val trackId: Int,
    val title: String?,
    val artistName: String? = null
) : JsonSerializable {
    override fun toJsonString(): String {
        return TrackElementParser.toJsonString(trackId, title, artistName)
    }

    // 마샬링/언마샬링은 같은 논리이기 때문에, 한곳에 있는 것이 좋음.
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

            fun toJsonString(trackId: Int, title: String?, artistName: String?): String {
                return "Track($trackIdHead${stringify(trackId)}, $titleHead${stringify(title)}, $artistNameHead${stringify(artistName)})"
            }
        }
    }

    companion object {
        private const val trackIdHead = "trackId="
        private const val titleHead = "title="
        private const val artistNameHead = "artistName="
    }
}

fun main() {
    val a = "\"([^\"]|(\\\\\"))*\"".toRegex()
    val b = "\"((\\\\\")|[^\"])*\"".toRegex()
    val p = "\"\\\"\""

    println(StringElementParser().convert(p))
    println(a.find(p)?.value)
    println(b.find(p)?.value)
}