package com.kirtan.audionotepro.Activities;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kirtan.audionotepro.Fragments.AudioNoteFragment;
import com.kirtan.audionotepro.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kirtan on 5/23/16.
 */
public class SearchResult extends AppCompatActivity implements AudioNoteFragment.OnClickedListener{

    public static MediaPlayer mediaPlayer;
    TextView currentTime,finalTime;
    SeekBar seekBar;
    ListView note;
    double startTime;
    private Handler myHandler = new Handler();
    String n = "",uri = "",cTime = "", search = "", real = "", nts = "";
    ArrayList<String> noteList;
    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;
    int currentNotePos;
    NoteListAdapter nla;
    final String splitter = "/////";
    boolean fragmentVisible;
    AudioNoteFragment audioNoteFragment;
    FragmentManager fragmentManager;
    FloatingActionButton add, pause, skipLeft, skipRight;
    Drawable pau;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pause = (FloatingActionButton) findViewById(R.id.pauseButton);
        currentTime = (TextView) findViewById(R.id.currentTime);
        finalTime = (TextView) findViewById(R.id.finalTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        note = (ListView) findViewById(R.id.note);
        skipLeft = (FloatingActionButton) findViewById(R.id.leftskip);
        skipLeft.setEnabled(false);
        skipRight = (FloatingActionButton) findViewById(R.id.rightskip);
        mediaPlayer = new MediaPlayer();
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();
        fragmentVisible = false;
        Player.random = "sr";
        pau = getResources().getDrawable(android.R.drawable.ic_media_pause);

        add = (FloatingActionButton) findViewById(R.id.fab);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleText();
            }

            private void toggleText() {
                if (pause.getDrawable().getConstantState().equals(pau.getConstantState())) {
                    pause.setImageResource(android.R.drawable.ic_media_play);
                    mediaPlayer.pause();
                } else {
                    pause.setImageResource(android.R.drawable.ic_media_pause);
                    mediaPlayer.start();
                    myHandler.postDelayed(UpdateSongTime, 100);
                }
            }
        });


        skipLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = currentTime.getText().toString();
                String temp2 = finalTime.getText().toString();
                int cn = Integer.parseInt(Integer.parseInt(temp.substring(0, temp.indexOf(":"))) + "" +
                        (Integer.parseInt(temp.substring(temp.indexOf(":") + 1))));
                int fn = Integer.parseInt(Integer.parseInt(temp2.substring(0, temp2.indexOf(":"))) + "" +
                        Integer.parseInt(temp2.substring(temp2.indexOf(":") + 1)));
                cn = cn - 10;
                int min = (Integer.parseInt(temp.substring(0, temp.indexOf(":"))));
                int sec = (Integer.parseInt(temp.substring(temp.indexOf(":") + 1)) - 10);
                int t = (int) (TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                mediaPlayer.pause();
                mediaPlayer.seekTo(t);
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
                if (cn > 10) {
                    skipLeft.setEnabled(true);
                } else {
                    skipLeft.setEnabled(false);
                }
                if (cn > fn - 10) {
                    skipRight.setEnabled(false);
                } else {
                    skipRight.setEnabled(true);
                }
            }
        });

        skipRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = currentTime.getText().toString();
                String temp2 = finalTime.getText().toString();
                int cn = Integer.parseInt(Integer.parseInt(temp.substring(0, temp.indexOf(":"))) + "" +
                        Integer.parseInt(temp.substring(temp.indexOf(":") + 1)));
                int fn = Integer.parseInt(Integer.parseInt(temp2.substring(0, temp2.indexOf(":"))) + "" +
                        Integer.parseInt(temp2.substring(temp2.indexOf(":") + 1)));
                cn = cn+10;
                int min = (Integer.parseInt(temp.substring(0, temp.indexOf(":"))));
                int sec = (Integer.parseInt(temp.substring(temp.indexOf(":") + 1))+10);
                int t = (int)(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                mediaPlayer.pause();
                mediaPlayer.seekTo(t);
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
                if(cn > 10)
                {
                    skipLeft.setEnabled(true);
                }
                else
                {
                    skipLeft.setEnabled(false);
                }
                if(cn > fn-10)
                {
                    skipRight.setEnabled(false);
                }
                else
                {
                    skipRight.setEnabled(true);
                }
            }
        });

        assert add != null;
        add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(!fragmentVisible) {
                    Player.nt = "";
                    cTime = currentTime.getText().toString() + ": ";
                    showFragment();
                }
                else {
                    hideFragment();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(progress);
                    mediaPlayer.start();
                    myHandler.postDelayed(UpdateSongTime, 100);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        currentNotePos = -1;
        Intent intent = getIntent();
        String file = intent.getStringExtra("file");
        setTitle(file);
        Uri myUri = Uri.parse(myPrefs.getString(file, ""));
        search = intent.getStringExtra("search");
        uri = myUri.toString();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (myPrefs.contains(uri)) {
                n = myPrefs.getString(uri, "");
                Scanner in  = new Scanner(n);
                noteList = new ArrayList<>();
                if(n.contains(splitter)){
                    for(String s: n.split(splitter))
                    {
                        noteList.add(s);
                    }
                }
                else
                {
                    in = new Scanner(n);
                    String temp = "";
                    while (in.hasNextLine()) {
                        temp = in.nextLine();
                        noteList.add((temp.trim()));
                        temp+=splitter;
                    }
                    editor.putString(uri, temp);
                    editor.apply();
                }
                /*while(in.hasNextLine())
                {
                    String temp = in.nextLine();
                    noteList.add(temp.trim());
                    if(temp.toLowerCase().contains(search.toLowerCase()))
                    {
                        temp = temp.substring(0, 5);
                        int min = Integer.parseInt(temp.substring(0,2));
                        int sec = Integer.parseInt(temp.substring(3, 5));
                        int t = (int)(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(t);
                        mediaPlayer.start();
                        myHandler.postDelayed(UpdateSongTime, 100);
                    }
                }*/
                noteList.remove("");
                Collections.sort((List) noteList);
                nla = new NoteListAdapter(noteList);
                note.setAdapter(nla);
                for(String s: noteList)
                {
                    if(s.toLowerCase().contains(search.toLowerCase()))
                    {
                        s = s.substring(0, s.indexOf(": "));
                        int min = Integer.parseInt(s.substring(0,s.indexOf(":")));
                        int sec = Integer.parseInt(s.substring(s.indexOf(":")+1));
                        int t = (int)(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                        mediaPlayer.pause();
                        mediaPlayer.seekTo(t);
                        mediaPlayer.start();
                        myHandler.postDelayed(UpdateSongTime, 100);
                        break;
                    }
                }
            }
            double fTime = mediaPlayer.getDuration();
            seekBar.setMax((int) fTime);
            finalTime.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) fTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) fTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) fTime)))
            );

            myHandler.postDelayed(UpdateSongTime, 100);
        } catch (IOException e) {
            Toast.makeText(this, "It seems like the audio file no longer exists on your device!",Toast.LENGTH_LONG).show();
            finish();
        }

        note.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String time = note.getItemAtPosition(position).toString();
                time = time.substring(0, time.indexOf(": "));
                int min = Integer.parseInt(time.substring(0, time.indexOf(":")));
                int sec = Integer.parseInt(time.substring(time.indexOf(":")+1));
                int t = (int) (TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                mediaPlayer.pause();
                mediaPlayer.seekTo(t);
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
            }
        });

        note.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchResult.this);
                builder.setTitle("Choose an option:");
                builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String s = noteList.get(position);
                        if (which == 0) {
                            nts = s;
                            edit(s);
                        } else {
                            delete(s);
                        }
                    }
                });

                builder.show();
                return true;
            }
        });
    }

    private void edit(String s) {
        Player.nt = s.substring(s.indexOf(" ")+1);
        cTime = s.substring(0, s.indexOf(" ")+1);
        showFragment();
    }

    private void delete(String s)
    {
        noteList.remove(s);
        updateList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer != null)
        {
            mediaPlayer.start();
            myHandler.postDelayed(UpdateSongTime, 100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
        }
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {

            if (mediaPlayer.isPlaying()) {
                startTime = mediaPlayer.getCurrentPosition();
                currentTime.setText(String.format("%02d:%02d",

                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) startTime)))
                );
                seekBar.setProgress((int) startTime);
                if(currentTime.getText().toString().equals(finalTime.getText().toString()))
                {
                    pause.setImageResource(android.R.drawable.ic_media_play);
                }
                else {
                    pause.setImageResource(android.R.drawable.ic_media_pause);
                }
                checkCurrentPos();
                myHandler.postDelayed(this, 100);
            }
        }
    };

    private void checkCurrentPos() {
        if(noteList != null) {
            for (int i = 0; i < noteList.size(); i++) {
                String temp = noteList.get(i);
                String toCheck = temp.substring(0, temp.indexOf(" ") - 1);
                if (currentTime.getText().toString().equals(toCheck)) {
                    currentNotePos = i;
                    nla = new NoteListAdapter(noteList);
                    note.setAdapter(nla);
                    note.setSelection(currentNotePos);
                    break;
                }
            }
        }
        String temp = currentTime.getText().toString();
        String temp2 = finalTime.getText().toString();
        int cn = Integer.parseInt(Integer.parseInt(temp.substring(0, temp.indexOf(":"))) + "" +
                Integer.parseInt(temp.substring(temp.indexOf(":") + 1)));
        int fn = Integer.parseInt(Integer.parseInt(temp2.substring(0, temp2.indexOf(":"))) + "" +
                Integer.parseInt(temp2.substring(temp2.indexOf(":")+1)));
        if(cn > 10)
        {
            skipLeft.setEnabled(true);
        }
        else
        {
            skipLeft.setEnabled(false);
        }
        if(cn > fn-10)
        {
            skipRight.setEnabled(false);
        }
        else
        {
            skipRight.setEnabled(true);
        }
    }

    @Override
    public void onCloseClicked() {
        hideFragment();
    }

    @Override
    public void onOKClicked() {
        String s = cTime + AudioNoteFragment.note;
        saveNote(s);
        hideFragment();
    }

    private void saveNote(String s) {
        if(Player.nt.equals("")) {
            noteList.add(s);
        }
        else
        {
            noteList.remove(nts);
            noteList.add(s);
        }
        updateList();
    }

    private void updateList() {
        Collections.sort((List) noteList);
        String temp = "";
        for (String s : noteList) {
            temp += s + splitter;
        }
        editor.putString(uri, temp);
        editor.apply();
        nla = new NoteListAdapter(noteList);
        note.setAdapter(nla);
    }

    private void hideFragment() {
        if(fragmentVisible)
        {
            fragmentManager.beginTransaction()
                    .remove(audioNoteFragment)
                    .commit();
            fragmentVisible = false;
            add.setVisibility(View.VISIBLE);
            mediaPlayer.start();
            myHandler.postDelayed(UpdateSongTime, 100);
        }
    }

    private void showFragment() {
        if(!fragmentVisible)
        {
            fragmentManager = getFragmentManager();
            audioNoteFragment = new AudioNoteFragment();
            fragmentManager.beginTransaction().
                    add(R.id.srLayout, audioNoteFragment).
                    commit();
            fragmentVisible = true;
            add.setVisibility(View.INVISIBLE);
        }
    }

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
            if (pos != currentNotePos)
                v = getLayoutInflater().inflate(R.layout.player_list, null);
            else
                v = getLayoutInflater().inflate(R.layout.player_current_list, null);

            TextView lbl = (TextView) v.findViewById(R.id.note);
            TextView ts = (TextView) v.findViewById(R.id.timeStamp);
            String temp = s.get(pos);
            String n = temp.substring(temp.indexOf(" ") + 1);
            String t = temp.substring(0, temp.indexOf(": "));
            ts.setText(t);
            lbl.setText(n);

            return v;
        }

    }

    @Override
    public void onBackPressed() {
        if(fragmentVisible)
        {
            hideFragment();
        }
        else {
            finish();
        }
    }
}