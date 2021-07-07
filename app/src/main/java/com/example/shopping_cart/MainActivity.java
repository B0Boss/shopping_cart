package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.MODE_PRIVATE;

public class MainActivity extends AppCompatActivity {

    private final Context context =this;
    public static ArrayList<String> selectedGoods =new ArrayList<>();
    private Map<String,ArrayList<String>> goodsLabel;
    private ArrayList<String> labelList;
    private ArrayList<Map<String, Object>> dataList;
    private Spinner spinner_main;
    private ListView listView_main;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner_main =findViewById(R.id.spinner_main);
        listView_main =findViewById(R.id.listView_main);

        getFirebase();
    }
    private void initializeUI(){
        goodsLabel =tool.gson.fromJson(tool.readData(context,"label"),
                new TypeToken<Map<String,ArrayList<String>>>(){}.getType());
        labelList = new ArrayList<>(goodsLabel.keySet());
        dataList = new ArrayList<>();
        //listView();
        adapter = new SimpleAdapter(context, dataList, R.layout.listview_main,
                new String[]{"name", "price", "quantity", "content","image"},
                new int[]{R.id.textView_name_listView, R.id.textView_price_listView,R.id.textView_quantity__listView,
                        R.id.textView_content_listView, R.id.imageView_icon_listView});
        adapter.setViewBinder((view, data, textRepresentation) -> {
            if(view instanceof ImageView && data instanceof Bitmap){
                ((ImageView)view).setImageBitmap((Bitmap) data);
                return true;
            }return false;
        });listView_main.setAdapter(adapter);
        listView_main.setOnItemClickListener((parent, view, position, id) -> startActivityForResult(new Intent(
                context,goodsActivity.class).putExtra("id",dataList.get(position).get("id").toString()),1));
        //spinner();
        ArrayAdapter spinnerAdapter =new ArrayAdapter<>(context,R.layout.simple_spinner_item,labelList);
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner_main.setAdapter(spinnerAdapter);    spinner_main.setSelection(goodsLabel.size()-1);
        spinner_main.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dataList.clear();
                for (String goods : goodsLabel.get(labelList.get(position))) {
                    Map<String,Object> data =tool.gson.fromJson(tool.readData(context, goods), Map.class);
                    data.put("id",goods);data.put("image",tool.readImage(context,goods));
                    data.put("price",(int)(double) data.get("price"));data.put("quantity",(int)(double)(data.get("quantity")));
                    dataList.add(data);
                }adapter.notifyDataSetChanged();
            }public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return true;
    }@Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.cart:
//                item goods = new item();
//                goods.selectedGoods =selectedGoods;
//                Bundle bundle =new Bundle();
//                bundle.putSerializable("selectedGoods",goods);
                startActivity(new Intent(context,carActivity.class));
        }return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
            if (resultCode == 1)
                selectedGoods.add(data.getStringExtra("id")+":"+data.getIntExtra("num",0));
    }

    private void getFirebase(){
        ProgressDialog dialog = ProgressDialog.show(context,"讀取中","請稍候",true);
        DatabaseReference goodsData = FirebaseDatabase.getInstance().getReference("goods");
        //first time
        if (new File(context.getFilesDir(), "label").exists()) {
            Log.d("Tag", "second connection");
            dialog.dismiss();
            initializeUI();
        }
        goodsData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Tag", "data loading");
                AtomicInteger count = new AtomicInteger(1);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    tool.writeData(context, ds.getKey(), ds.getValue().toString());
                    //image↓
                    if (!ds.getKey().equals("label")) {
                        FirebaseStorage.getInstance().getReference().child("goods").child(ds.getKey()).child("index.jpg").
                                getFile(new File(context.getDir(ds.getKey(), 0), "index.jpg")).
                                addOnFailureListener(e -> Log.d("Tag", ds.getKey() + " image download failed")).
                                addOnCompleteListener(task -> {
                                    if (count.addAndGet(1) == snapshot.getChildrenCount()) {
                                        initializeUI();
                                        dialog.dismiss();
                                    }
                                });
                    }
                }
            }@Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Tag", error.toString());
            }
        });

    }

}

class tool {
    public static Gson gson = new Gson();

    public static void writeData(Context context, String filename, String data){
        try {
            FileOutputStream f = context.openFileOutput(filename, MODE_PRIVATE);
            f.write(data.getBytes());
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String readData(Context context, String filename){
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fin = context.openFileInput(filename);
            byte[] data = new byte[fin.available()];
            while (fin.read(data) != -1)
                sb.append(new String(data));
            fin.close();
        } catch (IOException e) {
            Log.d("Tag", "read "+filename+" error : "+e);
        }return sb.toString();
    }
    public static Bitmap readImage(Context context,String id) {
        try {
            File f = new File(context.getFilesDir().getParent(), "app_" + id + "/index.jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            Log.d("Tag", id+" bitmap read failure");
        }
        return null;
    }

}