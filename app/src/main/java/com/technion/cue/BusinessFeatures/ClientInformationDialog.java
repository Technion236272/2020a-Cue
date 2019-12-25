package com.technion.cue.BusinessFeatures;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.R;
import com.technion.cue.data_classes.Client;

import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;

public class ClientInformationDialog extends DialogFragment {

    String client_id;

    ClientInformationDialog(String client_id) {
        this.client_id = client_id;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        return inflater.inflate(R.layout.client_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView client_name = view.findViewById(R.id.client_name);
        TextView client_phone = view.findViewById(R.id.client_phone_text);
        TextView client_email = view.findViewById(R.id.client_email_text);
        FirebaseFirestore.getInstance()
                .collection(CLIENTS_COLLECTION)
                .document(this.client_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Client client = documentSnapshot.toObject(Client.class);
                    client_name.setText(client.name);
                    client_phone.setText(client.phone_number);
                    client_email.setText(client.email);
                    client_phone.setClickable(true);
                    client_email.setClickable(true);
                });

        client_email.setOnClickListener(cl -> {
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copied to clipboard", client_email.getText());
            Toast.makeText(getContext(), "copied email to clipboard", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        });

        client_phone.setOnClickListener(cl -> {
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copied to clipboard", client_phone.getText());
            Toast.makeText(getContext(), "copied phone to clipboard", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}