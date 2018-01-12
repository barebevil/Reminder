package com.derekpoon.reminder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    private ListView lv;
    private ArrayList<String> myList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent intent = getIntent();
        if (intent.getStringArrayListExtra("list") != null) {
            myList = intent.getStringArrayListExtra("list");
        }

        lv = (ListView)findViewById(R.id.list_view);

        ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                myList);

        lv.setAdapter(arrayadapter);

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Log.v("long clicked","pos: " + pos);

                myList.remove(pos);
                Log.d("ARRAY UPDATED", myList.toString());
                CreateFileFromList();
                Intent intent = getIntent();
                finish();
                startActivity(intent);

                return true;
            }
        });

    }

    public void CreateFileFromList() {
        try {
            File testFile = new File(this.getExternalFilesDir(null), "TestFile.txt");
            if (!testFile.exists()) {
                testFile.createNewFile();
            }
            FileWriter fw = new FileWriter(testFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(String s : myList) {
                bw.write(s + System.getProperty("line.separator"));
            }
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
