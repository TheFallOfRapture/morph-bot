package com.morph.bot

import com.morph.bot.apis.RedditService
import com.morph.bot.commands.chatCommand
import com.morph.bot.util.BotUtils.createClient
import com.morph.bot.util.Config
import io.reactivex.*
import io.reactivex.disposables.Disposable
import kotlinx.serialization.json.JSON
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageBuilder
import sx.blah.discord.util.RequestBuffer
import java.util.concurrent.TimeUnit

class MorphBot(configPath: String) {
    val config: Config = JSON.parse(ClassLoader.getSystemResource(configPath).readText())
    val client = createClient(config.tokens["discord"] as String, login = true)
//    val reddit: RedditService
    val commands = mapOf(
            chatCommand("summon") {
                onCall { e, args ->
                    e.author.getVoiceStateForGuild(e.guild).channel.join()
                }
                helpMsg = "Summons Morph to your current voice channel."
                usageMsg = "${config.prefix}summon"
            },
            chatCommand("banish") {
                onCall { e, args ->
                    client.ourUser.getVoiceStateForGuild(e.guild).channel.leave()
                }
                helpMsg = "Banishes Morph to the abyss."
                usageMsg = "${config.prefix}banish"
            },
            chatCommand("disconnect") {
                onCall { e, args ->
                    sendMsg("Goodbye, everyone!", e.channel)
                    client.logout()
                }
                helpMsg = "Disconnects Morph from the server. :("
                usageMsg = "${config.prefix}disconnect"
            },
            chatCommand("timer") {
                onCall { e, args ->
                    sendMsg("Setting ${e.author.getNicknameForGuild(e.guild)}'s timer for ${args[0]} seconds...", e.channel)
                    Completable.timer(args[0].toLong(), TimeUnit.SECONDS).doOnComplete {
                        sendMsg("Done! ${e.author.mention()}", e.channel)
                        runCommand(args[2], e, args.drop(3).toTypedArray())
                    }.subscribe().apply {
                        timerQueue.add(this)
                    }
                }
                helpMsg = "Starts a timer for a set amount of seconds. Runs optional command if provided."
                usageMsg = "${config.prefix}timer <seconds> [cmd [cmd-args]]"
            },
            chatCommand("timerstop") {
                onCall { e, args ->
                    timerQueue.forEach { it.dispose() }
                    sendMsg("All running timers have been stopped.", e.channel)
                }
                helpMsg = "Stops all running timers."
                usageMsg = "${config.prefix}timerstop"
            }
    )

    val helpCmd = chatCommand("help") {
        onCall { e, args ->
            MessageBuilder(client).withChannel(e.channel).withEmbed(EmbedBuilder().run {
                withColor(127, 0, 0)
                withAuthorIcon(client.ourUser.avatarURL)
                withAuthorName("Morph")
                commands.forEach { cmd -> appendField("${config.prefix}${cmd.key}", "Usage: ${cmd.value.usageMsg}\n${cmd.value.helpMsg}", false)}
                build()
            }).build()
        }
        helpMsg = ""
        usageMsg = ""
    }

    var timerQueue = mutableListOf<Disposable>()

    init {
        client.dispatcher.registerListener(this)

//        reddit = Retrofit
//                .Builder()
//                .baseUrl("https://www.reddit.com")
//                .addConverterFactory(MoshiConverterFactory.create())
//                .build()
//                .create(RedditService::class.java)
    }

    fun sendMsg(msg: String, channel: IChannel) {
        RequestBuffer.request {
            MessageBuilder(client).withContent(msg).withChannel(channel).build()
        }
    }

    @EventSubscriber fun react(e: MessageReceivedEvent) {
        if (e.message.content.startsWith(config.prefix)) {
            val cmdParts = e.message.content.split(" ")
            val cmdName = cmdParts[0].substring(1)
            println(cmdName)
            val cmdArgs = cmdParts.drop(1).toTypedArray()
            if (cmdName == "help") helpCmd.second.action(e, cmdArgs) else runCommand(cmdName, e, cmdArgs)
        }
    }

    fun runCommand(name: String, e: MessageReceivedEvent, args: Array<String>) {
        commands[name]?.action?.invoke(e, args)
    }

    // TODO: This looks ugly, probably going to be removed later
    companion object {
        val VERSION = "0.0.1"
    }
}

fun main(args: Array<String>) {
    val bot = MorphBot("config.json")
    println("Initialized Morph Bot ${MorphBot.VERSION}")
}