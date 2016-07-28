package com.kirtan.audionotepro.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.kirtan.audionotepro.R;

/**
 * Created by Kirtan on 7/27/16.
 */
public class MenuFragment extends Fragment {
    private OnClickedListener mCallback;
    private View v;
    private Button login;
    private TextView odallfiles, odaudiofiles, odrec, odyt, callfiles, caudiofiles, crec, cyt, onCloud;
    float x1,x2;
    float y1, y2;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    //TODO: Implement this fragment to the mainActivity

    /**
     * Interface for the fragment
     */
    public interface OnClickedListener {
        void onCloseClicked();
        void onSignUpClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnClickedListener) context;
        }
        catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnCloseClickedListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnClickedListener) activity;
        }
        catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnCloseClickedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.menu_fragment, container, false);
        final RelativeLayout relativeLayout = (RelativeLayout) v.findViewById(R.id.signinLayout);
        login = (Button) v.findViewById(R.id.login);
        odallfiles = (TextView) v.findViewById(R.id.odallfiles);
        odaudiofiles = (TextView) v.findViewById(R.id.odaudioFiles);
        odrec = (TextView) v.findViewById(R.id.odrecordings);
        odyt = (TextView) v.findViewById(R.id.odyoutube);
        callfiles = (TextView) v.findViewById(R.id.callfiles);
        caudiofiles = (TextView) v.findViewById(R.id.caudiofiles);
        crec = (TextView) v.findViewById(R.id.crecordings);
        cyt = (TextView) v.findViewById(R.id.cyoutube);
        onCloud = (TextView) v.findViewById(R.id.oncloud);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            callfiles.setVisibility(View.INVISIBLE);
            caudiofiles.setVisibility(View.INVISIBLE);
            crec.setVisibility(View.INVISIBLE);
            cyt.setVisibility(View.INVISIBLE);
            onCloud.setVisibility(View.INVISIBLE);
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback != null){
                    mCallback.onSignUpClicked();
                }
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

                        // if right to left sweep event on screen
                        if (x1 > x2)
                        {
                            if(mCallback != null){
                                mCallback.onCloseClicked();
                            }
                        }
                        break;
                    }
                }
                return true;
            }
        });

        return v;
    }
}
