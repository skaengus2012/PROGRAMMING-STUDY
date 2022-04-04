package com.codespitz.programming.chapter5

import kotlinx.coroutines.*

// routine
// 메모리에 명령과 값이 적재되는데, 반복적으로 사용할 명령의 블럭
// 1. 한번 입장하면 무조건 반환된다.
// 2. 반복적으로 사용할 수 있다.
// 3. 인자를 받아들여 내부 로직에 활용할 수 있다.

// co-routine
// 1. 여러번 진입할 수 있고, 여러번 반환할 수 있다.
// 특수한 반환을 통해 그 다음 진입을 지정할 수 있다.

// 일반 routine : 명령은 적재되면 한번에 다 실행된다.
private fun sequenceMode1000(): Sequence<Int> = sequence {
    for (i in 0..10000000000) {
        yield((i % 1000).toInt())
    }
}

// co-routine :
// 동기명령을 일시적으로 멈추고(suspend), 다시 진입해서 그 다음부터 실행하는(resume) 기능을 갖는다.
// 일반적으로 언어가 지원하지 않으면, 굉장히 복잡한 함수형으로 개발해야함
// async, await : co-routine 은 아님, 그러나 suspend & resume 를 구현.
private fun asyncAndAwait() = runBlocking {
    val number = async { delay(1_000); 5 } // suspend
    print("2. async with suspend -> ")
    // ---------------------------------- resume
    println(number.await() + 3)
}
private data class Json(
    val isEnd: Boolean,
    val data: String
)

// 동기가 이뤄지지 않으면 에러가 발생..
private fun infinityScrollIterator(): Iterator<Json> = iterator {
    var page = 1
    while (true) {
        val json = runBlocking {
            suspend { delay(1_000); Json(isEnd = page >= 10, data = "${page}st. data.")  }()
        }
        if (json.isEnd) break
        else {
            yield(json)
            ++page
        }
    }
}

private fun render(value: String) {
    println(value)
}

private suspend fun pageLoad(pageLoader: () -> Json) {
    val (isEnded, value) = suspend { pageLoader() }()
    if (!isEnded) render(value)
}

private fun testPageLoad() = runBlocking {
    val pageLoader = run {
        val iterator = infinityScrollIterator()
        ({ iterator.next() })
    }

    pageLoad(pageLoader)
    pageLoad(pageLoader)
    pageLoad(pageLoader)
    pageLoad(pageLoader)
    pageLoad(pageLoader)
}


fun main() {
    sequenceMode1000().take(10).fold("") { acc, i -> "$acc, $i" }.run { println("1. [$this]") }
    asyncAndAwait()
    testPageLoad()
}

