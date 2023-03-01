/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosenemune.data

import kotlinx.serialization.Serializable

@Serializable
data class Source(
    val creator: String,
    val language: String,
    val word: String? = null,
    val transliteration: String? = null,
    val definition: String? = null
)
