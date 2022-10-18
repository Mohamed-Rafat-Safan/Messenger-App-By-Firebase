package com.mohamedrafat.messengerapp.model

import java.util.*

class ContentItemUser(val lastMessage:String,
                      val timeLastMessage: Date,
                      val user: User) {

    constructor() : this("", Date() , User())
}