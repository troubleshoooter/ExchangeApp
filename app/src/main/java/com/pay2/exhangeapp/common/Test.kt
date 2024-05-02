package com.pay2.exhangeapp.common

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    GlobalScope.launch{
        println(sum(10,30))
    }

}

suspend fun sum(a:Int, b:Int):Int {
    return a+b
}