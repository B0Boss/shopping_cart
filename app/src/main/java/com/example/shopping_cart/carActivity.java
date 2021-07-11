package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.shopping_cart.MainActivity.selectedGoods;

public class carActivity extends AppCompatActivity {

    Context context =this;
    ArrayList<Map<String,String>> item= new ArrayList<>();
    private RecyclerView recyclerView;
    private MyListAdapter recyclerAdapter;
    private String num;
    private TextView textView_total_cart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);
        setTitle("您的購物車");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference order = FirebaseDatabase.getInstance().getReference("order");
        DatabaseReference account = FirebaseDatabase.getInstance().getReference("account");
        textView_total_cart = findViewById(R.id.textView_total_cart);
        textView_total_cart.setText("");
        recyclerAdapter =new MyListAdapter();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(recyclerAdapter);
        for (String str:selectedGoods) {
            String[] pairs = str.split(":");
            String id =pairs[0];
            num =pairs[1];
            Map map =tool.gson.fromJson(tool.readData(context,id),Map.class);
            map.put("id",id);map.put("num",num);map.remove("content");
            map.put("price",String.valueOf((int)(double)(map.get("price"))));
            item.add(map);
        }
        if (item.size() == 0)
            findViewById(R.id.btn_checkOut_car).setVisibility(View.INVISIBLE);
        else
            findViewById(R.id.btn_checkOut_car).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_checkOut_car).setOnClickListener(v ->
                account.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot){
            if (firebaseAuth.getCurrentUser() == null) {
                startActivity(new Intent(context, Activity_login.class).putExtra("car", 1));
                Toast.makeText(context, "請先登入", Toast.LENGTH_LONG);
                return;
            }
            if (!firebaseAuth.getCurrentUser().isEmailVerified() ||
                snapshot.child(firebaseAuth.getCurrentUser().getUid()).child("address").getValue() == null ||
                    snapshot.child(firebaseAuth.getCurrentUser().getUid()).child("phone").getValue() == null) {
                        Toast.makeText(context, "請先驗證 E m a i l 和完成基本資料", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(context, Activity_profile.class));
            } else {
                for (Map data : item) {
                    data.remove("price");
                }
                item.get(0).put("userID", firebaseAuth.getCurrentUser().getUid());
                item.get(0).put("orderStatus", "1");
                order.push().setValue(item);
                finish();
                selectedGoods.clear();
                startActivity(new Intent(context, historyOrderActivity.class));
                Toast.makeText(context, "下單成功", Toast.LENGTH_LONG).show();
            }
        } @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

    }

    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        class ViewHolder extends RecyclerView.ViewHolder{
            private final ImageView image_recycler;
            private final TextView textView_name_recycler,textView_num_recycler,textView_price_recycler;
            private final Button btn_plus_recycler,btn_minus_recycler,btn_cancel_recycler;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                image_recycler = itemView.findViewById(R.id.image_recyler);
                textView_name_recycler = itemView.findViewById(R.id.textView_name_recycler);
                textView_num_recycler = itemView.findViewById(R.id.textView_num_recycler);
                textView_price_recycler = itemView.findViewById(R.id.textView_price_recycler);
                btn_plus_recycler = itemView.findViewById(R.id.btn_plus_recyler);
                btn_minus_recycler = itemView.findViewById(R.id.btn_minus_recyler);
                btn_cancel_recycler = itemView.findViewById(R.id.btn_cancel_recyler);
            }
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview,parent,false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (item.size() !=0 ){
                holder.image_recycler.setImageBitmap(tool.readImage(context,item.get(position).get("id")));
                holder.textView_name_recycler.setText(item.get(position).get("name"));
                holder.textView_num_recycler.setText(item.get(position).get("num"));
                item.get(position).put("totalPrice",""+
                        Integer.parseInt(item.get(position).get("price"))*Integer.parseInt(item.get(position).get("num")));
                holder.textView_price_recycler.setText(item.get(position).get("totalPrice"));
                if (item.get(item.size()-1).get("totalPrice") != null){
                    int totalPrice = 0;
                    for (Map data : item)
                        totalPrice += Integer.parseInt(data.get("totalPrice").toString());
                    textView_total_cart.setText("總共 : " + totalPrice);
                }
                holder.btn_plus_recycler.setOnClickListener(v -> {
                    String[] pairs = selectedGoods.get(position).split(":");
                    num =String.valueOf(Integer.parseInt(pairs[1])+1);
                    selectedGoods.set(position,pairs[0]+":"+num);
                    item.get(position).put("num",num);
                    recyclerAdapter.notifyItemChanged(position);
                });
                holder.btn_minus_recycler.setOnClickListener(v -> {
                    String[] pairs = selectedGoods.get(position).split(":");
                    int tmp =Integer.parseInt(pairs[1]);
                    if (tmp>1){
                        num =String.valueOf(tmp-1);
                        selectedGoods.set(position,pairs[0]+":"+num);
                        item.get(position).put("num",num);
                        recyclerAdapter.notifyItemChanged(position);
                    }

                });
                holder.btn_cancel_recycler.setOnClickListener(v -> {
                    selectedGoods.remove(position);
                    item.remove(position);
                    if (item.size() == 0)
                        findViewById(R.id.btn_checkOut_car).setVisibility(View.INVISIBLE);
                    recyclerAdapter.notifyItemRangeRemoved(0, item.size()+1);
                    int totalPrice = 0;
                    for (Map data : item)
                        totalPrice += Integer.parseInt(data.get("totalPrice").toString());
                    textView_total_cart.setText("總共 : " + totalPrice);
                    if (item.size() == 0){}
                        textView_total_cart.setText("");
                });
            }
        }
        @Override
        public int getItemCount() {
            return item.size();
        }
    }
}