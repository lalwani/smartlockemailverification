package com.saurabh.demo.smartlockemailverification;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.blockstore.Blockstore;
import com.google.android.gms.auth.blockstore.BlockstoreClient;
import com.google.android.gms.auth.blockstore.StoreBytesData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class FirstFragment extends Fragment {

    private Button saveButton;
    private Button retrieveButton;
    private EditText editText;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    private void saveCredentials(String text) {
        BlockstoreClient client = Blockstore.getClient(getActivity());
        client.isEndToEndEncryptionAvailable()
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Log.d("xxxx", "blockstore encryption enabled " + aBoolean);
                        saveCredentialsInternal(text);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("xxxx", "blockstore encryption not available " + e.getMessage());
                        saveCredentialsInternal(text);
                    }
                });
    }

    private void saveCredentialsInternal(String text) {
        StoreBytesData bytesData = new StoreBytesData.Builder()
                .setBytes(text.getBytes())
                .setShouldBackupToCloud(true)
                .build();
        Blockstore.getClient(getActivity())
                .storeBytes(bytesData)
                .addOnSuccessListener(result ->
                        Log.d("xxxx", "Stored: " + result))
                .addOnFailureListener(exception ->
                        Log.d("xxxx", "credentials could not be saved " + exception.getMessage()));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText editText = view.findViewById(R.id.some_text);
        saveButton = view.findViewById(R.id.button_save);
        saveButton.setOnClickListener(view1 -> {
            saveCredentials(editText.getText().toString());
        });
        view.findViewById(R.id.button_retrieve).setOnClickListener(view1 -> {
                    Blockstore.getClient(view1.getContext())
                            .retrieveBytes()
                            .addOnSuccessListener(result -> {
                                        Log.d("xxxx", "Retrieved: " + new String(result));
                                        editText.setText(new String(result));
                                    }
                            ).addOnFailureListener(e ->
                                    Log.e("xxxx", "Failed to retrieve bytes", e)
                            );
                }
        );
    }
}