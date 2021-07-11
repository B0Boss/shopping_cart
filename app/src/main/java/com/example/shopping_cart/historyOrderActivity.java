package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private ArrayList<Map<String, Object>> dataList;
    ArrayList<String> key = new ArrayList<>();
    private final DatabaseReference accountData = FirebaseDatabase.getInstance().getReference("account");
    private final DatabaseReference goods = FirebaseDatabase.getInstance().getReference("goods");
    private final DatabaseReference order = FirebaseDatabase.getInstance().getReference("order");
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);
        setTitle("歷史訂單");
        ListView listView_historyOrder = findViewById(R.id.listView_historyOrder);
        dataList = new ArrayList<>();
        order.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                accountData.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        boolean isAdmin = false;
                        for (DataSnapshot ds:snapshot.getChildren()) {
                            isAdmin = snapshot1.child(firebaseAuth.getCurrentUser().getUid()).child("admin").getValue() != null;
                            if (!isAdmin)
                                if (!ds.child("0").child("userID").getValue().equals(firebaseAuth.getCurrentUser().getUid()))
                                    continue;
                            key.add(ds.getKey());
                            Map<String,Object>data = new HashMap<>();
                            StringBuilder name = new StringBuilder();
                            int totalprice =0;
                            StringBuilder id = new StringBuilder(),num = new StringBuilder(),quantity = new StringBuilder();
                            for (DataSnapshot goodsData:ds.getChildren()){
                                id.append(goodsData.child("id").getValue()+":");
                                num.append(goodsData.child("num").getValue()+":");
                                quantity.append(goodsData.child("quantity").getValue()+":");
                                name.append("品名: ").append(goodsData.child("name").getValue())
                                        .append("\n數量: ").append(goodsData.child("num").getValue())
                                        .append("\n價格: ").append(goodsData.child("totalPrice").getValue()).append("\n\n");
                                totalprice += Integer.parseInt(goodsData.child("totalPrice").getValue().toString());
                            }
                            data.put("id",id.delete(id.length()-1,id.length()));
                            data.put("num",num.delete(num.length()-1,num.length()));
                            data.put("quantity",quantity.delete(quantity.length()-1,quantity.length()));
                            data.put("name",name);
                            data.put("totalPrice",totalprice);
                            if (ds.child("0").child("orderStatus").getValue().toString().equals("1"))
                                data.put("orderStatus","等待賣家接受中");
                            if (ds.child("0").child("orderStatus").getValue().toString().equals("2"))
                                data.put("orderStatus","等待賣家出貨中");
                            if (ds.child("0").child("orderStatus").getValue().toString().equals("3"))
                                data.put("orderStatus","已結案");
                            if (ds.child("0").child("orderStatus").getValue().toString().equals("0"))
                                data.put("orderStatus","已取消");
                            dataList.add(data);
                        }
                        boolean finalIsAdmin = isAdmin;
                        listView_historyOrder.setAdapter(new SimpleAdapter(context,dataList,R.layout.listview_history_order,
                            new String[]{"name","totalPrice","orderStatus"},
                                new int[]{R.id.textView_name_history,R.id.textView_totalPrice_history,
                                        R.id.textView_orderStatus_history}){
                            @Override
                            public boolean isEnabled(int position) {
                                if (!finalIsAdmin)
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
                    String status =snapshot.child(key.get(position)).child("0").child("orderStatus").getValue().toString();
                    dialog(position,status);
                });
            }@Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Tag", "get history failed");
            }
        });
    }
    private void dialog(int position,String status){
        StringBuilder nextStatus =new StringBuilder();
        View layout = getLayoutInflater().inflate(R.layout.order_dialog_layout, findViewById(R.id.dialog_layout));
        TextView textView_price_dialog = layout.findViewById(R.id.textView_price_dialog);
        TextView textView_status_dialog = layout.findViewById(R.id.textView_status_dialog);
        TextView textView_data_dialog = layout.findViewById(R.id.textView_data_dialog);
        Button btn_back_dialog = layout.findViewById(R.id.btn_back_dialog);
        Button btn_buyer_dialog = layout.findViewById(R.id.btn_buyer_dialog);
        Button btn_cancel_dialog = layout.findViewById(R.id.btn_cancel_dialog);
        Button btn_next_dialog = layout.findViewById(R.id.btn_next_dialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.create();
        builder.setTitle("訂單狀態");
        builder.setView(layout);
        textView_price_dialog.setText("總價 : "+dataList.get(position).get("totalPrice"));
        textView_status_dialog.setText("目前狀態 : "+dataList.get(position).get("orderStatus"));
        textView_data_dialog.setText(dataList.get(position).get("name").toString());

        if (status.equals("1"))
            nextStatus.append("接受訂單");
        if (status.equals("2"))
            nextStatus.append("出貨完成");
        btn_next_dialog.setText(nextStatus);

        btn_cancel_dialog.setVisibility(View.VISIBLE);
        if (status.equals("3") || status.equals("0"))
            btn_cancel_dialog.setVisibility(View.INVISIBLE);

        btn_back_dialog.setOnClickListener(v -> dialog.dismiss());

        btn_buyer_dialog.setOnClickListener(v -> {
            AlertDialog.Builder twoDialog = new AlertDialog.Builder(context);
            twoDialog.setTitle("買家資訊");
            StringBuilder buyer = new StringBuilder();
            accountData.get().addOnCompleteListener(task -> {
                DataSnapshot user = task.getResult().child(firebaseAuth.getCurrentUser().getUid());
                buyer.append("名稱 : ").append(firebaseAuth.getCurrentUser().getDisplayName())
                        .append("\nEmail : ").append(user.child("Email").getValue())
                            .append("\n電話 : ").append(user.child("phone").getValue())
                                .append("\n地址 : ").append(user.child("address").getValue());
                twoDialog.setMessage(buyer);
                twoDialog.setPositiveButton("返回",((dialog1, which) -> {}));
                twoDialog.show();
            });
        });

        btn_cancel_dialog.setOnClickListener(v -> {
            order.child(key.get(position)).child("0").child("orderStatus").setValue("0");
            dialog.dismiss();
        });

        btn_next_dialog.setOnClickListener(v -> {
            if (status.equals("1"))
                order.child(key.get(position)).child("0").child("orderStatus").setValue("2");
            if (status.equals("2")) {
                order.child(key.get(position)).child("0").child("orderStatus").setValue("3");
                Log.d("Tag", ""+dataList.get(position));
                String[]id = dataList.get(position).get("id").toString().split(":");
                String[]num = dataList.get(position).get("num").toString().split(":");
                String[]quantity = dataList.get(position).get("quantity").toString().split(":");
                for (int i = 0;i < id.length;i++){
                    goods.child(id[i]).child("quantity").
                            setValue(Integer.parseInt(quantity[i]) - Integer.parseInt(num[i]));
                }
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}