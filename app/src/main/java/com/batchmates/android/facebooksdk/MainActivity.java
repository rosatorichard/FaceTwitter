package com.batchmates.android.facebooksdk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;

    private static final String Consumer_ID = "jRycL6qgPnk9A4X0TdXrDl4gw";
    private static final String Consumer_SECRET = "Eo8ViT53QxXka7GjK1DxF62fEkN8m8nMbubJ26Eu32CcPWnXq0";
    private AccessToken accessToken;
    private TwitterLoginButton loginButton2;

    CallbackManager mCallbackManager;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);


        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }


            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


        //Twitter
//        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
//        TwitterAuthToken authToken = session.getAuthToken();
//        String token = authToken.token;
//        String secret = authToken.secret;

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("CONSUMER_KEY", "CONSUMER_SECRET"))
                .debug(true)
                .build();
        Twitter.initialize(config);

        loginButton2 = (TwitterLoginButton) findViewById(R.id.login_buttonTwitter);
        loginButton2.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                TwitterSession session = result.data;
                String Username = session.getUserName();
                String Token = session.getAuthToken().token;
                String secret = session.getAuthToken().secret;
                Log.d(TAG, "success: We got twitter");

                final Call<User> user= new TwitterApiClient(session).getAccountService().verifyCredentials(true,false,false);
                user.enqueue(new retrofit2.Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        User userinfo=response.body();
                        String email=userinfo.email;
                        String discription=userinfo.description;
                        String location=userinfo.location;
                        int friendCount=userinfo.friendsCount;
                        Log.d(TAG, "onResponse: "+userinfo.name);
                        tvEmail.setText(userinfo.name);
                        somethingElse.setText("Number of Friends: "+friendCount);
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.d(TAG, "onFailure: Failure");
                    }
                });


            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });

    }



    @BindView(R.id.yourAttributes)
    Button btn;

    @BindView(R.id.myPicture)
    ImageView myPicture;

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.gender)
    TextView gender;

    @BindView(R.id.email)
    TextView tvEmail;

    @BindView(R.id.otherTwitter)
    TextView somethingElse;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);


        //twiiter
        loginButton2.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token.getToken());
        accessToken = token;

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            btn.setVisibility(View.VISIBLE);
//                            Intent intent=new Intent(MainActivity.this,FaceBookLogin.class);
//                            intent.putExtra("TOKEN",token.getToken());
//                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });

    }

    private void updateUI(Object o) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }

    public void doStuff(View view) {

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
//                            Log.d(TAG, "onCompleted: "+response.getJSONObject().get("id"));
//                            Log.d(TAG, "onCompleted: "+response.getJSONObject().get("name"));
//                            Log.d(TAG, "onCompleted: "+response.getJSONObject().get("gender"));
//                            Log.d(TAG, "onCompleted: "+response.getJSONObject().get("link"));
//                            Log.d(TAG, "onCompleted: "+response.getJSONObject().get("picture"));
                            JSONObject json = new JSONObject(response.getJSONObject().toString());
//                            JSONObject picture=json.getJSONObject("picture");
//                            JSONArray data=picture.getJSONArray("data");

                            Log.d(TAG, "onCompleted: " + response.getJSONObject().toString());
                            JSONObject url = json.getJSONObject("picture");
                            JSONObject data = url.getJSONObject("data");
                            String theurl = (String) data.get("url");
                            loadPicture(theurl);
                            name.setText(response.getJSONObject().get("name").toString());
                            gender.setText(response.getJSONObject().get("gender").toString());
                            Log.d(TAG, "onCompleted: " + theurl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Application code
                    }
                });
        Bundle parameters = new Bundle();
//        parameters.putString("fields", "id,name,link,gender,picture");
        parameters.putString("fields", "name,gender,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void loadPicture(String s) {
        Glide.with(MainActivity.this).load(s).into(myPicture);
    }
}
