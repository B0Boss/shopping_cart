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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Activity_signup extends AppCompatActivity {

    private final Context context =this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("使用者申請");
        setContentView(R.layout.activity_signup);

        EditText editText_account_signUp = findViewById(R.id.editText_account_signUp);
        EditText editText_password_signUp = findViewById(R.id.editText_password_signUp);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btn_signUp_signUp).setOnClickListener(v ->{
            if (editText_account_signUp.getText().length() != 0 || editText_password_signUp.getText().length() != 0)
                firebaseAuth.createUserWithEmailAndPassword(editText_account_signUp.getText().toString(),
                        editText_password_signUp.getText().toString()).addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task1 -> {
                            if (!task1.isSuccessful())
                                Log.d("Tag", "failed to send an EmailVerification");
                        });
                        Toast.makeText(context,"註冊成功",Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(context,MainActivity.class));
                    }else {
                        Log.d("Tag", "註冊失敗 : " + task.getException());
                        Toast.makeText(context,"註冊失敗 : "+task.getException(),Toast.LENGTH_LONG).show();
                    }
                });
        });

    }
}