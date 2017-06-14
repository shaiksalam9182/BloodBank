package com.healthsaathi.bloodbank;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Loginwithgmailandfb extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,View.OnClickListener{



    FirebaseAuth mauth;
    GoogleApiClient mapiclient;
    FirebaseAuth.AuthStateListener mauthlistener;
    FirebaseDatabase mdatabase;
    private CallbackManager mcallmangaer;
    DatabaseReference mdatabasereference;
    ProgressDialog pdloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_loginwithgmailandfb);

        pdloading  = new ProgressDialog(Loginwithgmailandfb.this);


        pdloading.setCancelable(false);

        mdatabase = FirebaseDatabase.getInstance();
        mdatabasereference = mdatabase.getReference();


        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.bgmaillogin).setOnClickListener(this);
        mcallmangaer = CallbackManager.Factory.create();

        LoginButton loginbutton = (LoginButton)findViewById(R.id.login_button);
        loginbutton.setReadPermissions("email","public_profile");
        loginbutton.registerCallback(mcallmangaer, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleaccesstoken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(Loginwithgmailandfb.this,"Cancelled",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Loginwithgmailandfb.this,"Erro Occured\nTray Again Later",Toast.LENGTH_LONG).show();

            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mapiclient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        mauth = FirebaseAuth.getInstance();

        mauthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!=null){
                    Toast.makeText(Loginwithgmailandfb.this,user.getEmail(),Toast.LENGTH_LONG).show();
                    if (user.getEmail()!=null){

                        startActivity(new Intent(Loginwithgmailandfb.this,MainActivity.class));
                        finish();
                    }
                    //gmaillogin.setText(user.getEmail());
                }else {

                    Toast.makeText(Loginwithgmailandfb.this,"Signed Out",Toast.LENGTH_LONG).show();
                }
            }
        };


    }

    private void handleaccesstoken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mauth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Toast.makeText(Loginwithgmailandfb.this,"Status"+task.isSuccessful(),Toast.LENGTH_LONG).show();
                            //startActivity(new Intent(Loginwithgmailandfb.this,MainActivity.class));
                            //status = true;
                            //finish();

                        }else {

                            Toast.makeText(Loginwithgmailandfb.this,"Status"+task.getException(),Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }




    public void signinwithgmail() {
        pdloading.setMessage("Getting the Google Accounts From the Device\nPlease Wait");
        pdloading.show();
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mapiclient);
        startActivityForResult(intent,9001);
        pdloading.setMessage("Authenticating With Google Server\nPlease Wait..");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mcallmangaer.onActivityResult(requestCode,resultCode,data);
        if (requestCode==9001){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                authwithfirebase(account);
            }else {

            }
        }



    }

    private void authwithfirebase(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mauth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            pdloading.dismiss();
                        }else {
                            pdloading.dismiss();
                            Toast.makeText(Loginwithgmailandfb.this,"Status:"+task.getException(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mauth.addAuthStateListener(mauthlistener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(Loginwithgmailandfb.this,"Google Play Services Error",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i){
            case R.id.bgmaillogin:
                signinwithgmail();
                break;

        }
    }


}
