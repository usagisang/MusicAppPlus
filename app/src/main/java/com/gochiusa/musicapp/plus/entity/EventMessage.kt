package com.gochiusa.musicapp.plus.entity

data class EventMessage(val messageCode: Int, val message: String?) {
    constructor(messageCode: Int): this(messageCode, null)
}