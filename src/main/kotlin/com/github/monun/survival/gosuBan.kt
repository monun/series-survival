package com.github.monun.survival

import com.google.common.collect.ImmutableSortedSet
import java.io.File

object Gosuban {
    lateinit var denied: Set<String>

    fun ban(file: File) {
        if(!file.exists()) {
            file.createNewFile()
        }
        val lines = file.readLines()
        denied = ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, lines)
    }
}