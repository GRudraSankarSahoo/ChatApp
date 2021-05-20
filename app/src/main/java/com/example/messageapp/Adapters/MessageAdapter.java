package com.example.messageapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.messageapp.Models.Message;
import com.example.messageapp.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import kotlin.jvm.functions.Function1;

public class MessageAdapter extends RecyclerView.Adapter{
    Context context;
    ArrayList<Message> Messages;
    final int ITEM_SENT=1;
    final int ITEM_RECEiVE=2;
    String senderRoom;
    String receiverRoom;

    public MessageAdapter(Context context, ArrayList<Message> messages,String senderRoom,String receiverRoom) {
        this.context = context;
        Messages = messages;
        this.senderRoom=senderRoom;
        this.receiverRoom=receiverRoom;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_RECEiVE){
            View view= LayoutInflater.from(context).inflate(R.layout.item_receive,parent,false);
            return new ReceiverViewHolder(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.item_send,parent,false);
            return new SenderViewHolder(view);
        }

    }


    @Override
    public int getItemViewType(int position) {
        Message message= Messages.get(position);
        if(message.getSenderId().equals(FirebaseAuth.getInstance().getUid()))
            return ITEM_SENT;
        else
            return ITEM_RECEiVE;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Message message=Messages.get(position);
        final int reactions[]=new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();
        final ReactionPopup popup = new ReactionPopup(context, config, new Function1<Integer, Boolean>() {
            @Override
            public Boolean invoke(Integer pos) {
                if (holder.getClass() == SenderViewHolder.class){
                    SenderViewHolder viewHolder=(SenderViewHolder)holder;
                   viewHolder.feeling.setImageResource(reactions[pos]);
                   viewHolder.feeling.setVisibility(View.VISIBLE);
                }
                else{
                    ReceiverViewHolder viewHolder=(ReceiverViewHolder) holder;
                    viewHolder.feeling.setImageResource(reactions[pos]);
                    viewHolder.feeling.setVisibility(View.VISIBLE);
                }
                message.setFeeling(pos);
                FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(message.getMessageId()).setValue(message);

                FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(receiverRoom)
                        .child("messages")
                        .child(message.getMessageId()).setValue(message);

                return true;
            }
        });
            if (holder.getClass() == SenderViewHolder.class) {
                ((SenderViewHolder)holder).send_message.setText(message.getMessage());

                if(message.getMessage().equals("photo")){
                    ((SenderViewHolder)holder).send_image.setVisibility(View.VISIBLE);
                    ((SenderViewHolder)holder).send_message.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(message.getImageUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(((SenderViewHolder)holder).send_image);
                }

                if(message.getFeeling()>=0){
                    ((SenderViewHolder)holder).feeling.setImageResource(reactions[message.getFeeling()]);
                    ((SenderViewHolder)holder).feeling.setVisibility(View.VISIBLE);
                }
                else{
                    ((SenderViewHolder)holder).feeling.setVisibility(View.GONE);
                }
                ((SenderViewHolder)holder).send_message.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popup.onTouch(v,event);
                        return false;
                    }
                });
                ((SenderViewHolder)holder).send_image.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popup.onTouch(v,event);
                        return false;
                    }
                });
            } else {
                ((ReceiverViewHolder)holder).receive_message.setText(message.getMessage());

                if(message.getMessage().equals("photo")){
                    ((ReceiverViewHolder)holder).receive_image.setVisibility(View.VISIBLE);
                    ((ReceiverViewHolder)holder).receive_message.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(message.getImageUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(((ReceiverViewHolder)holder).receive_image);
                }

                if(message.getFeeling()>=0){
                    ((ReceiverViewHolder)holder).feeling.setImageResource(reactions[message.getFeeling()]);
                    ((ReceiverViewHolder)holder).feeling.setVisibility(View.VISIBLE);
                }
                else{
                    ((ReceiverViewHolder)holder).feeling.setVisibility(View.GONE);
                }
                ((ReceiverViewHolder)holder).receive_message.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popup.onTouch(v,event);
                        return false;
                    }
                });
                ((ReceiverViewHolder)holder).receive_image.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popup.onTouch(v,event);
                        return false;
                    }
                });
            }
    }

    @Override
    public int getItemCount() {
        return Messages.size();
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder{
        private TextView send_message;
        private ImageView feeling,send_image;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            send_message=itemView.findViewById(R.id.send_message);
            feeling=itemView.findViewById(R.id.feeling);
            send_image=itemView.findViewById(R.id.image);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
        private TextView receive_message;
        private ImageView feeling,receive_image;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receive_message=itemView.findViewById(R.id.send_message);
            feeling=itemView.findViewById(R.id.feeling);
            receive_image=itemView.findViewById(R.id.image);
        }
    }
}
