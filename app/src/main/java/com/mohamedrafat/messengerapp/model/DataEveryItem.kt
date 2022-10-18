package com.mohamedrafat.messengerapp.model

import java.util.*

class DataEveryItem(var userId:String ,
                    var lastMessage:String ,
                    var timeLastMessage: Date ) {

    constructor() : this("" ,"",Date())
}