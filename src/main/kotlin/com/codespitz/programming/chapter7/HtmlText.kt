package com.codespitz.programming.chapter7

/**
 * @author Doohyun
 */
data class HtmlText(val text: String = "") : HtmlObject {
    override val rawString: String get() = text
}