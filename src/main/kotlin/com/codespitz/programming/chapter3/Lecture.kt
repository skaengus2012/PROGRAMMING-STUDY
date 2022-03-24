package com.codespitz.programming.chapter3

// 코드를 작성할 때 설계 또는 디자인이란?
// 유지보수 기능 추가 등에 유리하도록(변화율이 동일한코드 끼리 묶어서 ocp를 이루도록) 코드(함수, 클래스, 객체 등의 구조를 이용해서)를 재배치하는 것.

// OCP - 수정에는 닫혀있고, 확장에는 열려있다.
//     - OCP 는 다양한 레벨에서 일어남 (모듈,코드,패키지....)
//     - 수업 목적 : 코드레벨의 OCP (코드)수정에는 닫혀있고, (케이스 - switch 의 병렬조건, (if-else 는 binary mandatory))확장에는 열려있다.
//                 switch 를 쓰지 않는다. 혹은 if-else 인줄 알았는데, 내일이 오니 3개의 케이스였어.. 따라서 if else 도 대상.

// 케이스를 추가하지만, 대상 코드는 수정이 안됨.
// 가장 쉽게 OCP 를 작성하는 방법은 라우터와 라우팅테이블을 만드는 것. switch 를 만나면, 라우터와 라우팅 테이블을 만든다.
fun main() {
    println(arrayStringifyWithWhileOcp(arrayOf(1, true, "ab\"c\n\t", null, { 3 })))
}

val arrayStringifyWithWhileOcp: (Array<out Any?>) -> String = run {
    val stringStringify: (String) -> String = run {
        // 라우팅 테이블 : 라우터보다 상대적으로 자주바뀜
        val table = arrayOf(
            "\"".toRegex() to "\\\\\"",
            "\t".toRegex() to "\\\\t",
            "(\r\n|\n\r|\n|\r)".toRegex() to "\\\\n",
        )

        // 라우터 : 테이블이 확장되는 동안 라우터는 수정하지 않아도 됨.
        // 라우터를 수정하면 OCP 가 깨진다? 테이블까지 전면 검토 필요 === 트랜잭션
        ({ str ->
            table.fold(str) { acc, (regex, replacement) -> acc.replace(regex = regex, replacement = replacement) }
        })
    }

    fun valueOf(any: Any?): String = when (any) {
        is Number, is Boolean -> any.toString()
        is String -> "\"${stringStringify(any)}\""
        else -> "null"
    }

    val resultProcess: (Array<out Any?>) -> String = run {
        // if 문을 식(값)으로 변경 -> 값을 처리형식의 함수로 변경
        // OCP 는 Command 패턴, 제어(문) -> 식, how? Command 객체로 만듦, 메소드가 하나뿐인 커맨드 객체가 람다.
        // 1.모든 분기는 반드시 라우터와 라우팅테이블로 대체할 수 있다.
        //   라우터는 제어를 갖게된다. 제어가 옮겨졌을 뿐, 사라진게 아님. (IoC) 변화율이 라우팅 테이블보다 드문드문 발생한다.
        //   IoC : 코드가 제어에 직접적으로 관여하지 않고, 제어를 한군데로 모음, 함수 역시도 제어를 추출한 것.
        // 2.라우터의 조건은 mandatory 여야 함.

        // 역할모델은 어떻게 코드의 역할을 나눌 것인가?
        // 정답 변화율. 그 코드는 어떤 이유로 바뀔 것인가. 유지보수 요령
        // 1.정단한 if 는 절대로 사라지지 않는다.
        // 2.if의 단계별 구성요소를 분석해서, 변화율에 따라 OCP를 준수할 수 있는 라우터와 라우팅테이블로 번역한다.
        //   왜? 유지보수가 변화율에 따라 OCP 준수해서 관리성이 좋아. -> 코드에는 정답이 있음.
        val table = mapOf(
            true to { "[]" },
            false to { arr: Array<out Any?> ->
                var acc = ""
                var index = 0
                while (index < arr.size) {
                    acc = "$acc,${valueOf(arr[index])}"
                    index = index + 1
                }
                "[${acc.substring(startIndex = 1)}]"
            }
        )
        val err = { _: Array<out Any?> -> throw IllegalArgumentException() }

        ({ arr -> table.getOrDefault(arr.isEmpty(), err) (arr) })
    }

    ({ arr -> resultProcess(arr) })
}