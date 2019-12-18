package com.technion.cue.ClientFeatures;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.technion.cue.BusinessFeatures.BusinessInfoFragment;
import com.technion.cue.FirebaseCollections;
import com.technion.cue.R;
import com.technion.cue.data_classes.Appointment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

public class ClientChooseTimeFragment extends Fragment {

    private Date date = null;
    private String business_id = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.activity_client_choose_time_fragment, container, false);

        Calendar mcurrentDate = Calendar.getInstance();
        int mHour = mcurrentDate.get(Calendar.HOUR_OF_DAY);
        int mMinute = mcurrentDate.get(Calendar.MINUTE);
        TimePickerDialog tpd = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            Bundle b = getArguments();
            business_id = b.getString("business_id");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date date_0 = sdf.parse(b.getInt("day") + "/" +
                        (b.getInt("month") + 1) + "/" + b.getInt("year"));
                Calendar c = Calendar.getInstance();
                c.setTime(date_0);
                c.add(Calendar.HOUR_OF_DAY, hourOfDay);
                c.add(Calendar.MINUTE, minute);
                date = c.getTime();

                FirebaseFirestore.getInstance()
                        .collection(APPOINTMENTS_COLLECTION)
                        .whereEqualTo("business_id", business_id)
                        .whereLessThanOrEqualTo("date", date)
                        .orderBy("date", Query.Direction.DESCENDING)
                        .limit(1)
                        .get().addOnSuccessListener(l -> {
                    if (l.isEmpty()) {
                        addAppointmentToBusiness();
                        switchBackToBusinessInfo();
                    } else {
                        FirebaseFirestore.getInstance()
                                .collection(BUSINESSES_COLLECTION)
                                .document(business_id)
                                .collection(TYPES_COLLECTION)
                                .whereEqualTo("name", "type_0") // TODO: will change to a chosen type in Sprint #2
                                .limit(1)
                                .get()
                                .addOnSuccessListener(l_a -> {
                                    if (l_a.isEmpty()) {
                                        addAppointmentToBusiness();
                                        switchBackToBusinessInfo();
                                    } else {
                                        Map<String, String> attributes =
                                                (Map<String, String>) l_a.getDocuments()
                                                        .get(0)
                                                        .get("attributes");
                                        if (attributes.isEmpty()) {
                                            addAppointmentToBusiness();
                                            switchBackToBusinessInfo();
                                        } else {
                                            String type_duration = attributes.get("duration");
                                            Appointment appointment = l.getDocuments().get(0).toObject(Appointment.class);
                                            Calendar cl = Calendar.getInstance();
                                            cl.setTime(appointment.date);
                                            cl.add(Calendar.MINUTE, Integer.parseInt(type_duration));
                                            Date previous_appointment_end = cl.getTime();
                                            if (previous_appointment_end.before(date)) {
                                                addAppointmentToBusiness();
                                                switchBackToBusinessInfo();
                                            } else {
                                                Toast.makeText(getContext(),
                                                        "time slot is unavailable. please choose a different time",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }

                                    }
                                });
                    }
                });
            } catch (ParseException e) {
                Toast.makeText(getContext(), "wrong format!", Toast.LENGTH_SHORT).show();
            }
        }, mHour, mMinute, true);
        tpd.setTitle("Select a time");
        tpd.show();
        return rootView;
    }

    private void switchBackToBusinessInfo() {
        Bundle b = getArguments();
        Fragment f = new BusinessInfoFragment();
        f.setArguments(b);
        getActivity().findViewById(R.id.switch_to_date_time_fragments).setVisibility(View.VISIBLE);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_holder_business_client, f)
                .commit();
    }

    private void addAppointmentToBusiness() {
        // TODO: will change to a chosen type in Sprint #2
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(business_id)
                .collection(TYPES_COLLECTION)
                .whereEqualTo("name", "type_0")
                .get()
                .addOnSuccessListener(l -> {
                    Appointment appointment = new Appointment(business_id,
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            l.getDocuments().get(0).getId(), date);
                    FirebaseFirestore.getInstance()
                            .collection(APPOINTMENTS_COLLECTION)
                            .document()
                            .set(appointment);
                });
    }
}
