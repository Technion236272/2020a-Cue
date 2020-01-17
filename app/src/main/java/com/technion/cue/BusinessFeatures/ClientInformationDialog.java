package com.technion.cue.BusinessFeatures;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.ClientFeatures.EditAppointmentActivity;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Client;

import org.w3c.dom.Text;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;

/**
 * A dialog displaying additional details about a certain client
 */
@ModuleAuthor("Ophir Eyal")
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
        ExtendedFloatingActionButton manual_schedule = view.findViewById(R.id.manual_schedule);
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
                    noShowClarify(view, client.name);
                });



        // clicking on the displayed email copied it to the clipboard
        client_email.setOnClickListener(cl -> {
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copied to clipboard", client_email.getText());
            Toast.makeText(getContext(), "copied email to clipboard", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        });

        // same as above, but for the phone number
        client_phone.setOnClickListener(cl -> {
            ClipboardManager clipboard = (ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copied to clipboard", client_phone.getText());
            Toast.makeText(getContext(), "copied phone to clipboard", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        });

        manual_schedule.setOnClickListener(cl -> {
            Bundle bundle = new Bundle();
            bundle.putString("business_id", FirebaseAuth.getInstance().getUid());
            bundle.putString("client_name", client_id);
            Intent intent = new Intent(getContext(), EditAppointmentActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
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

    private void noShowClarify(View view, String client_name) {
        FirebaseFirestore.getInstance()
                .collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("business_id", FirebaseAuth.getInstance().getUid())
                .whereEqualTo("client_id", client_id)
                .get()
                .addOnSuccessListener(documentSnapshots -> {
                    double size = documentSnapshots.size();
                    double no_show_num = 0;
                    for (DocumentSnapshot ds : documentSnapshots) {
                        if (ds.contains("no_show") && ds.getBoolean("no_show")) {
                            no_show_num++;
                        }
                    }
                    if (size > 0 && no_show_num >= ((1.0/3.0) * size)) {
                        view.findViewById(R.id.no_show_clarify).setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.no_show_clarify))
                                .setText(client_name + " hasn't shown / was late to "
                                        + (int) no_show_num + " out of "
                                        + (int) size + " appointments");
                    }
                });
    }
}