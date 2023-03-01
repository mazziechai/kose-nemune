/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosenemune.data

import kotlinx.serialization.Serializable

@Serializable
data class Word(val word: String, val definitions: List<Definition>, val source: Source)
