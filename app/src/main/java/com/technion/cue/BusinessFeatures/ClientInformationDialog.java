package com.technion.cue.BusinessFeatures;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.technion.cue.ClientFeatures.EditAppointmentActivity;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;
import com.technion.cue.data_classes.Client;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTELE_COLLECTION;
import static com.technion.cue.FirebaseCollections.CLIENTS_COLLECTION;

/**
 * A dialog displaying additional details about a certain client
 */
@ModuleAuthor("Ophir Eyal")
public class ClientInformationDialog extends DialogFragment {

    Business.ClienteleMember cm;
    boolean recentlyChanged = true;

    ClientInformationDialog(Business.ClienteleMember cm) {
        this.cm = cm;
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

        AtomicBoolean blocked = new AtomicBoolean(false);

        TextView client_name = view.findViewById(R.id.client_name);
        TextView client_phone = view.findViewById(R.id.client_phone);
        TextView client_email = view.findViewById(R.id.client_email);
        ExtendedFloatingActionButton manual_schedule = view.findViewById(R.id.manual_schedule);
        FirebaseFirestore.getInstance()
                .collection(CLIENTS_COLLECTION)
                .document(cm.client_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Client client = documentSnapshot.toObject(Client.class);
                    client_name.setText(client.name);
                    client_phone.setText(client.phone_number);
                    client_email.setText(client.email);
                    client_phone.setClickable(true);
                    client_email.setClickable(true);
                    noShowClarify(view, client.name);
                    FirebaseFirestore.getInstance()
                            .collection(BUSINESSES_COLLECTION)
                            .document(FirebaseAuth.getInstance().getUid())
                            .collection(CLIENTELE_COLLECTION)
                            .document(cm.id)
                            .get()
                            .addOnSuccessListener(ds -> {
                                if (ds.exists() && ds.contains("blocked") && ds.getBoolean("blocked")) {
                                    blocked.set(true);
                                    recentlyChanged = true;
                                    ((CheckBox)view.findViewById(R.id.block)).setChecked(true);
                                    recentlyChanged = false;
                                } else {
                                    recentlyChanged = true;
                                    blocked.set(false);
                                    ((CheckBox)view.findViewById(R.id.block)).setChecked(false);
                                    recentlyChanged = false;
                                }
                            });
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
           if (blocked.get())
               Toast.makeText(getContext(),
                       "This client is blocked, so you can't schedule appointments with him/her",
                       Toast.LENGTH_SHORT).show();
           else {
               Bundle bundle = new Bundle();
               bundle.putString("business_id", FirebaseAuth.getInstance().getUid());
               bundle.putString("client_name", cm.client_id);
               Intent intent = new Intent(getContext(), EditAppointmentActivity.class);
               intent.putExtras(bundle);
               startActivity(intent);
           }
        });


        ((CheckBox)view.findViewById(R.id.block)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (recentlyChanged) {
                recentlyChanged = false;
                return;
            }
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
            androidx.appcompat.app.AlertDialog ad = builder.setMessage("Are you sure")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        if (isChecked) {
                            FirebaseFirestore.getInstance()
                                    .collection(APPOINTMENTS_COLLECTION)
                                    .whereEqualTo("business_id", FirebaseAuth.getInstance().getUid())
                                    .whereEqualTo("client_id", cm.client_id)
                                    .get()
                                    .addOnSuccessListener(ds -> {
                                        if (ds.isEmpty()) {
                                            FirebaseFirestore.getInstance()
                                                    .collection(BUSINESSES_COLLECTION)
                                                    .document(FirebaseAuth.getInstance().getUid())
                                                    .collection(CLIENTELE_COLLECTION)
                                                    .document(cm.id)
                                                    .set(new Business.ClienteleMember(cm.client_id, cm.name, true));
                                            blocked.set(true);
                                        }

                                        else {
                                            Toast.makeText(getContext(),
                                                    "You can block a client only if he has no scheduled appointments in your business",
                                                    Toast.LENGTH_SHORT).show();
                                            recentlyChanged = true;
                                            ((CheckBox)view.findViewById(R.id.block)).setChecked(false);
                                        }
                                    });
                        } else {
                            FirebaseFirestore.getInstance()
                                    .collection(BUSINESSES_COLLECTION)
                                    .document(FirebaseAuth.getInstance().getUid())
                                    .collection(CLIENTELE_COLLECTION)
                                    .document(cm.id)
                                    .set(new Business.ClienteleMember(cm.client_id, cm.name, false));
                            blocked.set(false);
                        }

                    }).setNegativeButton("Deny", (dialog, which) -> {
                        recentlyChanged = true;
                        ((CheckBox)view.findViewById(R.id.block)).setChecked(!isChecked);
                    }).create();
            ad.show();
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
                .whereEqualTo("client_id", cm.client_id)
                .get()
                .addOnSuccessListener(documentSnapshots -> {
                    double size = 0;
                    double no_show_num = 0;
                    for (DocumentSnapshot ds : documentSnapshots) {
                        if (ds.getTimestamp("date").toDate().getTime() <= System.currentTimeMillis()) {
                            size++;
                            if (ds.contains("no_show") && ds.getBoolean("no_show"))
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