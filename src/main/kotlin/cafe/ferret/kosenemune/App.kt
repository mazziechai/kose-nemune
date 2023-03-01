/*
 * Copyright (c) 2023 mazziechai
 */

package cafe.ferret.kosenemune

import cafe.ferret.kosenemune.extensions.KataExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.Event

val TEST_SERVER_ID = Snowflake(
    env("TEST_SERVER").toLong()
)

val ENVIRONMENT = env("ENVIRONMENT")

private val token = env("TOKEN")

suspend fun main() {
    val bot = ExtensibleBot(token) {
        applicationCommands {
            if (ENVIRONMENT == "dev") {
                defaultGuild(TEST_SERVER_ID)
            }
        }

        extensions {
            add(::KataExtension)
        }
    }

    bot.start()
}

suspend fun <T : Event> CheckContext<T>.isDeveloper(arg: suspend () -> Snowflake) {
    if (!passed) {
        return
    }

    val id = arg()

    if (id != Snowflake(env("DEVELOPER").toLong())) {
        fail()
    } else {
        pass()
    }
}