package com.example.shopping_cart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Activity_login extends AppCompatActivity {

    Context context =this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.btn_signUp_login).setOnClickListener(v ->
                startActivity(new Intent(context,Activity_signup.class)));
    }
}