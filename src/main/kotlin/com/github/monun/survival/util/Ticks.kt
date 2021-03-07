package com.github.monun.survival.util

object Ticks {
    private val INIT_TIME = System.nanoTime()

    fun currentTicks(): Long {
        return (System.nanoTime() - INIT_TIME) / 50000000L
    }
}