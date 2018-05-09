package com.github.kory33.util.sponge.ezdata.kotlin.extensions.internal

fun String.splitCamelCase() = fold(listOf("")) { words, char ->
    if (char.isUpperCase()) words + char.toString() else words.mapLast { it + char }
}.filter { it.isNotEmpty() }

fun String.toSnakeCase() = splitCamelCase().joinToString("_").toLowerCase()

fun String.toKebabCase() = splitCamelCase().joinToString("-").toLowerCase()

fun String.toManipulatorId() = "{$this|"
