/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosenemune.data

import kotlinx.serialization.Serializable

@Serializable
data class Definition(val partOfSpeech: String, val meaning: String)
