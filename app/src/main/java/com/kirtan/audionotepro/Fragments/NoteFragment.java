package com.kirtan.audionotepro.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kirtan.audionotepro.Activities.YoutubeActivity;
import com.kirtan.audionotepro.R;

/**
 * Created by Kirtan on 5/23/16.
 */
public class NoteFragment extends Fragment {

    private EditText input;
    private Button ok, close;
    private View v;
    private OnClickedListener mCallback;
    public static String note;
    // Container Activity must implement this interface
    public interface OnClickedListener {
        void onCloseClicked();
        void onOKClicked();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            mCallback = (OnClickedListener) context;
        }
        catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnCloseClickedListener");
        }
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try {
            mCallback = (OnClickedListener) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnCloseClickedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_notetaker, container, false);
        input = (EditText) v.findViewById(R.id.input);
        ok = (Button) v.findViewById(R.id.ok);
        close = (Button) v.findViewById(R.id.close);

        String nt = YoutubeActivity.nt;
        input.setText(nt);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String in = input.getText().toString();
                if(in.length() < 1 || in.trim().equals(""))
                {
                    Toast.makeText(v.getContext(), "Cannot add an empty note!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    note = input.getText().toString();
                    if (mCallback != null) {
                        mCallback.onOKClicked();
                    }
                    if (v != null) { //closes the keyboard if it is open
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallback != null) {
                    mCallback.onCloseClicked();
                }
                if (v != null) { //closes the keyboard if it is open
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        return v;
    }
}