package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.shopping_cart.MainActivity.selectedGoods;

public class carActivity extends AppCompatActivity {

    Context context =this;
    ArrayList<Map<String,String>> item= new ArrayList<>();
    private RecyclerView recycleview;
    private MyListAdapter recyclerAdapter;
    private String num;
    private TextView textView_total_cart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        textView_total_cart =(TextView)findViewById(R.id.textView_total_cart);
        recyclerAdapter =new MyListAdapter();
        recycleview = (RecyclerView)findViewById(R.id.recycleview);
        recycleview.setLayoutManager(new LinearLayoutManager(context));
        recycleview.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));
        recycleview.setAdapter(recyclerAdapter);
        for (String str:selectedGoods) {
            String[] pairs = str.split(":");
            String id =pairs[0];
            num =pairs[1];
            Map map =new Gson().fromJson(tool.readData(context,id),Map.class);
            map.put("id",id);map.put("num",num);map.remove("quantity");map.remove("content");
            map.put("price",String.valueOf((int)(double)(map.get("price"))));
            item.add(map);
        }

    }

    private class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder>{
        class ViewHolder extends RecyclerView.ViewHolder{
            private final ImageView image_recycler;
            private final TextView textView_name_recycler,textView_num_recycler,textView_price_recycler;
            private final Button btn_plus_recycler,btn_minus_recycler,btn_cancel_recycler;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                image_recycler =(ImageView)itemView.findViewById(R.id.image_recyler);
                textView_name_recycler =(TextView)itemView.findViewById(R.id.textView_name_recycler);
                textView_num_recycler =(TextView)itemView.findViewById(R.id.textView_num_recycler);
                textView_price_recycler =(TextView)itemView.findViewById(R.id.textView_price_recycler);
                btn_plus_recycler =(Button)itemView.findViewById(R.id.btn_plus_recyler);
                btn_minus_recycler =(Button)itemView.findViewById(R.id.btn_minus_recyler);
                btn_cancel_recycler =(Button)itemView.findViewById(R.id.btn_cancel_recyler);
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
                Log.d("Tag",item.get(position).get("price"));
                item.get(position).put("totalPrice",""+Integer.parseInt(item.get(position).get("price"))*Integer.parseInt(item.get(position).get("num")));
                holder.textView_price_recycler.setText(item.get(position).get("totalPrice"));
                if (item.get(item.size()-1).get("totalPrice") != null){
                    int totalPrice = 0;
                    for (Map data : item)
                        totalPrice += Integer.parseInt(data.get("totalPrice").toString());
                    textView_total_cart.setText("總共 : " + totalPrice);
                }
                holder.btn_plus_recycler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] pairs = selectedGoods.get(position).split(":");
                        num =String.valueOf(Integer.parseInt(pairs[1])+1);
                        selectedGoods.set(position,pairs[0]+":"+num);
                        item.get(position).put("num",num);
                        recyclerAdapter.notifyItemChanged(position);
                    }
                });
                holder.btn_minus_recycler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] pairs = selectedGoods.get(position).split(":");
                        int tmp =Integer.parseInt(pairs[1]);
                        if (tmp>0){
                            num =String.valueOf(tmp-1);
                            selectedGoods.set(position,pairs[0]+":"+num);
                            item.get(position).put("num",num);
                            recyclerAdapter.notifyItemChanged(position);
                        }

                    }
                });
                holder.btn_cancel_recycler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedGoods.remove(position);
                        item.remove(position);
                        recyclerAdapter.notifyItemChanged(position);
                        int totalPrice = 0;
                        for (Map data : item)
                            totalPrice += Integer.parseInt(data.get("totalPrice").toString());
                        textView_total_cart.setText("總共 : " + totalPrice);
                    }
                });
            }else textView_total_cart.setText("");
        }
        @Override
        public int getItemCount() {
            return item.size();
        }
    }
}