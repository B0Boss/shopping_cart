package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class MainActivity extends AppCompatActivity {

    protected Context context = this;
    public static ArrayList<String> selectedGoods =new ArrayList<>();
    ArrayList<Map<String, Object>> dataList = new ArrayList<>();

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        Spinner spinner_main =findViewById(R.id.spinner_main);
        ListView listView_main =findViewById(R.id.listView_main);

        ArrayList<String> goodsLabel = new Gson().fromJson(tool.readData(context, "goodsLabel"), ArrayList.class);
        //spinner adapter
        ArrayAdapter spinnerAdapter =new ArrayAdapter<String>(context,R.layout.simple_spinner_item,goodsLabel);
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        //listView adapter
        SimpleAdapter adapter = new SimpleAdapter(context, dataList, R.layout.listview_main,
                new String[]{"name", "price", "quantity", "content","image"},
                new int[]{R.id.textView_name_listView, R.id.textView_price_listView,R.id.textView_quantity__listView,
                        R.id.textView_content_listView, R.id.imageView_icon_listView});
        adapter.setViewBinder((view, data, textRepresentation) -> {
            if(view instanceof ImageView && data instanceof Bitmap){
                ((ImageView)view).setImageBitmap((Bitmap) data);
                return true;
            }return false;
        });

        spinner_main.setAdapter(spinnerAdapter);spinner_main.setSelection(goodsLabel.size()-1);
        spinner_main.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dataList.clear();
                for (Object label : new Gson().fromJson(tool.readData(context,goodsLabel.get(position)),ArrayList.class)) {
                    Map<String,Object> data =new Gson().fromJson(tool.readData(context, label.toString()), Map.class);
                    data.put("id",label.toString());data.put("image",tool.readImage(context,label.toString()));
                    data.put("price",(int)(double)(data.get("price")));data.put("quantity",(int)(double)(data.get("quantity")));
                    dataList.add(data);
                }adapter.notifyDataSetChanged();
            }public void onNothingSelected(AdapterView<?> parent) {}
        });

        listView_main.setAdapter(adapter);
        listView_main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivityForResult(new Intent(context,goodsActivity.class).putExtra("id",dataList.get(position).get("id").toString()),1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.cart:
//                item goods = new item();
//                goods.selectedGoods =selectedGoods;
//                Bundle bundle =new Bundle();
//                bundle.putSerializable("selectedGoods",goods);
                startActivity(new Intent(context,carActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
            if (resultCode == 1)
                selectedGoods.add(data.getStringExtra("id")+":"+data.getIntExtra("num",0));
    }

    private void initialize(){
        ProgressDialog dialog = ProgressDialog.show(this,"讀取中","請稍候",true);
        new Thread(()->{
            DatabaseReference goodsLabel = FirebaseDatabase.getInstance().getReference("goods_label");
            DatabaseReference goodsData = FirebaseDatabase.getInstance().getReference("goods");
            StorageReference imageData = FirebaseStorage.getInstance().getReference().child("goods");
            goodsLabel.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ArrayList<String> goodsLabelData = new ArrayList<String>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        goodsLabelData.add(ds.getKey());
                        tool.writeData(context, ds.getKey(), ds.getValue().toString());
                    }tool.writeData(context, "goodsLabel", goodsLabelData.toString());
                }@Override
                public void onCancelled(@NonNull DatabaseError error) {}});

            goodsData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        tool.writeData(context,ds.getKey(),ds.getValue().toString());
                        //image↓
                        File localFile = new File(context.getDir(ds.getKey(), 0), "index.jpg");
                        imageData.child(ds.getKey()).child("index.jpg").getFile(localFile).addOnFailureListener(
                                e -> Log.d("Tag", "image downloaded failure : " + localFile.toString()));
                    }
                }@Override
                public void onCancelled(@NonNull DatabaseError error) {}});
            runOnUiThread(dialog::dismiss);
        }).start();
    }

}

class tool {
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
            Log.d("Tag", "read "+filename+" error");
        }return sb.toString();
    }
    public static Bitmap readImage(Context context,String id) {
        try {
            File f = new File(context.getFilesDir().getParent(), "app_" + id + "/index.jpg");
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            Log.d("Tag", "bitmap read failure : "+e);
        }
        return null;
    }

}
//class item implements Serializable {
//    public ArrayList<String> selectedGoods;
//}