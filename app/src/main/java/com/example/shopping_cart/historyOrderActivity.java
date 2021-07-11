package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class historyOrderActivity extends AppCompatActivity {
    private final Context context =this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);
        setTitle("歷史訂單");

        ListView listView_historyOrder = findViewById(R.id.listView_historyOrder);
        ArrayList<Map<String, Object>> dataList = new ArrayList<>();
        DatabaseReference accountData = FirebaseDatabase.getInstance().getReference("account");
        DatabaseReference order = FirebaseDatabase.getInstance().getReference("order");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        order.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> key = new ArrayList<>();
                dataList.clear();
                accountData.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        for (DataSnapshot ds:snapshot.getChildren()) {
                            if (snapshot1.child(firebaseAuth.getCurrentUser().getUid()).child("admin").getValue() == null)
                                if (!ds.child("0").child("userID").getValue().equals(firebaseAuth.getCurrentUser().getUid()))
                                    return;
                            key.add(ds.getKey());
                            Map<String,Object>data = new HashMap<>();
                            StringBuilder name = new StringBuilder();
                            int totalprice =0;
                            for (DataSnapshot goodsData:ds.getChildren()){
                                name.append(goodsData.child("name").getValue()+"\t\t\t\t\t\t"+
                                        goodsData.child("num").getValue()+"\t\t\t\t\t\t"+
                                        goodsData.child("totalPrice").getValue()+"\n");
                                totalprice += Integer.parseInt(goodsData.child("totalPrice").getValue().toString());
                            }
                            data.put("name",name);
                            data.put("totalPrice",totalprice);
                            if (ds.child("0").child("orderStatus").getValue().toString().equals("1"))
                                data.put("orderStatus","等待賣家接受中");
                            if (ds.child("0").child("orderStatus").getValue().toString().equals("2"))
                                data.put("orderStatus","訂單已接受，等待賣家出貨中");
                            if (ds.child("0").child("orderStatus").getValue().toString().equals("3"))
                                data.put("orderStatus","已結束");
                            dataList.add(data);
                        }

                        listView_historyOrder.setAdapter(new SimpleAdapter(context,dataList,R.layout.listview_history_order,
                            new String[]{"name","totalPrice","orderStatus"},
                                new int[]{R.id.textView_name_history,R.id.textView_totalPrice_history,
                                        R.id.textView_orderStatus_history}){
                            @Override
                            public boolean isEnabled(int position) {
                                if (snapshot1.child(firebaseAuth.getCurrentUser().getUid()).child("admin").getValue() == null)
                                    return false;
                                else
                                    return super.isEnabled(position);
                            }
                        });
                    }@Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                listView_historyOrder.setOnItemClickListener((parent, view, position, id) -> {
                    if (snapshot.child(key.get(position)).child("0").child("orderStatus").getValue().toString().equals("1"))
                        order.child(key.get(position)).child("0").child("orderStatus").setValue("2");
                    if (snapshot.child(key.get(position)).child("0").child("orderStatus").getValue().toString().equals("2"))
                        order.child(key.get(position)).child("0").child("orderStatus").setValue("3");
                });
            }@Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Tag", "get history failed");
            }
        });
    }
}