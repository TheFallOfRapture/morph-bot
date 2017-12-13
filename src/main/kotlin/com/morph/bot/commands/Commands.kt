package com.morph.bot.commands

import sx.blah.discord.api.events.Event
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

import kotlinx.serialization.Serializable

typealias Args = Array<String>

class Command<in T : Event>(val action: (T, Args) -> Unit, val helpMsg: String, val usageMsg: String)

typealias ChatCommand = Command<MessageReceivedEvent>

class CommandBuilder<T: Event> {
    lateinit var action: (T, Args) -> Unit
    lateinit var helpMsg: String
    lateinit var usageMsg: String

    fun onCall(block: (T, Args) -> Unit): CommandBuilder<T> {
        action = block
        return this
    }

    fun withHelpMessage(helpMsg: String): CommandBuilder<T> {
        this.helpMsg = helpMsg
        return this
    }

    fun withUsageMessage(usageMsg: String): CommandBuilder<T> {
        this.usageMsg = usageMsg
        return this
    }

    fun build(): Command<T> {
        return Command(action, helpMsg, usageMsg)
    }
}

inline fun <reified T: Event> command(commandName: String, block: CommandBuilder<T>.() -> Unit): Pair<String, Command<T>> {
    val builder = CommandBuilder<T>()
    builder.block()
    return commandName to builder.build()
}

inline fun chatCommand(commandName: String, block: CommandBuilder<MessageReceivedEvent>.() -> Unit): Pair<String, ChatCommand> = command(commandName, block)