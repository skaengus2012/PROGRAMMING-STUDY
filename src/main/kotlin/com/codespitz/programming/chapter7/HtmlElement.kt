package com.codespitz.programming.chapter7

/**
 * @author Doohyun
 */
data class HtmlElement(
    val tagName: String,
    override val rawString: String,
    val child: List<HtmlObject> = emptyList(),
    val fields: List<HtmlField> = emptyList()
) : HtmlObject