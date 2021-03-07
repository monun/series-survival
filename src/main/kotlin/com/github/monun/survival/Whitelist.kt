package com.github.monun.survival

import com.google.common.collect.ImmutableSortedSet
import java.io.File

object Whitelist {
    lateinit var allows: Set<String>

    fun load(file: File) {
        if (!file.exists()) {
            file.createNewFile()
        }

        val lines = file.readLines()
        allows = ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, lines)
    }
}