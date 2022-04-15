package com.codespitz.programming.chapter7

/**
 * @author Doohyun
 */
sealed class HtmlObject {
    abstract val rawString: String

    data class HtmlElement(
        val tagName: String,
        override val rawString: String,
        val child: List<HtmlObject> = emptyList(),
        val fields: List<HtmlField> = emptyList()
    ) : HtmlObject() {
        fun withChild(obj: HtmlObject): HtmlElement = copy(
            child = child + obj,
            rawString = rawString + obj.rawString
        )

        data class HtmlField(val name: String, val value: Any?)
    }

    data class HtmlText(val text: String = "") : HtmlObject() {
        override val rawString: String get() = text
    }
}