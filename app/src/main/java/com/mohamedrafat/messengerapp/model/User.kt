package com.mohamedrafat.messengerapp.model

import java.util.*

data class User(val uid:String ,
                var userName:String ,
                var profileImage:String,
                val token:String ,
                val online:Boolean ){

    constructor() : this("","","","", false)

}
