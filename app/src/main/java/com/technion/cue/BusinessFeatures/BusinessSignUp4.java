package com.technion.cue.BusinessFeatures;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.technion.cue.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The fourth business sign-up screen
 * Used for inserting the activity times of the business
 */
public class BusinessSignUp4 extends Fragment {

    private final BusinessSignUpContainer businessSignUpContainer;

    BusinessSignUp4(BusinessSignUpContainer businessSignUpContainer) {
        this.businessSignUpContainer = businessSignUpContainer;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fourth_business_sign_up_fragment,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView daysList = view.findViewById(R.id.schedule_days_sign_up);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        daysList.setLayoutManager(layoutManager);

        daysList.setHasFixedSize(true);

        daysList.setAdapter(new OpenDaysAdapter());

        businessSignUpContainer.done_btn = view.findViewById(R.id.sign_up_finish_button);

        businessSignUpContainer.done_btn
                .setOnClickListener(v -> businessSignUpContainer.onFinished());
    }

    class OpenDaysAdapter extends RecyclerView.Adapter<OpenDaysAdapter.DayHolder> {

        private String[] days =
                {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        class DayHolder extends RecyclerView.ViewHolder {
            String dayText;
            TextView day;
            TextView openHours;
            TextView closeHours;

            DayHolder(@NonNull View itemView) {
                super(itemView);
                openHours = itemView.findViewById(R.id.openingHoursEditText);
                closeHours = itemView.findViewById(R.id.closingHoursEditText);
                day = itemView.findViewById(R.id.sign_up_day);

                for (String day : days) {
                    if (!businessSignUpContainer.open_hours.containsKey(day))
                        businessSignUpContainer.open_hours.put(day, "");
                }

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
                                if (businessSignUpContainer.open_hours.get(dayText).split("-").length == 2) {
                                    businessSignUpContainer.open_hours.put(dayText, value_to_put + "-" +
                                            businessSignUpContainer.open_hours
                                                    .get(dayText).split("-")[1]);
                                } else {
                                    businessSignUpContainer.open_hours.put(dayText, value_to_put + "-");
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
                                if (businessSignUpContainer.open_hours.get(dayText).contains("-")) {
                                    businessSignUpContainer.open_hours.put(dayText,
                                            businessSignUpContainer.open_hours.get(dayText)
                                                    .split("-")[0] + "-" + value_to_put);
                                } else {
                                    businessSignUpContainer.open_hours
                                            .put(dayText, "-" + value_to_put);
                                }
                            }, hour, minute, true);
                    mTimePicker.setTitle("Select closing hour");
                    mTimePicker.show();
                });
            }
        }

        @NonNull
        @Override
        public OpenDaysAdapter.DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DayHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sign_up_day_holder, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull OpenDaysAdapter.DayHolder holder, int position) {
            holder.day.setText(days[position]);
            holder.dayText = days[position];

            String set_hours = businessSignUpContainer.open_hours.get(days[position]);
            if (set_hours.contains("-")) {
                if (set_hours.split("-").length == 2) {
                    holder.openHours.setText(set_hours.split("-")[0]);
                    holder.closeHours.setText(set_hours.split("-")[1]);
                } else if (set_hours.split("-")[0].contains(":")) {
                    holder.openHours.setText(set_hours.split("-")[0]);
                } else {
                    holder.closeHours.setText(set_hours.split("-")[1]);
                }
            } else {
                holder.openHours.setText("");
                holder.closeHours.setText("");
            }


        }

        @Override
        public int getItemCount() {
            return 7;
        }
    }

}
