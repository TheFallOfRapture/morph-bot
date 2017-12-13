package com.morph.bot.util

import sx.blah.discord.api.ClientBuilder

object BotUtils {
    fun createClient(token: String, login: Boolean) = ClientBuilder().withToken(token).run {
        if (login) login() else build()
    }
}