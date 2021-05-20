package com.example.messageapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.messageapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneNumberActivity extends AppCompatActivity {


    Button continueBtn;
    EditText PhoneBox;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone_number2);
        auth=FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            Intent intent=new Intent(PhoneNumberActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }


        PhoneBox=findViewById(R.id.PhoneBox);
        continueBtn=findViewById(R.id.continueBtn);
        getSupportActionBar().hide();
       continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent=new Intent(PhoneNumberActivity.this,OTPActivity.class);
                intent.putExtra("phoneNumber",PhoneBox.getText().toString());
                startActivity(intent);
           }
        });

    }



}