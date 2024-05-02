package com.pay2.exhangeapp.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object CoroutineDispatchersProvider : CoroutineDispatchers {

    override val main = Dispatchers.Main

    override val io = Dispatchers.IO

    override val default = Dispatchers.Default

    override val immediate = Dispatchers.Main.immediate

    override val computation = Dispatchers.Default
}

interface CoroutineDispatchers {

    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val immediate: CoroutineDispatcher
    val computation: CoroutineDispatcher
}