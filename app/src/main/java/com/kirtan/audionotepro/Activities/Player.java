package com.kirtan.audionotepro.Activities;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kirtan.audionotepro.Fragments.AudioNoteFragment;
import com.kirtan.audionotepro.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kirtan on 5/23/16.
 */
public class Player extends AppCompatActivity implements AudioNoteFragment.OnClickedListener {

    public static MediaPlayer mediaPlayer;
    Button pause, skipLeft, skipRight;
    TextView currentTime, finalTime, t;
    SeekBar seekBar;
    ListView note;
    double startTime;
    private Handler myHandler = new Handler();
    ArrayList<String> noteList;
    SharedPreferences myPrefs;
    SharedPreferences.Editor editor;
    private String cTime, n = "", uri = "", real = "", nts = "";
    final String splitter = "/////";
    public static String nt = "", random="";
    int currentNotePos;
    NoteListAdapter nla;
    private boolean isAppOpen, fragmentVisible;
    ImageView shareButton;
    final int PICK_FILE = 0;
    Uri myUri;
    File exportedFile = null;
    private FragmentManager fragmentManager;
    private AudioNoteFragment audioNoteFragment;
    FloatingActionButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        shareButton = (ImageView) findViewById(R.id.shareButton);
        pause = (Button) findViewById(R.id.pauseButton);
        currentTime = (TextView) findViewById(R.id.currentTime);
        finalTime = (TextView) findViewById(R.id.finalTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        note = (ListView) findViewById(R.id.note);
        t = (TextView) findViewById(R.id.noteText);
        skipLeft = (Button) findViewById(R.id.leftskip);
        skipLeft.setEnabled(false);
        skipRight = (Button) findViewById(R.id.rightskip);
        isAppOpen = true;
        mediaPlayer = new MediaPlayer();
        random = "";

        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();

        add = (FloatingActionButton) findViewById(R.id.fab);

        currentNotePos = -1;
        fragmentVisible = false;
        noteList = new ArrayList<>();
        Intent intent = getIntent();
        String file = intent.getStringExtra("file");
        getSupportActionBar().setTitle(file);
        myUri = Uri.parse(myPrefs.getString(file, ""));
        uri = myUri.toString();
        Log.d("URI", uri);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(this.getBaseContext(), myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (myPrefs.contains(uri)) {
                n = myPrefs.getString(uri, "");
                noteList = new ArrayList<>();
                if(n.contains(splitter)){
                    for(String s: n.split(splitter))
                    {
                        noteList.add(s);
                    }
                }
                else
                {
                    Scanner in = new Scanner(n);
                    String temp = "";
                    while (in.hasNextLine()) {
                        temp = in.nextLine();
                        noteList.add((temp.trim()));
                        temp+=splitter;
                    }
                    editor.putString(uri, temp);
                    editor.apply();
                }
                noteList.remove("");
                Collections.sort((List) noteList);
                nla = new NoteListAdapter(noteList);
                note.setAdapter(nla);
            }
            double fTime = mediaPlayer.getDuration();
            seekBar.setMax((int) fTime);
            finalTime.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes((long) fTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) fTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) fTime)))
            );

            myHandler.postDelayed(UpdateSongTime, 100);
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleText();
            }

            private void toggleText() {
                if (pause.getText().toString().equals("Pause")) {
                    pause.setText("Play");
                    mediaPlayer.pause();
                } else {
                    pause.setText("Pause");
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
                cn = cn-10;
                int min = (Integer.parseInt(temp.substring(0, temp.indexOf(":"))));
                int sec = (Integer.parseInt(temp.substring(temp.indexOf(":") + 1))-10);
                int t = (int)(TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
                mediaPlayer.pause();
                mediaPlayer.seekTo(t);
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
                if(cn >= 10)
                {
                    skipLeft.setEnabled(true);
                }
                else
                {
                    skipLeft.setEnabled(false);
                }
                if(cn >= fn-10)
                {
                    skipRight.setEnabled(false);
                }
                else
                {
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
                if(cn >= 10)
                {
                    skipLeft.setEnabled(true);
                }
                else
                {
                    skipLeft.setEnabled(false);
                }
                if(cn >= fn-10)
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
                    nt = "";
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
                AlertDialog.Builder builder = new AlertDialog.Builder(Player.this);
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

    /**
     * Edits the string(note)
     * @param s - String to be edited
     */
    private void edit(String s) {
        nt = s.substring(s.indexOf(" ")+1);
        cTime = s.substring(0, s.indexOf(" ")+1);
        showFragment();
    }

    /**
     * Deleted the note from the list
     * @param s - Note to be deleted from the list
     */
    private void delete(String s)
    {
        noteList.remove(s);
        updateList();
    }

    /**
     * Share the notes and audio with others
     */
    private void share() {
        export();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Audio and Notes for " + getSupportActionBar().getTitle().toString());
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(myUri);
        uris.add(Uri.fromFile(exportedFile));
        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(sharingIntent, "Share audio and notes via"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppOpen = false;
        if(mediaPlayer != null && mediaPlayer.isPlaying())
        {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppOpen = true;
        if(mediaPlayer != null)
        {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                myHandler.postDelayed(UpdateSongTime, 100);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppOpen = false;
        if(mediaPlayer != null)
        {
            mediaPlayer.stop();
        }
    }

    /**
     * Thread that runs with the audio
     */
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {

            if (mediaPlayer.isPlaying() && isAppOpen) {
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
                    pause.setText("Play");
                }
                else {
                    pause.setText("Pause");
                }
                checkCurrentPos();
                myHandler.postDelayed(this, 100);
            }
        }
    };

    /**
     * Checks the current position of the audio and updates the list accordingly
     */
    private void checkCurrentPos() {
        if(noteList != null) {
            for (int i = 0; i < noteList.size(); i++) {
                String temp = noteList.get(i);
                if(!temp.trim().equals("")) {
                    String toCheck = temp.substring(0, temp.indexOf(" ") - 1);
                    if (currentTime.getText().toString().equals(toCheck)) {
                        currentNotePos = i;
                        nla = new NoteListAdapter(noteList);
                        note.setAdapter(nla);
                        note.setSelection(currentNotePos);
                        break;
                    }
                }
                else
                {
                    noteList.remove(i);
                    i--;
                    updateList();
                }
            }
        }
        String temp = currentTime.getText().toString();
        String temp2 = finalTime.getText().toString();
        int cn = Integer.parseInt(Integer.parseInt(temp.substring(0, temp.indexOf(":"))) + "" +
                Integer.parseInt(temp.substring(temp.indexOf(":") + 1)));
        int fn = Integer.parseInt(Integer.parseInt(temp2.substring(0, temp2.indexOf(":"))) + "" +
                Integer.parseInt(temp2.substring(temp2.indexOf(":")+1)));
        if(cn >= 10)
        {
            skipLeft.setEnabled(true);
        }
        else
        {
            skipLeft.setEnabled(false);
        }
        if(cn >= fn-10)
        {
            skipRight.setEnabled(false);
        } else
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

    /**
     * Hides the fragment
     */
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

    /**
     * Shows the fragment
     */
    private void showFragment() {
        if(!fragmentVisible)
        {
            fragmentManager = getFragmentManager();
            audioNoteFragment = new AudioNoteFragment();
            fragmentManager.beginTransaction().
                    add(R.id.playerLayout, audioNoteFragment).
                    commit();
            fragmentVisible = true;
            add.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Saves the note
     * @param s - The note to be saved
     */
    public void saveNote(String s)
    {
        if(nt.equals("")) {
            noteList.add(s);
        }
        else
        {
            noteList.remove(nts);
            noteList.add(s);
        }
        updateList();
    }

    /**
     * Updates the list
     */
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
            if (pos != currentNotePos)
                v = getLayoutInflater().inflate(R.layout.player_list, null);
            else
                v = getLayoutInflater().inflate(R.layout.player_current_list, null);

            TextView lbl = (TextView) v.findViewById(R.id.note);
            TextView ts = (TextView) v.findViewById(R.id.timeStamp);
            String temp = s.get(pos);
            String t = temp.substring(0, temp.indexOf(": ")+1);
            String n = temp.substring(temp.indexOf(": ") + 2);
            ts.setText(t);
            lbl.setText(n);

            return v;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.export)
        {
            if(export())
            {
                Toast.makeText(Player.this, "Exported Successfully to 'My Files/Audio Note/Notes/'", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(Player.this, "Export Unsuccessful!", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            //intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE );

        }
        return true;
    }

    /**
     * Exports the notes to the file on the device
     * @return  Returns true if exported successfully, false otherwise
     */
    private boolean export() {

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Audio Note Pro" + File.separator + "Notes");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String outputFile = folder + File.separator + getSupportActionBar().getTitle().toString() + ".txt";
        File n = new File(outputFile);
        exportedFile = n;
        try {
            PrintWriter pw = new PrintWriter(n);
            pw.println(myPrefs.getString(uri, ""));
            pw.close();
            //Toast.makeText(Player.this, "Exported Successfully!", Toast.LENGTH_LONG).show();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            File f = new File(uri.getPath());
            importFrom(f);
        }
    }

    /**
     * Import the notes from the file
     * @param f - File from which the notes will be imported
     */
    private void importFrom(File f) {
        try {
            Scanner scanner = new Scanner(f);
            scanner = scanner.useDelimiter("njfbvjhk");
            String fl = "";
            while(scanner.hasNextLine())
            {
                fl+=scanner.nextLine()+ "\n";
            }
            if(!fl.contains(splitter)) {
                n = myPrefs.getString(uri, "");
                noteList = new ArrayList<>();
                for(String s: n.split(splitter))
                {
                    if(!s.trim().equals(""))
                        noteList.add(s);
                }
                String temp = "";
                scanner = new Scanner(fl);
                while(scanner.hasNextLine())
                {
                    String s = scanner.nextLine();
                    if(s.contains(": "))
                    {
                        temp = temp.trim();
                        noteList.add(temp);
                        temp = s+"\n";
                    }
                    else
                    {
                        temp += s+"\n";
                    }
                }
                if(!noteList.contains(temp))
                    noteList.add(temp.trim());
                noteList.remove("");
            }
            else
            {
                noteList = new ArrayList<>();
                n = myPrefs.getString(uri, "");
                for(String s: n.split(splitter))
                {
                    if(!s.trim().equals(""))
                        noteList.add(s);
                }
                for(String s: fl.split(splitter))
                {
                    if(!s.trim().equals(""))
                        noteList.add(s);
                }
            }
            updateList();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}