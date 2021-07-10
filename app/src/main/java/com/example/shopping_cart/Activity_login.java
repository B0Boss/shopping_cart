package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Activity_login extends AppCompatActivity {

    Context context =this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText editText_account_login = findViewById(R.id.editText_account_login);
        EditText editText_password_login = findViewById(R.id.editText_password_login);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        findViewById(R.id.btn_login_login).setOnClickListener(v ->{
            if (editText_account_login.getText().length() != 0 || editText_password_login.getText().length() != 0)
                firebaseAuth.signInWithEmailAndPassword(editText_account_login.getText().toString(),
                        editText_password_login.getText().toString()).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(context,"登入成功",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(context,MainActivity.class));
                    }else {
                        Log.d("Tag", "登入失敗 : " + task.getException());
                        Toast.makeText(context,"認證失敗",Toast.LENGTH_LONG).show();
                    }
                });
        });
        findViewById(R.id.btn_signUp_login).setOnClickListener(v ->
                startActivity(new Intent(context,Activity_signup.class)));
    }
}