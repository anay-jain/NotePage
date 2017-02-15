package com.example.jatin.notepage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by present on 2/13/17.
 */

public class Settings extends AppCompatActivity{

    private final int RC_SIGN_IN = 9001;
    private GoogleSignInHandler googleSignInHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        googleSignInHandler = new GoogleSignInHandler(this);

        SignInButton signInButton = (SignInButton)findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInHandler.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        if (googleSignInHandler.isSilentLogin()){
            handleSignInResult(googleSignInHandler.getGoogleSignInResult());
        } else {
            ((CardView)findViewById(R.id.profile_card_view)).setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //result returned from launching the intent
        switch(requestCode){
            case RC_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            ((CardView) findViewById(R.id.sign_in_card)).setVisibility(View.GONE);
            ((CardView) findViewById(R.id.profile_card_view)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.settings_username)).setText(account.getDisplayName());
            ((TextView) findViewById(R.id.settings_email)).setText(account.getEmail());
            ((CircleImageView) findViewById(R.id.profile_image)).setImageURI(account.getPhotoUrl());

        } else {
            Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
        }
    }

}
