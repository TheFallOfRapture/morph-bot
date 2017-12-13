package com.morph.bot.util

import kotlinx.serialization.Serializable

@Serializable data class Config(val tokens: Map<String, String>, val prefix: String)