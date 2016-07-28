package com.kirtan.audionotepro.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kirtan.audionotepro.R;

/**
 * Created by Kirtan on 7/26/16.
 */
public class SignupFragment extends Fragment {
    private OnClickedListener mCallback;
    private View v;
    private EditText fName, lName, email, password;
    private Button signup;
    float x1,x2;
    float y1, y2;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    /**
     * Interface for the fragment
     */
    public interface OnClickedListener {
        void onSignupCloseClicked();
        void onSignupSuccess();
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
        v = inflater.inflate(R.layout.signup_fragment, container, false);
        final RelativeLayout relativeLayout = (RelativeLayout) v.findViewById(R.id.signupLayout);
        fName = (EditText) v.findViewById(R.id.firstName);
        lName = (EditText) v.findViewById(R.id.lastName);
        email = (EditText) v.findViewById(R.id.email);
        password = (EditText) v.findViewById(R.id.password);
        signup = (Button) v.findViewById(R.id.login);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    //Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                //updateUI(user);
                // [END_EXCLUDE]
            }
        };

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vi) {
                if(!validForm()){
                    Snackbar.make(relativeLayout, "Please fill in all the fields", Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).
                        addOnCompleteListener((Activity) v.getContext(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Snackbar.make(relativeLayout, "Authentication failed.",
                                            Snackbar.LENGTH_SHORT).show();
                                }
                                if(task.isSuccessful()){
                                    Snackbar.make(relativeLayout, "Authentication successful.",
                                            Snackbar.LENGTH_SHORT).show();
                                    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    dbr.child("users").child(user.getUid()).setValue(user);
                                    dbr.child("users").child(user.getUid()).child("Name").setValue(
                                            fName.getText().toString() + " " + lName.getText().toString()
                                    );
                                    mCallback.onSignupSuccess();
                                }
                            }
                        });
            }
        });


        if(relativeLayout != null)
        {
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
                                    mCallback.onSignupCloseClicked();
                                }
                            }
                            break;
                        }
                    }
                    return true;
                }
            });
        }
        return v;
    }

    private boolean validForm() {
        boolean valid = true;
        if(fName.getText().toString().trim().equals("")){
            fName.setError("Required");
            valid = false;
        }
        if(lName.getText().toString().trim().equals("")){
            lName.setError("Required");
            valid = false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()){
            email.setError("Invalid E-mail");
            valid = false;
        }
        if(password.getText().toString().trim().length() < 8 ||
                password.getText().toString().trim().length() > 16){
            password.setError("Password must be 8 - 16 characters long");
            valid = false;
        }
        return valid;
    }
}
