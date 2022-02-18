package com.saurabh.demo.smartlockemailverification;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.blockstore.Blockstore;
import com.google.android.gms.auth.blockstore.BlockstoreClient;
import com.google.android.gms.auth.blockstore.StoreBytesData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "486393151765-rlkj2ciae907dgk70kotsciflsjgrehl.apps.googleusercontent.com";
    private ActivityResultLauncher launcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        launcher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            Credential credential = result.getData().getParcelableExtra(Credential.EXTRA_KEY);
            Log.d("xxxx", credential.getId());
            Log.d("xxxx", credential.getIdTokens().size() + " size");
            Log.d("xxxx", credential.getGivenName());
            Log.d("xxxx", credential.getFamilyName());
            Log.d("xxxx", credential.getIdTokens().get(0).getIdToken());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> verifyEmail(credential.getIdTokens().get(0).getIdToken()));
        });

//        CredentialRequest credentialRequest = new CredentialRequest.Builder()
//                .setCredentialHintPickerConfig(
//                        new CredentialPickerConfig.Builder()
//                                .setPrompt(CredentialPickerConfig.Prompt.CONTINUE)
//                                .setShowAddAccountButton(true)
//                                .build()
//                )
//                .setIdTokenRequested(true)
//                .setAccountTypes(IdentityProviders.GOOGLE, IdentityProviders.TWITTER)
//                .build();
//        credentialsClient.request(credentialRequest).addOnCompleteListener(
//                new OnCompleteListener<CredentialRequestResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<CredentialRequestResponse> task) {
//
//                        if (task.isSuccessful()) {
//                            // See "Handle successful credential requests"
//                            onCredentialRetrieved(task.getResult().getCredential());
//                            return;
//                        }
//
//                        // See "Handle unsuccessful and incomplete credential requests"
//                        // ...
//                    }
//                });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                saveCredentials();
                smartlockHint();
            }
        });

    }

    private void smartlockHint() {
        CredentialsClient credentialsClient = Credentials.getClient(this);
        HintRequest hintRequest = new HintRequest.Builder()
                .setHintPickerConfig(new CredentialPickerConfig.Builder()
                        .setShowCancelButton(true)
                        .setPrompt(CredentialPickerConfig.Prompt.CONTINUE)
                        .build())
                .setPhoneNumberIdentifierSupported(true)
                .setEmailAddressIdentifierSupported(false)
                .setAccountTypes(IdentityProviders.GOOGLE, IdentityProviders.FACEBOOK)
                .setIdTokenRequested(true)
                .build();

        PendingIntent intent = credentialsClient.getHintPickerIntent(hintRequest);
        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(intent.getIntentSender())
                .build();
        launcher.launch(intentSenderRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
        Log.d("xxxx", credential.getId());
        Log.d("xxxx", credential.getIdTokens().size() + " size");
        Log.d("xxxx", credential.getGivenName());
        Log.d("xxxx", credential.getFamilyName());
        Log.d("xxxx", credential.getIdTokens().get(0).getIdToken());

    }

    private void verifyEmail(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    // Specify the CLIENT_ID of the app that accesses the backend:
//                    .setAudience(Collections.singletonList(CLIENT_ID))
                    // Or, if multiple clients access the backend:
                    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(MainActivity.this, "Email verified", Toast.LENGTH_LONG).show();
                });
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Print user identifier
                String userId = payload.getSubject();
                System.out.println("User ID: " + userId);

                // Get profile information from payload
                String email = payload.getEmail();
                boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String locale = (String) payload.get("locale");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");

                // Use or store profile information
                // ...

            } else {
                Log.d("xxxx", "invalid id token");
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.d("xxxx", "exception aaya: " + e.getMessage());
        }

    }

    private void onCredentialRetrieved(Credential credential) {
        String accountType = credential.getAccountType();
        if (accountType == null) {
            // Sign the user in with information from the Credential.
//            signInWithPassword(credential.getId(), credential.getPassword());
        } else if (accountType.equals(IdentityProviders.GOOGLE)) {
            // The user has previously signed in with Google Sign-In. Silently
            // sign in the user with the same ID.
            // See https://developers.google.com/identity/sign-in/android/
            GoogleSignInOptions gso =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build();

            GoogleSignInClient signInClient = GoogleSignIn.getClient(this, gso);
            Task<GoogleSignInAccount> task = signInClient.silentSignIn();
            // ...
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}