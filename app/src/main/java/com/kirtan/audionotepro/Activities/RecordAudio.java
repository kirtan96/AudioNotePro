package com.kirtan.audionotepro.Activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kirtan.audionotepro.Fragments.NoteFragment;
import com.kirtan.audionotepro.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kirtan on 5/23/16.
 */
public class RecordAudio extends AppCompatActivity implements NoteFragment.OnClickedListener{

    private MediaRecorder mediaRecorder;
    Button start, stop;
    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;
    String outputFile, fileName, myUri, n, real, folderName, cTime = "", nts = "", splitter = "/////";
    TextView timer;
    private Handler myHandler = new Handler();
    private long startTime = 0L, timeInMilliseconds = 0L, timeSwapBuff = 0L, updatedTime = 0L;
    int mins, secs;
    ListView note;
    FloatingActionButton add;
    ArrayList<String> noteList;
    NoteListAdapter nla;
    NoteFragment noteFragment;
    boolean fragmentVisible;
    FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        timer = (TextView) findViewById(R.id.timer);
        note = (ListView) findViewById(R.id.list);
        add = (FloatingActionButton) findViewById(R.id.fab);
        timer.setText("00:00");
        stop.setEnabled(false);
        add.setEnabled(false);
        note.setEnabled(false);

        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        setTitle(fileName);
        folderName = intent.getStringExtra("folderName");
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();
        fragmentVisible = false;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Audio Note" + File.separator + "Recordings");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        if(myPrefs.getInt("recordingsInt", -1) == -1)
        {
            editor.putInt("recordingsInt", 1);
            editor.apply();
        }
        outputFile = folder + "/recordings " + myPrefs.getInt("recordingsInt", 1) + ".3gp";
        mediaRecorder.setOutputFile(outputFile);
        myUri = Uri.fromFile(new File(outputFile)).toString();
        n = "";
        real = "";
        noteList = new ArrayList<>();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                start.setEnabled(false);
                stop.setEnabled(true);
                note.setEnabled(true);
                add.setEnabled(true);
                startTime = SystemClock.uptimeMillis();
                myHandler.postDelayed(UpdateRecordingTime, 100);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder  = null;
                stop.setEnabled(false);
                start.setEnabled(true);
                timeSwapBuff += timeInMilliseconds;
                myHandler.removeCallbacks(UpdateRecordingTime);

                save();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(!fragmentVisible)
                {
                    YoutubeActivity.nt = "";
                    cTime = timer.getText().toString()+": ";
                    showFragment();
                }
                else
                {
                    hideFragment();
                }
            }
        });

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RecordAudio.this);
                builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s = noteList.get(position);
                        if(which == 0)
                        {
                            nts = s;
                            edit(s);
                        }
                        else{

                            delete(s);
                        }
                    }
                });
                builder.show();
                return true;
            }
        });

    }

    private void edit(String s)
    {
        YoutubeActivity.nt = s.substring(s.indexOf(" ") + 1);
        cTime = s.substring(0, s.indexOf(" ")+1);
        showFragment();
    }

    private void delete(String s)
    {
        noteList.remove(s);
        updateList();
    }

    private void save() {
        editor.putInt("recordingsInt", myPrefs.getInt("recordingsInt", 1) + 1);
        editor.apply();
        if(folderName.equals("All Notes"))
        {
            editor.putString("myFiles", myPrefs.getString("myFiles", "") + fileName + "\n");
            editor.apply();
        }
        else
        {
            editor.putString(folderName + " (FOLDER)", myPrefs.getString(folderName + " (FOLDER)", "") + fileName + "\n");
            editor.apply();
        }
        editor.putString(fileName, myUri);
        editor.apply();
        Toast.makeText(this, "Recording Saved!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(RecordAudio.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("WARNING");
        builder.setMessage("You will lose this recording. Do you want to continue?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                File f = new File(outputFile);
                f.delete();
                editor.remove(myUri);
                editor.apply();
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private Runnable UpdateRecordingTime = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedTime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            timer.setText("" + String.format("%02d", mins) + ":"
                    + String.format("%02d", secs));
            myHandler.postDelayed(this,100);
        }
    };

    @Override
    public void onCloseClicked() {
        hideFragment();
    }

    @Override
    public void onOKClicked() {
        String n = cTime + NoteFragment.note;
        saveNote(n);
        hideFragment();
    }

    private void saveNote(String n) {
        if(YoutubeActivity.nt.equals("")) {
            noteList.add(n);
        }
        else
        {
            noteList.remove(nts);
            noteList.add(n);
        }
        updateList();
    }

    private void updateList() {
        Collections.sort((List) noteList);
        String temp = "";
        for (String s : noteList) {
            temp += s + splitter;
        }
        editor.putString(myUri, temp);
        editor.apply();
        nla = new NoteListAdapter(noteList);
        note.setAdapter(nla);
    }

    private void hideFragment() {
        if(fragmentVisible)
        {
            fragmentManager.beginTransaction()
                    .remove(noteFragment)
                    .commit();
            fragmentVisible = false;
            add.setVisibility(View.VISIBLE);
        }
    }

    private void showFragment() {
        if(!fragmentVisible)
        {
            fragmentManager = getFragmentManager();
            noteFragment = new NoteFragment();
            fragmentManager.beginTransaction().
                    add(R.id.raLayout, noteFragment).
                    commit();
            fragmentVisible = true;
            add.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Private Class for listView
     */
    private class NoteListAdapter extends BaseAdapter
    {

        ArrayList<String> s;
        protected NoteListAdapter(ArrayList<String> s1)
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
            v = getLayoutInflater().inflate(R.layout.player_current_list, null);

            TextView lbl = (TextView) v.findViewById(R.id.note);
            TextView ts = (TextView) v.findViewById(R.id.timeStamp);
            String temp = s.get(pos);
            String n = temp.substring(temp.indexOf(" ") + 1);
            String t = temp.substring(0, temp.indexOf(" "));
            ts.setText(t);
            lbl.setText(n);

            return v;
        }

    }
}