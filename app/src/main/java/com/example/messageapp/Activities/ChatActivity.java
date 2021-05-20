package com.example.messageapp.Activities;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.messageapp.Adapters.MessageAdapter;
import com.example.messageapp.Models.Message;
import com.example.messageapp.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class ChatActivity extends AppCompatActivity {
    String senderRoom,receiverRoom;
    EditText chatBox;
    FloatingActionButton chatSendBtn;
    FirebaseDatabase database;
    RecyclerView chatView;
    ImageView attachment;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderUid;
    String receiverUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        dialog=new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);
        chatBox=findViewById(R.id.chatBox);
        chatSendBtn=findViewById(R.id.chatSendBtn);
        chatView=findViewById(R.id.chatView);
        attachment=findViewById(R.id.attachment);
        final ArrayList<Message> messages=new ArrayList<>();
        String name=getIntent().getStringExtra("name");
        receiverUid=getIntent().getStringExtra("uid");
        senderUid= FirebaseAuth.getInstance().getUid();
        senderRoom=senderUid+receiverUid;
        receiverRoom=receiverUid+senderUid;
        final MessageAdapter adapter=new MessageAdapter(this,messages,senderRoom,receiverRoom);
        chatView.setLayoutManager(new LinearLayoutManager(this));
        chatView.setAdapter(adapter);
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        chatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageTxt = chatBox.getText().toString();

                final Date date = new Date();
                final Message message = new Message(messageTxt, senderUid, date.getTime());
                chatBox.setText("");
                final String randomKey=database.getReference().push().getKey();
                HashMap<String,Object> lastMsgObj=new HashMap<>();
                lastMsgObj.put("lastMsg",message.getMessage());
                lastMsgObj.put("lastMsgTime",date.getTime());
                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                });

                    }
                });
            }
        });
        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,100);
            }
        });
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (data != null) {
                if (data.getData() != null) {
                    Uri selectedImage=data.getData();
                    Calendar calendar=Calendar.getInstance();
                    final StorageReference reference=storage.getReference().child("chats").child(calendar.getTimeInMillis()+ "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();;
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath=uri.toString();

                                        String messageTxt = chatBox.getText().toString();

                                        final Date date = new Date();
                                        final Message message = new Message(messageTxt, senderUid, date.getTime());
                                        message.setImageUrl(filePath);
                                        message.setMessage("photo");
                                        chatBox.setText("");
                                        final String randomKey=database.getReference().push().getKey();
                                        HashMap<String,Object> lastMsgObj=new HashMap<>();
                                        lastMsgObj.put("lastMsg",message.getMessage());
                                        lastMsgObj.put("lastMsgTime",date.getTime());
                                        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);
                                        database.getReference().child("chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database.getReference().child("chats")
                                                        .child(receiverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });

                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }
}