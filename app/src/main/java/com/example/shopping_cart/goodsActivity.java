package com.example.shopping_cart;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class goodsActivity extends AppCompatActivity {
    private final Context context = this;
    private int num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goods);

        TextView textView_name_goods = (TextView) findViewById(R.id.textView_name_goods);
        TextView textView_price_goods = (TextView) findViewById(R.id.textView_price_goods);
        TextView textView_content_goods = (TextView) findViewById(R.id.textView_content_goods);
        TextView textView_quantity_goods = (TextView) findViewById(R.id.textView_quantity_goods);
        EditText editText_num__goods = (EditText) findViewById(R.id.editText_num__goods);
        ImageView imageView_index_goods = (ImageView) findViewById(R.id.imageView_index_goods);

        String id = getIntent().getStringExtra("id");
        num = Integer.parseInt(editText_num__goods.getText().toString());
        Map goods =new Gson().fromJson(tool.readData(context,id),Map.class);
        imageView_index_goods.setImageBitmap(tool.readImage(context,id));
        textView_name_goods.setText(goods.get("name").toString());
        textView_price_goods.setText("價格 : " + goods.get("price"));
        textView_quantity_goods.setText("剩餘數量 : " + goods.get("quantity"));
        textView_content_goods.setText("商品敘述 : " + goods.get("content"));

        findViewById(R.id.btn_plus__goods).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText_num__goods.setText(String.valueOf(++num));
            }});
        findViewById(R.id.btn_minus_goods).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (num != 0)
                    editText_num__goods.setText(String.valueOf(--num));
            }});
        findViewById(R.id.btn_ok_goods).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(1,getIntent().putExtra("id",id).putExtra("num",num));
                finish();
            }});

    }

}
