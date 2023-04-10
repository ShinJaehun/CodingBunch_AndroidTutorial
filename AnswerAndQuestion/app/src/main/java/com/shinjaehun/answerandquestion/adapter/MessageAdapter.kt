package com.shinjaehun.answerandquestion.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.shinjaehun.answerandquestion.R
import com.shinjaehun.answerandquestion.databinding.DeleteLayoutBinding
import com.shinjaehun.answerandquestion.databinding.ReceiveMsgBinding
import com.shinjaehun.answerandquestion.databinding.SendMsgBinding
import com.shinjaehun.answerandquestion.model.Message

private const val TAG = "MessageAdapter"

class MessageAdapter(
    val context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    lateinit var messages: ArrayList<Message>

    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val senderRoom: String
    val receiverRoom: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentMsgHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.javaClass == SentMsgHolder::class.java) {
            val viewHolder = holder as SentMsgHolder
            Log.i(TAG, "sent message : ${message.message}")

            if (message.message.equals("photo")) {
                viewHolder.binding.ivImage.visibility = View.VISIBLE
                viewHolder.binding.tvMessage.visibility = View.GONE
                viewHolder.binding.llMessage.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.ivImage)
            } else {
                // 이렇게 하지 않으면 이미지를 보낸 후에 다시 어떤 메시지를 넣을때 계속 이미지가 반복됨
                // 좀 더 깔끔하게 해결할 수 있는 방법이 없을까?
                viewHolder.binding.tvMessage.text = message.message
                viewHolder.binding.ivImage.visibility = View.GONE
                viewHolder.binding.tvMessage.visibility = View.VISIBLE
                viewHolder.binding.llMessage.visibility = View.VISIBLE
            }

            viewHolder.itemView.setOnClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                binding.tvEveryone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()
                }

                binding.tvDelete.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }

                binding.tvCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
                false
            }
        } else {
            val viewHolder = holder as ReceiveMsgHolder
            Log.i(TAG, "receive message : ${message.message}")

            if (message.message.equals("photo")) {
                viewHolder.binding.ivImage.visibility = View.VISIBLE
                viewHolder.binding.tvMessage.visibility = View.GONE
                viewHolder.binding.llMessage.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.ivImage)
            } else {
                // 이렇게 하지 않으면 이미지를 보낸 후에 다시 어떤 메시지를 넣을때 계속 이미지가 반복됨
                // 좀 더 깔끔하게 해결할 수 있는 방법이 없을까?
                viewHolder.binding.tvMessage.text = message.message
                viewHolder.binding.ivImage.visibility = View.GONE
                viewHolder.binding.tvMessage.visibility = View.VISIBLE
                viewHolder.binding.llMessage.visibility = View.VISIBLE
            }

//            viewHolder.binding.tvMessage.text = message.message
            viewHolder.itemView.setOnClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                binding.tvEveryone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(it1!!).setValue(message)
                    }
                    dialog.dismiss()
                }

                binding.tvDelete.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()
                }

                binding.tvCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
                false
            }
        }
    }

    inner class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }

    init {
        if (messages != null) {
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }
}