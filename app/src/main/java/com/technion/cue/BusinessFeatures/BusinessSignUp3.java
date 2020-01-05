package com.technion.cue.BusinessFeatures;

import android.app.TimePickerDialog;
import java.text.ParseException;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseUser;
import com.technion.cue.R;
import com.technion.cue.SignInActivity;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.util.Calendar;
import java.util.Date;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

@ModuleAuthor("Topaz & Ophir")
public class BusinessSignUp3 extends Fragment {
    private final BusinessSignUpContainer businessSignUpContainer;
    private String[] days =
            {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private String lastUsedKey;

    BusinessSignUp3(BusinessSignUpContainer businessSignUpContainer) {
        this.businessSignUpContainer = businessSignUpContainer;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.third_business_sign_up_fragment,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        for (String day : days) {
            businessSignUpContainer.open_hours.put(day, "");
        }

        AutoCompleteTextView openHoursDays = view.findViewById(R.id.days_filled_exposed_dropdown);
        openHoursDays.setAdapter(new ArrayAdapter<>(getActivity(),
                R.layout.dropdown_menu_popup_item, days));

        TextView openHours = view.findViewById(R.id.openingHoursEditText);
        TextView closeHours = view.findViewById(R.id.closingHoursEditText);
        openHours.setClickable(false);
        closeHours.setClickable(false);

        openHoursDays.setOnItemClickListener((parent, v, position, id) -> {

            // we use the map open_hours in order to store the operating hours of the business
            // we check if the chosen day's operating hours are already stored in the map
            // if they aren't, we store them. Regardless, we set the opening hour and closing hour
            // text fields content according to this day's stored values.
            String key = parent.getItemAtPosition(position).toString();
            if (businessSignUpContainer.open_hours.containsKey(key)) {
                String open_hour = "", close_hour = "";
                String hours = businessSignUpContainer.open_hours.get(key);
                if (hours.contains("-")) {
                    open_hour = hours.split("-")[0];
                    close_hour = hours.split("-")[1];
                }
                openHours.setText(open_hour);
                closeHours.setText(close_hour);
                lastUsedKey = key;
            }

            openHours.setClickable(true);
            closeHours.setClickable(true);

        });

        // setting the opening hours of the business. we use a TimePickerDialog to let
        // the business owner to choose a new opening time, which will be displayed in the
        // opening hour text field.
        // Additionally, we make sure that the chosen opening time precedes the chosen closing time.
        openHours.setOnClickListener(cl -> {
            int hour, minute;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                Date d = sdf.parse(openHours.getText().toString());
                c.setTime(d);
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            } catch (ParseException e) {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }

            TimePickerDialog mTimePicker = new TimePickerDialog(getActivity(),
                    (timePicker, selectedHour, selectedMinute) -> {

                        String closeHoursText = closeHours.getText().toString();
                        if (closeHoursText.contains(":")) {
                            String closeHour = closeHoursText.split(":")[0];
                            String closeMinute = closeHoursText.split(":")[1];
                            if ((selectedHour > Integer.valueOf(closeHour)) ||
                                    (selectedHour == Integer.valueOf(closeHour)
                                            && selectedMinute >= Integer.valueOf(closeMinute))) {
                                Toast.makeText(getActivity(),
                                        "Opening time must precede closing time. Please refill the form.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        String selectedMinuteFormatted =
                                selectedMinute == 0 ? "00" : String.valueOf(selectedMinute);
                        String selectedHourFormatted =
                                selectedHour <= 9 ? "0" + selectedHour : String.valueOf(selectedHour);
                        String value_to_put = selectedHourFormatted + ":" + selectedMinuteFormatted;

                        openHours.setText(value_to_put);
                        if (lastUsedKey == null)
                            return;
                        if (businessSignUpContainer.open_hours.get(lastUsedKey).contains("-")) {
                            businessSignUpContainer.open_hours.put(lastUsedKey, value_to_put + "-" +
                                    businessSignUpContainer.open_hours
                                            .get(lastUsedKey).split("-")[1]);
                        } else {
                            businessSignUpContainer.open_hours.put(lastUsedKey, value_to_put + "-");
                        }
                    }, hour, minute, true);
            mTimePicker.setTitle("Select opening hour");
            mTimePicker.show();
        });

        // same as above, but for the closing time
        closeHours.setOnClickListener(cl -> {
            int hour, minute;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            try {
                Date d = sdf.parse(openHours.getText().toString());
                c.setTime(d);
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            } catch (ParseException e) {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }

            TimePickerDialog mTimePicker = new TimePickerDialog(getActivity(),
                    (timePicker, selectedHour, selectedMinute) -> {

                        String openHoursText = openHours.getText().toString();
                        if (openHoursText.contains(":")) {
                            String openHour = openHoursText.split(":")[0];
                            String openMinute = openHoursText.split(":")[1];
                            if ((selectedHour < Integer.valueOf(openHour)) ||
                                    (selectedHour == Integer.valueOf(openHour)
                                            && selectedMinute <= Integer.valueOf(openMinute))) {
                                Toast.makeText(getActivity(),
                                        "Opening time must precede closing time. Please refill the form.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        String selectedMinuteFormatted =
                                selectedMinute == 0 ? "00" : String.valueOf(selectedMinute);
                        String selectedHourFormatted =
                                selectedHour <= 9 ? "0" + selectedHour : String.valueOf(selectedHour);
                        String value_to_put = selectedHourFormatted + ":" + selectedMinuteFormatted;
                        closeHours.setText(value_to_put);
                        if (lastUsedKey == null)
                            return;
                        if (businessSignUpContainer.open_hours.get(lastUsedKey).contains("-")) {
                            businessSignUpContainer.open_hours.put(lastUsedKey,
                                    businessSignUpContainer.open_hours.get(lastUsedKey)
                                            .split("-")[0] + "-" + value_to_put);
                        } else {
                            businessSignUpContainer.open_hours
                                    .put(lastUsedKey, "-" + value_to_put);
                        }
                    }, hour, minute, true);
            mTimePicker.setTitle("Select closing hour");
            mTimePicker.show();
        });

        businessSignUpContainer.done_btn = view.findViewById(R.id.sign_up_finish_button);

        businessSignUpContainer.done_btn
                .setOnClickListener(v -> businessSignUpContainer.onFinished());
    }
}


