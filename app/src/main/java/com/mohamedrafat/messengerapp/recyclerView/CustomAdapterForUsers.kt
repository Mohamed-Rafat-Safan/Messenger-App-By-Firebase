package com.mohamedrafat.messengerapp.recyclerView

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.mohamedrafat.messengerapp.ui.ChattingActivity
import com.mohamedrafat.messengerapp.R
import com.mohamedrafat.messengerapp.model.ContentItemUser
import com.mohamedrafat.messengerapp.model.User
import kotlinx.android.synthetic.main.recycler_view_item.view.*

class CustomAdapterForUsers(var listChat:ArrayList<ContentItemUser>, var context: Context, var showType:Int): RecyclerView.Adapter<CustomAdapterForUsers.MessengerViewHolder>() {
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessengerViewHolder {
        if(showType == 1)
            return MessengerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item , parent , false))
        else
            return MessengerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item_1 , parent , false))
    }

    override fun onBindViewHolder(holder: MessengerViewHolder, position: Int) {
        val userData = listChat[position]
        holder.bind(userData)

        holder.itemView.setOnClickListener{
            val intent = Intent(context , ChattingActivity::class.java)
            intent.putExtra("uId" , userData.user.uid)
            intent.putExtra("friendName" , userData.user.userName)
            intent.putExtra("friendImage" , userData.user.profileImage)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listChat.size
    }



    inner class MessengerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(contentItemUser: ContentItemUser){
            val shapeOnline = itemView.shape_online_forUser

            if(showType==1){
                val userName = itemView.tv_item_userName
                val image = itemView.iv_person
                val lastMessage = itemView.tv_item_lastMessage
                val time = itemView.tv_item_timeLastMessage

                userName.text = contentItemUser.user.userName
                getImageAndItShow(contentItemUser.user.profileImage , image)

                if(contentItemUser.lastMessage.isEmpty()){
                    lastMessage.text = ""
                    time.text = ""
                }else{
                    lastMessage.text = "..."+contentItemUser.lastMessage
                    time.text = DateFormat.format("hh:mm a" , contentItemUser.timeLastMessage)
                }

            }else{
                val userName = itemView.tv_item_userName
                val image = itemView.iv_person
                userName.text = contentItemUser.user.userName
                getImageAndItShow(contentItemUser.user.profileImage , image)
            }

            if(contentItemUser.user.online)
                shapeOnline.visibility = View.VISIBLE
            else
                shapeOnline.visibility = View.INVISIBLE

        }  // end method bind

        private fun getImageAndItShow(imagePath:String , imageView: ImageView){
            if(imagePath.isNotEmpty()){
                Glide.with(context)
                    .load(storageInstance.getReference(imagePath))
                    .into(imageView)
            }else{
                imageView.setImageResource(R.drawable.man)
            }
        }


    } // end class viewHolder

}