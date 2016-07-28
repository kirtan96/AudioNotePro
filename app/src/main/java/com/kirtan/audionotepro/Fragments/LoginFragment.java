package com.kirtan.audionotepro.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kirtan.audionotepro.R;

/**
 * Created by Kirtan on 7/25/16.
 */
public class LoginFragment extends Fragment{

    private OnClickedListener mCallback;
    private View v;
    private EditText email, password;
    private Button signin;
    private TextView signup;
    float x1,x2;
    float y1, y2;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


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
        v = inflater.inflate(R.layout.signin_fragment, container, false);
        final RelativeLayout relativeLayout = (RelativeLayout) v.findViewById(R.id.signinLayout);
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) v.findViewById(android.R.id
                .content);
        email = (EditText) v.findViewById(R.id.email);
        password = (EditText) v.findViewById(R.id.password);
        signin = (Button) v.findViewById(R.id.login);
        signup = (TextView) v.findViewById(R.id.signup);
        mAuth = FirebaseAuth.getInstance();

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vi) {
                if(!validInput()){
                    Snackbar sn = Snackbar.make(relativeLayout, "Please fill in all the fields", Snackbar.LENGTH_LONG);
                    sn.show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener((Activity) v.getContext(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Log.d("Signin", "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w("Signin", "signInWithEmail", task.getException());
                                    Snackbar.make(relativeLayout, "Authentication failed.",
                                            Snackbar.LENGTH_SHORT).show();
                                }
                                if (task.isSuccessful()) {
                                    Log.w("Signin", "signInWithEmail", task.getException());
                                    Snackbar.make(relativeLayout, "Authentication successful.",
                                            Snackbar.LENGTH_SHORT).show();
                                    mCallback.onCloseClicked();
                                }

                                // [START_EXCLUDE]
                                //hideProgressDialog();
                                // [END_EXCLUDE]
                            }
                        });
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
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

    private boolean validInput() {
        boolean valid = true;
        if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches())
        {
            email.setError("Invalid E-mail");
            valid = false;
        }
        if(password.getText().toString().trim().length() == 0){
            password.setError("Required");
            valid = false;
        }
        return valid;
    }
}