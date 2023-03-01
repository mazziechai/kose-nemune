/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosenemune.extensions

import cafe.ferret.kosenemune.data.Word
import cafe.ferret.kosenemune.isDeveloper
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.linkButton
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.types.edit
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import com.kotlindiscord.kord.extensions.utils.suggestStringCollection
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.interaction.response.PublicMessageInteractionResponse
import dev.kord.rest.builder.message.modify.embed
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.xdrop.fuzzywuzzy.FuzzySearch
import mu.KotlinLogging
import kotlin.time.Duration.Companion.minutes

class KataExtension : Extension() {
    override val name = "kata"

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    private lateinit var words: List<Word>
    private val scheduler = Scheduler()
    private lateinit var kataTask: Task

    private val logger = KotlinLogging.logger { }

    override suspend fun setup() {
        kataTask = scheduler.schedule(delay = 10.minutes, startNow = true, repeat = true) {
            words = httpClient.get("https://kata.nimi.li/words").body()
            logger.debug { "Received word data!" }
        }

        kataTask.callNow()

        publicSlashCommand(::KataCommandArgs) {
            name = "kata"
            description = "Gets information about a word, including definitions"

            action {
                val kata = words.find { it.word == arguments.word }

                if (kata == null) {
                    respondEphemeral {
                        content = "I couldn't find that word."
                    }

                    return@action
                }

                kataDescription(kata, user.id)
            }
        }

        ephemeralSlashCommand {
            name = "refresh"
            description = "Developer only command to refresh kata now."

            check { isDeveloper { event.interaction.user.id } }

            action {
                kataTask.callNow()

                respond {
                    content = "Refreshed kata!"
                }
            }
        }
    }

    inner class KataCommandArgs : Arguments() {
        val word by string {
            name = "word"
            description = "The word to get"

            autoComplete {
                suggestStringCollection(
                    FuzzySearch.extractTop(focusedOption.value, words.map { it.word }, 5).map { it.string })
            }
        }
    }

    private suspend fun PublicInteractionContext.kataDescription(
        kata: Word,
        user: Snowflake
    ): PublicMessageInteractionResponse {
        return edit {
            embed {
                title = kata.word

                kata.definitions.forEach { definition ->
                    field {
                        name = definition.partOfSpeech
                        value = definition.meaning

                        inline = true
                    }
                }

                field {
                    name = "source"
                    value = buildString {
                        append("← ${kata.source.language} ")
                        if (kata.source.word != null) {
                            append("*${kata.source.word}* ")
                        }
                        if (kata.source.transliteration != null) {
                            append("(${kata.source.transliteration}) ")
                        }
                        if (kata.source.definition != null) {
                            append("‘${kata.source.definition}’")
                        }
                        append("\ncoined by *${kata.source.creator}*")
                    }
                }
            }

            components {
                linkButton {
                    label = "kata.nimi.li page"
                    url = "https://kata.nimi.li/${kata.word}"
                }

                publicButton {
                    label = "remove this"
                    style = ButtonStyle.Danger

                    check { failIf(event.interaction.user.id != user) }

                    action {
                        edit {
                            content = "(removed)"
                            embeds = mutableListOf()
                            components = mutableListOf()
                        }
                    }
                }
            }
        }
    }
}
