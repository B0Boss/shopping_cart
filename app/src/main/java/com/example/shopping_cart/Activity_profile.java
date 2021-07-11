package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Activity_profile extends AppCompatActivity {
    private final Context context =this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("身分認證");
        DatabaseReference accountData = FirebaseDatabase.getInstance().getReference("account");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        TextView textView_email_profile = findViewById(R.id.textView_email_profile);
        EditText editText_name_profile = findViewById(R.id.editText_name_profile);
        EditText editText_phone_profile = findViewById(R.id.editText_phone_profile);
        EditText editText_address_profile = findViewById(R.id.editText_address_profile);
        TextView textView_emailVerified_profile = findViewById(R.id.textView_emailVerified_profile);
        Button btn_getEmailVerified_profile = findViewById(R.id.btn_getEmailVerified_profile);
        String id = firebaseAuth.getCurrentUser().getUid();
        textView_email_profile.setText(firebaseAuth.getCurrentUser().getEmail());
        if (firebaseAuth.getCurrentUser().isEmailVerified()) {
            textView_emailVerified_profile.setText("已認證");
            textView_emailVerified_profile.setTextColor(Color.GREEN);
            btn_getEmailVerified_profile.setVisibility(View.INVISIBLE);
        }
        else {
            textView_emailVerified_profile.setText("未認證");
            textView_emailVerified_profile.setTextColor(Color.RED);
            btn_getEmailVerified_profile.setVisibility(View.VISIBLE);
            btn_getEmailVerified_profile.setOnClickListener(v ->
                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            Toast.makeText(context, "已寄送認證信", Toast.LENGTH_LONG).show();
                    })
            );
        }
        editText_name_profile.setText(firebaseAuth.getCurrentUser().getDisplayName());
        accountData.child(id).get().addOnCompleteListener(task -> {
            Log.d("Tag", ""+task.getResult());
                if (task.getResult().child("phone").getValue() != null)
                    editText_phone_profile.setText(task.getResult().child("phone").getValue().toString());
                if (task.getResult().child("address").getValue() != null)
                    editText_address_profile.setText(task.getResult().child("address").getValue().toString());
            });

        findViewById(R.id.btn_updateData_profile).setOnClickListener(v ->
            firebaseAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().
            setDisplayName(editText_name_profile.getText().toString()).build()).addOnCompleteListener(task ->{
                if (task.isSuccessful()) {
                    Log.d("Tag", "????????????????");
                    HashMap<String, String> data = new HashMap<>();
                    data.put("Email",firebaseAuth.getCurrentUser().getEmail());
                    data.put("Email_is_checked",firebaseAuth.getCurrentUser().isEmailVerified()+"");
                    if (editText_phone_profile.getText().length() != 0)
                        data.put("phone",editText_phone_profile.getText().toString());
                    if (editText_address_profile.getText().length() != 0)
                        data.put("address",editText_address_profile.getText().toString());
                    accountData.child(id).setValue(data);
                    finish();
                    Toast.makeText(context, "修改成功", Toast.LENGTH_LONG).show();
                }
            })
        );

    }
}