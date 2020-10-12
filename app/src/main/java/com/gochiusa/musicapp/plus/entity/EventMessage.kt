package com.gochiusa.musicapp.plus.entity

data class EventMessage(val messageCode: Int, val message: String?) {
    constructor(messageCode: Int): this(messageCode, null)

    companion object {
        const val PREPARE_MUSIC = 1
        const val BUTTON_TURN_TO_PAUSE = 2
        const val BUTTON_TURN_TO_PLAY = 3
    }
}