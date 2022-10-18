package com.mohamedrafat.messengerapp.recyclerView

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.mohamedrafat.messengerapp.ui.ChattingActivity
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.model.ContentMessages
import kotlinx.android.synthetic.main.item_friend_image_message.view.*
import kotlinx.android.synthetic.main.item_friend_text_message.view.*
import kotlinx.android.synthetic.main.item_friend_text_message.view.iv_friendPhoto
import kotlinx.android.synthetic.main.item_friend_text_message.view.tv_friendMessageTime
import kotlinx.android.synthetic.main.item_my_image_message.view.*
import kotlinx.android.synthetic.main.item_my_text_message.view.*
import kotlinx.android.synthetic.main.item_my_text_message.view.tv_myMessageTime

class CustomAdapterForMessages (var listMessage:ArrayList<ContentMessages>, var context: Context , var friendStatus:Boolean): RecyclerView.Adapter<CustomAdapterForMessages.MessageViewHolder>() {
    private val auth: FirebaseAuth by lazy {  FirebaseAuth.getInstance()  }

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    // Variables
    private val myMessageText = 1
    private val myMessageImage = 2

    private val friendMessageText = 3
    private val friendMessageImage = 4




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
         if(viewType == myMessageText)
             return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_my_text_message , parent , false))
        else if(viewType == myMessageImage)
             return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_my_image_message , parent , false))
        else if(viewType == friendMessageText)
             return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_friend_text_message , parent , false))
        else
             return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_friend_image_message , parent , false))
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val contentMyMessage = listMessage[position]
        holder.bind(contentMyMessage)
    }

    override fun getItemViewType(position: Int): Int {
        val messageSenderId  = listMessage[position].senderId
        val messageType = listMessage[position].messageType

        return if(messageSenderId == auth.currentUser!!.uid ){
            if(messageType == ChattingActivity.textMessage)
                myMessageText
            else
                myMessageImage
        } else{
            if(messageType == ChattingActivity.textMessage)
                friendMessageText
            else
                friendMessageImage
        }
    }

    override fun getItemCount(): Int {
        return listMessage.size
    }



    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(contentMessages: ContentMessages){
            if(contentMessages.senderId == auth.currentUser!!.uid){
                val myMessageTime = itemView.tv_myMessageTime
                if(contentMessages.messageType == ChattingActivity.textMessage){
                    val myMessage = itemView.tv_myTextMessage!!
                    myMessage.text = contentMessages.messageText
                }else{
                    val myImageMessage = itemView.iv_my_imageMessage
                    getImageAndItShow(contentMessages.messageImage , myImageMessage)
                }
                myMessageTime.text = DateFormat.format("hh:mm a" , contentMessages.date).toString()
            }else{
                val friendImage = itemView.iv_friendPhoto
                val friendMessageTime = itemView.tv_friendMessageTime

                getImageAndItShow(contentMessages.friendPhoto , friendImage)

                if(contentMessages.messageType == ChattingActivity.textMessage){
                    val friendTextMessage = itemView.tv_friendMessageText
                    val friendStatusTM = itemView.shape_online_textMessage
                    friendTextMessage.text = contentMessages.messageText

                    if(friendStatus)
                        friendStatusTM.visibility = View.VISIBLE
                    else
                        friendStatusTM.visibility = View.INVISIBLE
                }else{
                    val friendImageMessage = itemView.iv_friend_imageMessage
                    val friendStatusIM = itemView.shape_online_imageMessage
                    getImageAndItShow(contentMessages.messageImage , friendImageMessage)
                    if(friendStatus)
                        friendStatusIM.visibility = View.VISIBLE
                    else
                        friendStatusIM.visibility = View.INVISIBLE
                }
                friendMessageTime.text = DateFormat.format("hh:mm a" , contentMessages.date).toString()
            }
        }


        private fun getImageAndItShow(imagePath:String , imageView:ImageView){
            if(imagePath.isNotEmpty()){
                Glide.with(context)
                    .load(storageInstance.getReference(imagePath))
                    .into(imageView)
            }else{
                imageView.setImageResource(R.drawable.man)
            }
        }

    }

}