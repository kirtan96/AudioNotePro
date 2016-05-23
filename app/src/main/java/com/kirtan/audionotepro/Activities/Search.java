package com.kirtan.audionotepro.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kirtan.audionotepro.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Kirtan on 5/23/16.
 */
public class Search extends AppCompatActivity {
    ListView listView;
    ArrayList<String> key;
    String search;
    String file = "";
    TextView nrf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listView = (ListView) findViewById(R.id.listView);
        nrf = (TextView) findViewById(R.id.no_result);
        nrf.setVisibility(View.INVISIBLE);

        final EditText editText = (EditText) findViewById(R.id.searchText);
        assert editText != null;
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    handled = true;

                    search = editText.getText().toString().trim();
                    if(!search.isEmpty()) {
                        getSupportActionBar().setTitle(search);
                        key = new ArrayList<>();
                        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                        Map<String, ?> keys = myPrefs.getAll();
                        for (Map.Entry<String, ?> entryKey : keys.entrySet()) {
                            if ((!entryKey.getKey().contains("myYouTubeURLS")) &&
                                    (!entryKey.getKey().contains("checkBox")) &&
                                    (!entryKey.getKey().contains("myYouTubeFiles")) &&
                                    (!entryKey.getKey().contains("content://")) &&
                                    (!entryKey.getKey().contains("file://")) &&
                                    (!entryKey.getKey().contains("(FOLDER)")) &&
                                    (!entryKey.getKey().contains("myFolders")) &&
                                    (!entryKey.getKey().contains("recordingsInt"))) {
                                if (myPrefs.getString(entryKey.getValue().toString(), "").toLowerCase().contains(search.toLowerCase()) ||
                                        ((!entryKey.getKey().toString().equals("myFiles") &&
                                                (entryKey.getKey().toString().toLowerCase().contains(search.toLowerCase())))))
                                {
                                    key.add(entryKey.getKey());
                                }
                            }
                        }
                        if(key.isEmpty())
                        {
                            nrf.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            nrf.setVisibility(View.INVISIBLE);
                        }
                        Collections.sort((List)key);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(Search.this,
                                android.R.layout.simple_list_item_1, key);
                        listView.setAdapter(arrayAdapter);
                    }
                }

                return handled;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                file = listView.getItemAtPosition(position).toString();
                Intent intent = new Intent(Search.this, SearchResult.class);
                intent.putExtra("file", file);
                intent.putExtra("search", search);
                startActivity(intent);
            }
        });

    }

}