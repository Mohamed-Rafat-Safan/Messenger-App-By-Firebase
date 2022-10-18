package com.mohamedrafat.messengerapp.model

import java.util.*

class ContentMessages (val senderId:String,
                       val receiverId:String ,
                       val senderName:String ,
                       val receiverName:String ,
                       val messageType: String,
                       var friendPhoto: String,
                       val messageId: String,
                       val messageText: String,
                       val messageImage: String,
                       val date: Date ) {

    constructor() : this("","","","", "", "", "", "", "", Date())
}
