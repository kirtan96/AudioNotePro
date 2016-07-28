package com.kirtan.audionotepro.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.kirtan.audionotepro.Fragments.LoginFragment;
import com.kirtan.audionotepro.Fragments.SignupFragment;
import com.kirtan.audionotepro.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnClickedListener,
        SignupFragment.OnClickedListener{


    ListView note;
    String uri = "", file = "";
    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;
    ImageView back;
    TextView title;
    Button search, menu;
    FileAdapter adp;
    boolean isFolderOpen;
    int readCheck, writeCheck, recordCheck, internetCheck;
    public View row;
    ArrayList<String> folderLists, fileLists, recordLists, youTubeLists, noteList;
    private FragmentManager fragmentManager;
    private LoginFragment loginFragment;
    private SignupFragment signupFragment;
    final String MY_FILES = "myFiles",
            MY_FOLDERS = "myFolders",
            MY_YOUTUBE_FILES = "myYouTubeFiles",
            MY_YOUTUBE_URLS = "myYouTubeURLS";
    FloatingActionButton add;
    RelativeLayout relativeLayout;
    float x1,y1,x2,y2;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        writeCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        readCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        recordCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        internetCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        if (writeCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        if (recordCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }


        add = (FloatingActionButton) findViewById(R.id.fab);
        setTitle("All Notes");


        search = (Button) findViewById(R.id.search_button);
        back = (ImageView) findViewById(R.id.back);
        back.setVisibility(View.INVISIBLE);
        note = (ListView) findViewById(R.id.listView2);
        menu = (Button) findViewById(R.id.menu_button);
        relativeLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();
        title = (TextView) findViewById(R.id.title);
        title.setVisibility(View.INVISIBLE);
        isFolderOpen = false;

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            menu.setText("Sign Out");
        }
        else{
            menu.setText("Log In");
        }

        assert add != null;
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose an option:");
                if (title.getVisibility() == View.INVISIBLE) {
                    builder.setItems(new String[]{"Open Audio File", "Start New Recording", "Create New Folder", "Open a YouTube Video"},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 2) {
                                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                        alertDialog.setTitle("Folder");
                                        alertDialog.setMessage("Add a Folder:");


                                        final EditText input = new EditText(MainActivity.this);
                                        input.setSingleLine();
                                        input.setHint("Name of the folder");
                                        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.MATCH_PARENT);
                                        input.setLayoutParams(lp);
                                        alertDialog.setView(input);

                                        alertDialog.setPositiveButton("Add",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                        String name = input.getText().toString();
                                                        if (name.length() >= 1) {
                                                            name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                                        }
                                                        if (!myPrefs.getString(MY_FOLDERS, "").contains(name.trim() + " (FOLDER)") &&
                                                                !name.trim().equals("") &&
                                                                !name.trim().equals("All Notes")) {
                                                            String temp = myPrefs.getString(MY_FOLDERS, "");
                                                            editor.putString(MY_FOLDERS, temp + name.trim()
                                                                    + " (FOLDER)" + "\n");
                                                            editor.apply();
                                                            folderLists.add(name.trim());
                                                            Collections.sort((List) folderLists);
                                                            noteList = new ArrayList<>();
                                                            noteList.addAll(folderLists);
                                                            noteList.addAll(fileLists);
                                                            noteList.addAll(recordLists);
                                                            noteList.addAll(youTubeLists);
                                                            adp = new FileAdapter(noteList);
                                                            note.setAdapter(adp);
                                                        } else {
                                                            Toast.makeText(MainActivity.this,
                                                                    "Cannot create a folder with this name!",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });

                                        alertDialog.setNegativeButton("Cancel",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                        dialog.cancel();
                                                    }
                                                });

                                        alertDialog.show();
                                    } else if (which == 0) {
                                        openAudioFile();
                                    } else if (which == 1) {
                                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                        alertDialog.setTitle("Recording File");
                                        alertDialog.setMessage("File Name:");

                                        final EditText input = new EditText(MainActivity.this);
                                        input.setSingleLine();
                                        input.setHint("Name of the file");
                                        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.MATCH_PARENT);
                                        input.setLayoutParams(lp);
                                        alertDialog.setView(input);

                                        alertDialog.setPositiveButton("Add",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        String name = input.getText().toString().trim();
                                                        if (name.length() >= 1) {
                                                            name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                                        }
                                                        if (!name.trim().equals("") &&
                                                                checkEveryFolder(name+"\n")) {
                                                            Intent intent = new Intent(MainActivity.this, RecordAudio.class);
                                                            intent.putExtra("fileName", name);
                                                            intent.putExtra("folderName", "All Notes");
                                                            startActivity(intent);
                                                        } else {
                                                            Toast.makeText(MainActivity.this,
                                                                    "Invalid File Name!",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                    }
                                                });

                                        alertDialog.setNegativeButton("Cancel",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                        dialog.cancel();
                                                    }
                                                });

                                        alertDialog.show();
                                    }
                                else if(which == 3)
                                {
                                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                    alertDialog.setTitle("Add a YouTube Video");

                                    final EditText input = new EditText(MainActivity.this);
                                    final EditText input2 = new EditText(MainActivity.this);
                                    input.setSingleLine();
                                    input.setHint("Name of the file");
                                    input2.setHint("YouTube URL");
                                    input2.setSingleLine();
                                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT);
                                    input.setLayoutParams(lp);
                                    input2.setLayoutParams(lp);
                                    LinearLayout ll = new LinearLayout(MainActivity.this);
                                    ll.setOrientation(LinearLayout.VERTICAL);
                                    ll.addView(input);
                                    ll.addView(input2);
                                    alertDialog.setView(ll);

                                    alertDialog.setPositiveButton("Add",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String name = input.getText().toString().trim();
                                                    String url = input2.getText().toString().trim();
                                                    String videoId = "";
                                                    if(url.contains("youtube.com/watch?v=")){
                                                        videoId = url.substring(url.indexOf("=")+1);
                                                    }
                                                    else if(url.contains("youtu.be/")){
                                                        videoId = url.substring(url.indexOf("be/") + 3);
                                                    }
                                                    if (name.length() >= 1)
                                                    {
                                                        name = name.substring(0,1).toUpperCase()+name.substring(1);
                                                        if (checkEveryFolder(name+"\n") &&
                                                                !name.trim().equals("") &&
                                                                checkEveryVideoIds(videoId)) {
                                                            if (url.contains("youtube.com/watch?v=") ||
                                                                    url.contains("youtu.be/")){
                                                                editor.putString(MY_YOUTUBE_FILES,
                                                                        myPrefs.getString(MY_YOUTUBE_FILES, "")+
                                                                        name + MY_YOUTUBE_FILES);
                                                                Intent intent = new Intent(MainActivity.this,
                                                                        YoutubeActivity.class);
                                                                intent.putExtra("VideoID", videoId);
                                                                intent.putExtra("File", name+" (Youtube)");
                                                                editor.putString(MY_YOUTUBE_URLS,
                                                                        myPrefs.getString(MY_YOUTUBE_URLS, "")+ videoId +
                                                                                MY_YOUTUBE_URLS);
                                                                editor.putString(name+MY_YOUTUBE_FILES, videoId);
                                                                editor.apply();
                                                                startActivity(intent);
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(MainActivity.this,
                                                                        "Invalid URL!",
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                        else{
                                                            Toast.makeText(MainActivity.this,
                                                                    "File with this name/video already exists!",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(MainActivity.this,
                                                                "Invalid File Name!",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                    update();
                                                }
                                            });

                                    alertDialog.setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                    dialog.cancel();
                                                }
                                            });

                                    alertDialog.show();
                                }
                                    }
                            }
                    );
                } else {
                    builder.setItems(new String[]{"Open Audio File", "Start New Recording"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                openAudioFile();
                            } else if (which == 1) {
                                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                alertDialog.setTitle("Recording File");
                                alertDialog.setMessage("File Name:");

                                final EditText input = new EditText(MainActivity.this);
                                input.setSingleLine();
                                input.setHint("Name of the file");
                                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                alertDialog.setView(input);

                                alertDialog.setPositiveButton("Add",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                String name = input.getText().toString().trim();
                                                if (name.length() >= 1) {
                                                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                                }
                                                if (checkEveryFolder(name+"\n") &&
                                                        !name.trim().equals("")) {
                                                    Intent intent = new Intent(MainActivity.this, RecordAudio.class);
                                                    intent.putExtra("fileName", name);
                                                    intent.putExtra("folderName", title.getText().toString().trim());
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(MainActivity.this,
                                                            "Invalid File Name!",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                                            }
                                        });

                                alertDialog.setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                                dialog.cancel();
                                            }
                                        });

                                alertDialog.show();
                            }
                        }
                    });
                }
                builder.show();
            }
        });

        note.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                file = noteList.get(position);
                if (position >= folderLists.size()+fileLists.size()+recordLists.size()) {
                    navigateToYoutube(file);
                }
                else if(position >= folderLists.size())
                {
                    navigateTo(file.trim());
                }
                else {
                    updateFolder();
                }
            }
        });

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(note.getItemAtPosition(position).toString());
                if (position >= folderLists.size() && position < folderLists.size()+
                        fileLists.size()+recordLists.size()) {
                    alertDialog.setItems(new String[]{"Rename", "Move to...", "Delete"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                rename(position);

                            } else if (which == 1) {
                                move(position);
                            } else {
                                delete(position);
                            }
                        }
                    });
                } else if(position < folderLists.size()){
                    alertDialog.setItems(new String[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                rename(position);
                            } else {
                                delete(position);
                            }
                        }
                    });
                }
                else
                {
                    alertDialog.setItems(new String[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == 0)
                                rename(position);
                            else
                                delete(position);
                        }
                    });
                }
                alertDialog.show();
                return true;
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle("All Notes");
                update();
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSearch();
            }
        });
        update();

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment();
            }
        });

        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent touchevent) {
                switch (touchevent.getAction())
                {
                    // when user first touches the screen we get x and y coordinate
                    case MotionEvent.ACTION_DOWN:
                    {
                        x1 = touchevent.getX();
                        y1 = touchevent.getY();
                    }
                    case MotionEvent.ACTION_UP:
                    {
                        x2 = touchevent.getX();
                        y2 = touchevent.getY();

                        if (x1 < x2)
                        {
                            showFragment();
                        }
                        break;
                    }
                }
                return true;
            }
        });

    }

    @Override
    public void onCloseClicked() {
        hideFragment();
    }

    @Override
    public void onSignUpClicked() {
        fragmentManager.beginTransaction().
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                remove(loginFragment).commit();
        signupFragment = new SignupFragment();
        fragmentManager.beginTransaction().
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                add(R.id.mainLayout, signupFragment).
                commit();
    }

    @Override
    public void onSignupCloseClicked() {
        menu.setVisibility(View.VISIBLE);
        add.show();
        search.setVisibility(View.VISIBLE);
        fragmentManager.beginTransaction().
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                remove(signupFragment)
                .commitAllowingStateLoss();
    }

    @Override
    public void onSignupSuccess(){
        onSignupCloseClicked();
    }

    private void hideFragment() {
        menu.setVisibility(View.VISIBLE);
        add.show();
        search.setVisibility(View.VISIBLE);
        fragmentManager.beginTransaction().
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                remove(loginFragment)
                .commit();
    }

    private void showFragment() {
        menu.setVisibility(View.INVISIBLE);
        search.setVisibility(View.INVISIBLE);
        add.hide();
        fragmentManager = getFragmentManager();
        loginFragment = new LoginFragment();
        fragmentManager.beginTransaction().
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                add(R.id.mainLayout, loginFragment).
                commit();
    }

    private boolean checkEveryVideoIds(String videoId) {
        boolean b = true;
        for(String x: myPrefs.getString(MY_YOUTUBE_URLS,"").split(MY_YOUTUBE_URLS))
        {
            if(x.trim().equals(videoId))
            {
                b = false;
                break;
            }
        }
        return b;
    }

    private void updateFolder() {
        isFolderOpen = true;
        folderLists = new ArrayList<>();
        noteList = new ArrayList<>();
        Scanner in = new Scanner(myPrefs.getString(file + " (FOLDER)", ""));
        fileLists = new ArrayList<>();
        recordLists = new ArrayList<>();
        while (in.hasNextLine()) {
            String temp = in.nextLine().trim();
            if (myPrefs.getString(temp, "").contains("file:/")) {
                recordLists.add(temp);
            } else {
                fileLists.add(temp);
            }
        }
        recordLists.remove("");
        fileLists.remove("");
        Collections.sort((List) recordLists);
        Collections.sort((List) fileLists);
        noteList.addAll(fileLists);
        noteList.addAll(recordLists);
        noteList.remove("");
        title.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
        title.setText(file);
        setTitle(file);
        adp = new FileAdapter(noteList);
        note.setAdapter(adp);
    }

    /**
     * Go to YoutubeActivity
     * @param file - The name of the file
     */
    private void navigateToYoutube(String file) {
        Intent intent = new Intent(MainActivity.this, YoutubeActivity.class);
        intent.putExtra("VideoID", myPrefs.getString(file+MY_YOUTUBE_FILES,""));
        intent.putExtra("File", file + " (Youtube)");
        startActivity(intent);
    }

    /**
     * Updates the listView
     */
    private void update() {
        isFolderOpen = false;
        back.setVisibility(View.INVISIBLE);
        title.setVisibility(View.INVISIBLE);
        noteList = new ArrayList<>();
        fileLists = new ArrayList<>();
        folderLists = new ArrayList<>();
        recordLists = new ArrayList<>();
        youTubeLists = new ArrayList<>();
        Scanner in = new Scanner(myPrefs.getString(MY_FILES, ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine().trim();
            if(myPrefs.getString(temp, "").contains("file:/"))
            {
                recordLists.add(temp);
            }
            else {
                fileLists.add(temp);
            }
        }
        in = new Scanner(myPrefs.getString(MY_FOLDERS, ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            temp = temp.replace(" (FOLDER)", "");
            folderLists.add(temp.trim());
        }
        in = new Scanner(myPrefs.getString(MY_YOUTUBE_FILES,""));
        if(in.hasNextLine()) {
            String youfilelist = in.nextLine();
            for (String s : youfilelist.split(MY_YOUTUBE_FILES)) {
                if (!s.equals("")) {
                    youTubeLists.add(s);
                }
            }
        }
        fileLists.remove("");
        folderLists.remove("");
        recordLists.remove("");
        Collections.sort((List) fileLists);
        Collections.sort((List) folderLists);
        Collections.sort((List) recordLists);
        Collections.sort((List) youTubeLists);
        noteList.addAll(folderLists);
        noteList.addAll(fileLists);
        noteList.addAll(recordLists);
        noteList.addAll(youTubeLists);
        adp = new FileAdapter(noteList);
        note.setAdapter(adp);
    }

    /**
     * Opens audio chooser
     */
    private void openAudioFile() {
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, 1);
    }


    private boolean checkEveryFolder(String name) {
        boolean b = true;
        for(String x: myPrefs.getString(MY_FOLDERS,"").split("\n"))
        {
            if(!x.trim().equals("")){
                String z = myPrefs.getString(x.trim(),"");
                if(myPrefs.getString(x.trim(),"").contains(name)){
                    b = false;
                    break;
                }
            }
        }
        for(String x: myPrefs.getString(MY_FILES,"").split("\n"))
        {
            if(!x.trim().equals("")) {
                if (x.trim().equals(name.replace("\n", ""))) {
                    b = false;
                    break;
                }
            }
        }
        for(String x: myPrefs.getString(MY_YOUTUBE_FILES,"").split(MY_YOUTUBE_FILES))
        {
            if(!x.trim().equals("")) {
                if (x.trim().equals(name.replace("\n", ""))) {
                    b = false;
                    break;
                }
            }
        }
        return b;
    }

    /**
     * Moves the selected file to selected folder
     * @param position - moves the file to a different folder
     */
    private void move(final int position) {
        final String currentName = noteList.get(position);
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Move to...");
        builder.setCancelable(false);
        final ListView listView = new ListView(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        listView.setLayoutParams(lp);
        builder.setView(listView);
        final ArrayList<String> list =  new ArrayList<>();
        Scanner in = new Scanner(myPrefs.getString(MY_FOLDERS, ""));
        while(in.hasNextLine())
        {
            String temp = in.nextLine();
            temp = temp.replace(" (FOLDER)", "");
            if(!myPrefs.getString(temp.trim() + " (FOLDER)", "").contains(currentName))
            {
                list.add(temp.trim());
            }
        }
        if(title.getVisibility() == View.VISIBLE)
        {
            if(!myPrefs.getString(MY_FILES, "").contains(currentName)) {
                list.add("All Notes");
            }
            list.remove(title.getText().toString().trim());
        }
        list.remove("");
        Collections.sort((List) list);
        listView.setAdapter(new FileAdapter(list));
        final String[] temp = {""};
        final String[] t = {""};
        final String[] t2 = {""};
        final Integer[] i = {0};

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int p, long id) {
                if (title.getVisibility() == View.INVISIBLE) {
                    temp[0] = myPrefs.getString(MY_FILES, "");
                    t[0] = noteList.get(position) + "\n";
                    temp[0] = temp[0].replace(t[0].toString(), "");
                    t2[0] = list.get(p) + " (FOLDER)";
                    i[0] = 1;
                } else if (title.getVisibility() == View.VISIBLE) {
                    temp[0] = myPrefs.getString(title.getText().toString() + " (FOLDER)", "");
                    t[0] = noteList.get(position) + "\n";
                    temp[0] = temp[0].replace(t[0].toString(), "");
                    if (!list.get(p).equals("All Notes")) {
                        t2[0] = list.get(p) + " (FOLDER)";
                    } else {
                        t2[0] = list.get(p);
                    }
                    i[0] = 1;
                }
                if (row != null) {
                    row.setBackgroundResource(R.color.back);
                }
                row = view;
                view.setBackgroundResource(R.color.colorPrimaryDark);
            }
        });

        builder.setPositiveButton("Move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (title.getVisibility() == View.INVISIBLE &&
                        i[0] == 1) {
                    editor.putString(MY_FILES, temp[0]);
                    editor.putString(t2[0].toString(), myPrefs.getString(t2[0].toString(), "") + t[0]);
                    editor.apply();
                    update();
                } else if (title.getVisibility() == View.VISIBLE &&
                        i[0] == 1) {
                    editor.putString(title.getText().toString() + " (FOLDER)", temp[0]);
                    if (!t2[0].equals("All Notes")) {
                        editor.putString(t2[0].toString(), myPrefs.getString(t2[0].toString(), "") + t[0]);
                    } else {
                        editor.putString(MY_FILES, myPrefs.getString(MY_FILES, "") + t[0]);
                    }
                    editor.apply();
                    update();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    /**
     * Renames the selected file or folder
     * @param position - renames the file/folder at the given position
     */
    private void rename(final int position) {
        final String currentName = noteList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Rename");
        builder.setCancelable(false);
        final EditText input = new EditText(MainActivity.this);
        input.setSingleLine();
        input.setHint("Name of the file");
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        input.setText(currentName);

        builder.setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        String changedName = input.getText().toString().trim();
                        if (changedName.length() >= 1) {
                            changedName = changedName.substring(0, 1).toUpperCase()
                                    + changedName.substring(1);
                        }
                        if (position >= folderLists.size() && position < folderLists.size()+
                                fileLists.size()+
                                recordLists.size() &&
                                title.getVisibility() == View.INVISIBLE) {
                            String temp = myPrefs.getString(MY_FILES, "");
                            String t = "\n" + currentName + "\n";
                            if (!temp.contains(t)) {
                                t = currentName + "\n";
                                temp = temp.replace(t, changedName + "\n");
                            } else {
                                temp = temp.replace(t, "\n" + changedName + "\n");
                            }
                            editor.putString(changedName, myPrefs.getString(currentName, ""));

                            editor.putString(MY_FILES, temp);
                            editor.remove(currentName);
                            editor.apply();
                            update();
                        } else if (position < folderLists.size() &&
                                title.getVisibility() == View.INVISIBLE) {
                            String temp = myPrefs.getString(MY_FOLDERS, "");
                            String t = "\n" + currentName + " (FOLDER)" + "\n";
                            if (!temp.contains(t)) {
                                t = currentName + " (FOLDER)" + "\n";
                                temp = temp.replace(t, changedName + " (FOLDER)" + "\n");
                            } else {
                                temp = temp.replace(t, "\n" + changedName + " (FOLDER)" + "\n");
                            }
                            editor.putString(changedName + " (FOLDER)", myPrefs.getString(currentName + " (FOLDER)", ""));

                            editor.putString(MY_FOLDERS, temp);
                            editor.remove(currentName + " (FOLDER)");
                            editor.apply();
                            update();
                        } else if (!myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "").contains(
                                changedName.trim() + "\n") &&
                                title.getVisibility() == View.VISIBLE) {
                            String temp = myPrefs.getString(title.getText().toString() + " (FOLDER)",
                                    "");
                            String t = "\n" + currentName + "\n";
                            if (!temp.contains(t)) {
                                t = currentName + "\n";
                                temp = temp.replace(t, changedName + "\n");
                            } else {
                                temp = temp.replace(t, "\n" + changedName + "\n");
                            }
                            editor.putString(changedName, myPrefs.getString(currentName, ""));
                            editor.putString(title.getText().toString() + " (FOLDER)", temp);
                            editor.remove(currentName);
                            editor.apply();
                            update();
                        }
                        else if(position >= folderLists.size()+fileLists.size()+recordLists.size()){
                            String temp = myPrefs.getString(MY_YOUTUBE_FILES,"");
                            String vID = myPrefs.getString(currentName+MY_YOUTUBE_FILES,"");
                            for(String s: temp.split(MY_YOUTUBE_FILES))
                            {
                                if(s.equals(currentName))
                                {
                                    if(!temp.contains(changedName))
                                    {
                                        temp = temp.replace(currentName+MY_YOUTUBE_FILES, changedName+MY_YOUTUBE_FILES);
                                        editor.putString(MY_YOUTUBE_FILES, temp);
                                        editor.putString(changedName+MY_YOUTUBE_FILES, vID);
                                        editor.remove(currentName+MY_YOUTUBE_FILES);
                                        editor.apply();
                                        update();

                                    }
                                    else
                                    {
                                        Toast.makeText(MainActivity.this,
                                                "A file with this name already exists!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this,
                                    "A file/folder with this name already exists!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        dialog.cancel();
                    }
                });

        builder.show();
    }


    /**
     * Deletes the file or the folder
     * @param position deleted the file/folder at the given position
     */
    private void delete(int position) {
        String temp;
        if (position >= folderLists.size() &&
                position < folderLists.size()+fileLists.size()+recordLists.size() &&
                title.getVisibility() == View.INVISIBLE) {
            temp = myPrefs.getString(MY_FILES, "");
            String t = note.getItemAtPosition(position).toString() + "\n";
            editor.remove(myPrefs.getString(
                    note.getItemAtPosition(position).toString()
                    , ""));  //deletes notes in the audio file
            temp = temp.replace(t, "");     //deletes the audio file from the app
            editor.putString(MY_FILES, temp);
            editor.remove(note.getItemAtPosition(position).toString());
            editor.apply();
            update();
        } else if (title.getVisibility() == View.VISIBLE) {
            temp = myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "");
            String t = noteList.get(position) + "\n";
            editor.remove(myPrefs.getString(
                    noteList.get(position)
                    , ""));  //deletes notes in the audio file
            temp = temp.replace(t, "");     //deletes the audio file from the app
            editor.putString(title.getText().toString().trim() + " (FOLDER)", temp);
            editor.remove(noteList.get(position));
            editor.apply();
            update();
        } else if(position < folderLists.size()){
            String t = noteList.get(position) + " (FOLDER)" + "\n";
            Scanner in = new Scanner(myPrefs.getString(
                    noteList.get(position) + " (FOLDER)", ""));
            while (in.hasNextLine()) {
                temp = in.nextLine();
                Log.d("Deleted", myPrefs.getString(temp
                        , ""));
                editor.remove(myPrefs.getString(temp
                        , ""));  //deletes notes in the audio file
                Log.d("Deleted", temp);
                editor.remove(temp);  //delete the audio file
                editor.apply();
            }
            temp = myPrefs.getString(MY_FOLDERS, "");
            temp = temp.replace(t, "");     //deletes the folder from the app
            editor.putString(MY_FOLDERS, temp);
            editor.remove(note.getItemAtPosition(position).toString() + " (FOLDER)");
            editor.apply();
            update();
        }
        else if(position >= folderLists.size()+fileLists.size()+recordLists.size())
        {
            String name = noteList.get(position);
            String vID = myPrefs.getString(name+MY_YOUTUBE_FILES,"");
            temp = myPrefs.getString(MY_YOUTUBE_FILES,"");
            temp = temp.replace(name+MY_YOUTUBE_FILES, "");
            editor.putString(MY_YOUTUBE_FILES, temp);

            String t = myPrefs.getString(MY_YOUTUBE_URLS,"");
            t = t.replace(myPrefs.getString(name+MY_YOUTUBE_FILES,"")+MY_YOUTUBE_URLS,"");
            editor.putString(MY_YOUTUBE_URLS, t);
            editor.remove(myPrefs.getString(name+MY_YOUTUBE_FILES,""));
            editor.remove(name+MY_YOUTUBE_FILES);
            editor.remove(vID);
            editor.apply();
            update();
        }
        update();
        Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_LONG).show();
    }





    /**
     * After the selection of the audio file
     * @param requestCode - the requestcode
     * @param resultCode - the resultcode
     * @param data - the received data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {

                //the selected audio.

                Uri myUri = data.getData();
                uri = myUri.toString();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Create");
                alertDialog.setMessage("Name the file:");

                final EditText input = new EditText(MainActivity.this);
                input.setSingleLine();
                input.setHint("Name of the file");
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);

                final String myFiles = myPrefs.getString(MY_FILES, "");

                alertDialog.setPositiveButton("Create",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                String name = input.getText().toString();
                                if (name.length() >= 1) {
                                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                                }
                                if (title.getVisibility() == View.INVISIBLE &&
                                        !input.getText().toString().trim().equals("")) {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    if (!myFiles.contains(name)) {
                                        file = input.getText().toString();
                                        editor.putString(MY_FILES, myFiles + name + "\n");
                                        editor.putString(name, uri);
                                        editor.apply();
                                        update();
                                        navigateTo(name);
                                    } else {
                                        Toast.makeText(MainActivity.this, "This file already exists. Name it differently!", Toast.LENGTH_LONG).show();
                                    }
                                } else if (title.getVisibility() == View.VISIBLE &&
                                        !name.trim().equals("")) {
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                    String f = myPrefs.getString(title.getText().toString().trim() + " (FOLDER)", "");
                                    if (!myPrefs.getString(f, "").contains(name)) {
                                        file = input.getText().toString();
                                        editor.putString(title.getText().toString().trim() + " (FOLDER)",
                                                f + name + "\n");
                                        editor.putString(name, uri);
                                        editor.apply();
                                        update();
                                        navigateTo(name);
                                    } else {
                                        Toast.makeText(MainActivity.this, "This file already exists. Name it differently!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Sorry. Cannot create a file with this name!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }
                        });
                alertDialog.show();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Go to the Player
     * @param name - The name of the file
     */
    private void navigateTo(String name) {
        isFolderOpen = false;
        Intent intent = new Intent(MainActivity.this, Player.class);
        intent.putExtra("file", name);
        startActivity(intent);
    }

    /**
     * Navigates to search
     */
    private void navigateToSearch() {
        Intent intent = new Intent(this, Search.class);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if(writeCheck != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
                /*else if (readCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            2);
                }*/
                else if (recordCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            3);
                }
            }
            case 2: {
                if(readCheck != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            2);
                }
                else if (recordCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            3);
                }
            }
            case 3: {
                if(recordCheck != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            3);
                }
                else if (internetCheck != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.INTERNET},
                            4);
                }
            }
            case 4: {
                if (internetCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.INTERNET},
                            4);
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /**
     * Private Class for listView
     */
    private class FileAdapter extends BaseAdapter
    {

        ArrayList<String> s;
        protected FileAdapter(ArrayList<String> s1)
        {
            s = s1;
        }
        /**
         * gets the size of conversatrion list
         * @return size of conversation list
         */
        @Override
        public int getCount()
        {
            return s.size();
        }

        /**
         * gets the selected conversation
         * @param arg0 the position on the list
         * @return the conversation at a selected position
         */
        @Override
        public String getItem(int arg0)
        {
            return s.get(arg0);
        }

        /**
         * gets the id for a selected positon
         * @param arg0 the position on the list
         * @return the id for the position
         */
        @Override
        public long getItemId(int arg0)
        {
            return arg0;
        }

        /**
         * gets the layout for a conversation
         * @param pos the psoition of the conversation
         * @param v the view for how the conversation is laid out
         * @param arg2 the view group
         * @return the overall layout of a conversation
         */
        @Override
        public View getView(int pos, View v, ViewGroup arg2)
        {
            if (pos < folderLists.size())
                v = getLayoutInflater().inflate(R.layout.folder_list, null);
            else if(pos < folderLists.size() + fileLists.size())
                v = getLayoutInflater().inflate(R.layout.file_list, null);
            else if(pos < folderLists.size() + fileLists.size() + recordLists.size())
                v = getLayoutInflater().inflate(R.layout.recordings_list, null);
            else
                v = getLayoutInflater().inflate(R.layout.youtube_list, null);

            TextView lbl = (TextView) v.findViewById(R.id.note);
            lbl.setText(s.get(pos));

            return v;
        }

    }

    @Override
    public void onBackPressed() {
        if(menu.getVisibility() == View.INVISIBLE)
        {
            hideFragment();
            menu.setVisibility(View.VISIBLE);
            add.show();
            search.setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction().
                    setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                    remove(signupFragment)
                    .commit();
        }
        else {
            if (title.getVisibility() == View.VISIBLE) {
                update();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isFolderOpen)
            update();
        else
            updateFolder();
    }
}