package com.shinjaehun.answerandquestion.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shinjaehun.answerandquestion.ChatActivity
import com.shinjaehun.answerandquestion.R
import com.shinjaehun.answerandquestion.databinding.ItemProfileBinding
import com.shinjaehun.answerandquestion.model.User

class UserAdapter(var context: Context, var userList: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false)
        return UserViewHolder(v)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.tvUsername.text = user.name
        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.avatar)
            .into(holder.binding.ivProfile)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding : ItemProfileBinding = ItemProfileBinding.bind(itemView)
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
//        val binding = ItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return UserViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
//        holder.bind(userList[position])
//    }
//
//    inner class UserViewHolder(binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root) {
//        private val binding = binding
//        fun bind(user: User) {
//            val username = user.name
//            binding.tvUsername.text = username
//            Glide.with(context).load(user.profileImage).placeholder(R.drawable.avatar).into(binding.ivProfile)
//        }
//    }

}