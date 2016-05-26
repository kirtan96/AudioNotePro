package com.kirtan.audionotepro.Activities;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.kirtan.audionotepro.Fragments.NoteFragment;
import com.kirtan.audionotepro.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kirtan on 5/23/16.
 */
public class YoutubeActivity extends YouTubeBaseActivity implements NoteFragment.OnClickedListener {

    private YouTubePlayerView youTubePlayerView;
    private NoteFragment noteFragment;
    private YouTubePlayer youTubePlayer;
    private YouTubePlayer.OnInitializedListener onInitializedListener;
    private FloatingActionButton add;
    private ListView ytnList;
    private FragmentManager fragmentManager;
    private SharedPreferences myPrefs;
    private SharedPreferences.Editor editor;
    private boolean fragmentVisible;
    private int currentNotePos;
    private NoteListAdapter nla;
    private ArrayList<String> noteList;
    private final String
            MY_YOUTUBE_FILES = "myYouTubeFiles",
            MY_YOUTUBE_URLS = "myYouTubeURLS";
    private String cTime, videoId, splitter, nts, n, fname;
    public static String nt = "";
    private File exportedFile;
    private ImageView shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.YoutubePlayer);
        ytnList = (ListView) findViewById(R.id.ytnList);
        add = (FloatingActionButton) findViewById(R.id.fab);
        shareButton = (ImageView) findViewById(R.id.shareButton);
        noteFragment = new NoteFragment();
        myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = myPrefs.edit();
        Intent intent = getIntent();
        videoId = intent.getStringExtra("VideoID");
        fname = intent.getStringExtra("File");
        setTitle(fname.replace(" (Youtube)", ""));
        splitter = "/////";
        nts = "";
        fragmentVisible = false;
        currentNotePos = -1;
        ytnList.setLongClickable(true);
        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer yPlayer, boolean b) {
                youTubePlayer = yPlayer;
                youTubePlayer.loadVideo(videoId);
                youTubePlayer.setShowFullscreenButton(false);
                youTubePlayer.setManageAudioFocus(false);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };
        youTubePlayerView.initialize(String.valueOf(R.string.YAPI), onInitializedListener);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fragmentVisible)
                {
                    nt = "";
                    int n = youTubePlayer.getCurrentTimeMillis();
                    cTime = String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes((long) n),
                            TimeUnit.MILLISECONDS.toSeconds((long) n) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) n))) + ": ";
                    showFragment();
                }
                else
                {
                    hideFragment();
                }
            }
        });

        generateList();

        ytnList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = noteList.get(position).substring(0, noteList.get(position).indexOf(" "));
                playFrom(temp);
            }
        });

        ytnList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(YoutubeActivity.this);
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

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    /**
     * Edits the note
     * @param s - The note
     */
    private void edit(String s)
    {
        nt = s.substring(s.indexOf(" ") + 1);
        cTime = s.substring(0, s.indexOf(" ")+1);
        showFragment();
    }

    /**
     * Deletes the note
     * @param s - The note
     */
    private void delete(String s)
    {
        noteList.remove(s);
        updateList();
    }

    /**
     * Plays the video from given time stamp
     * @param temp - The time stamp
     */
    private void playFrom(String temp) {
        int min = Integer.parseInt(temp.substring(0,2));
        int sec = Integer.parseInt(temp.substring(3,5));
        int t = (int) (TimeUnit.MINUTES.toMillis(min) + TimeUnit.SECONDS.toMillis(sec));
        youTubePlayer.seekToMillis(t);
        if(!youTubePlayer.isPlaying())
            youTubePlayer.play();
    }

    /**
     * Generates the list
     */
    private void generateList() {
        noteList = new ArrayList<>();
        for(String s: myPrefs.getString(videoId, "").split(splitter))
        {
            if(!s.trim().equals(""))
                noteList.add(s);
        }
        Collections.sort((List)noteList);
        nla = new NoteListAdapter(noteList);
        ytnList.setAdapter(nla);
    }

    /**
     * Hides the fragment
     */
    private void hideFragment() {
        if(fragmentVisible)
        {
            fragmentManager.beginTransaction()
                    .remove(noteFragment)
                    .commit();
            fragmentVisible = false;
            add.setVisibility(View.VISIBLE);
            if(!youTubePlayer.isPlaying())
            {
                youTubePlayer.play();
            }
        }
    }

    /**
     * Shows the fragment
     */
    private void showFragment() {
        if(!fragmentVisible)
        {
            youTubePlayer.pause();
            fragmentManager = getFragmentManager();
            noteFragment = new NoteFragment();
            fragmentManager.beginTransaction().
                    add(R.id.ytlayout, noteFragment).
                    commit();
            fragmentVisible = true;
            add.setVisibility(View.INVISIBLE);
        }
    }

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

    /**
     * Saves the note
     * @param n - The note
     */
    private void saveNote(String n) {
        if(nt.equals("")) {
            noteList.add(n);
        }
        else
        {
            noteList.remove(nts);
            noteList.add(n);
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
        editor.putString(videoId, temp);
        editor.apply();
        nla = new NoteListAdapter(noteList);
        ytnList.setAdapter(nla);
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
            String n = temp.substring(temp.indexOf(" ") + 1);
            String t = temp.substring(0, temp.indexOf(" "));
            ts.setText(t);
            lbl.setText(n);

            return v;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_youtube, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.export)
        {
            if(export())
            {
                Toast.makeText(YoutubeActivity.this, "Exported Successfully to 'My Files/Audio Note/Notes/'", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(YoutubeActivity.this, "Export Unsuccessful!", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/plain");
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 0 );

        }
        return true;
    }

    /**
     * Exports the notes to the file on the device
     * @return - True if export is successful, False otherwise
     */
    private boolean export() {

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "Audio Note Pro" + File.separator + "Notes");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String outputFile = folder + File.separator + fname + ".txt";
        File n = new File(outputFile);
        exportedFile = n;
        try {
            PrintWriter pw = new PrintWriter(n);
            pw.println(myPrefs.getString(videoId, ""));
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

        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            File f = new File(uri.getPath());
            importFrom(f);
        }
    }

    /**
     * Imports all the notes from the file
     * @param f - The file
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
                n = myPrefs.getString(videoId, "");
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
                n = myPrefs.getString(videoId, "");
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

    /**
     * Shares the notes with others
     */
    private void share() {
        export();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(exportedFile));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "www.youtube.com/watch?v="+videoId);
        startActivity(Intent.createChooser(sharingIntent, "Share audio and notes via"));
    }
}