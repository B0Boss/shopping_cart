package com.example.shopping_cart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
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
    private DatabaseReference goodsData,goodsLabel;
    private final int selectGoods=1;
    public static ArrayList<String> selectedGoods =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<Map<String, Object>> dataList = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(context, dataList, R.layout.listview_main,
                new String[]{"name", "price", "quantity", "content","image"},
                new int[]{R.id.textView_name_listView, R.id.textView_price_listView,
                        R.id.textView_quantity__listView, R.id.textView_content_listView, R.id.imageView_icon_listView});
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if(view instanceof ImageView && data instanceof Bitmap){
                    ((ImageView)view).setImageBitmap((Bitmap) data);
                    return true;
                }return false;
            }});
        ListView listView_main =findViewById(R.id.listView_main);
        listView_main.setAdapter(adapter);
        Spinner spinner_main =findViewById(R.id.spinner_main);

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://shopping-car-dac29.appspot.com");
        StorageReference StorageReference = storage.getReference().child("goods");
        goodsData = FirebaseDatabase.getInstance().getReference("goods");
        goodsLabel = FirebaseDatabase.getInstance().getReference("goods_label");
        goodsData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data;
                dataList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    HashMap<String, Object> mapData = new HashMap<>();
                    data = ds.getKey();
                    if (data == null)   mapData.put("id","no id");
                    else                mapData.put("id",data);

                    data = ds.child("name").getValue().toString();
                    if (data == null)   mapData.put("name","no name");
                    else                mapData.put("name",data);

                    data = ds.child("price").getValue().toString();
                    if (data == null)   mapData.put("price","no price");
                    else                mapData.put("price",data);

                    data = ds.child("quantity").getValue().toString();
                    if (data == null)   mapData.put("quantity","no quantity");
                    else                mapData.put("quantity",data);

                    data = ds.child("content").getValue().toString();
                    if (data == null)   mapData.put("content","no content");
                    else                mapData.put("content",data);

                    tool.writeData(context,ds.getKey(),mapData);

                    File localFile = new File(context.getDir(ds.getKey(),0),"index.jpg");
                    StorageReference.child(ds.getKey()).child("index.jpg").getFile(localFile).addOnSuccessListener(
                            taskSnapshot -> Log.d("Tag","succeed : "+localFile.toString() )).addOnFailureListener(
                                    e -> Log.d("Tag", "image failed : "+localFile.toString()));
                    mapData.put("image",tool.readImage(context,ds.getKey()));

//                    Log.d("Tag",mapData.toString());
                    dataList.add(mapData);
                }adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });//LV初值設定
        listView_main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivityForResult(new Intent(context,goodsActivity.class).putExtra("id",dataList.get(position).get("id").toString()),selectGoods);
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
        if (requestCode == selectGoods)
            if (resultCode == 1)
                selectedGoods.add(data.getStringExtra("id")+":"+data.getIntExtra("num",0));
    }
}

class tool {
    public static void writeData(Context context, String filename, Map data){
        String dataString = data.toString().substring(1,data.toString().length()-1);
        try {
            FileOutputStream f = context.openFileOutput(filename, MODE_PRIVATE);
            f.write(dataString.getBytes());
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Map readData(Context context, String filename){
        HashMap<String,String> result = new HashMap<>();
        try {
            StringBuilder sb = new StringBuilder();
            FileInputStream fin = context.openFileInput(filename);
            byte[] data = new byte[fin.available()];
            while (fin.read(data) != -1)
                sb.append(new String(data));
            fin.close();
            String[] pairs = sb.toString().split(", ");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                result.put(keyValue[0], keyValue[1]);
            }
        } catch (IOException e) {e.printStackTrace();}
        return result;
    }
    public static Bitmap readImage(Context context,String id) {
        try {
            File f = new File(context.getFilesDir().getParent(), "app_" + id + "/index.jpg");
            Log.d("Tag", f.toString());
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            Log.d("Tag", "bitmap fail ");
        }
        return null;
    }
}
//class item implements Serializable {
//    public ArrayList<String> selectedGoods;
//}