package com.technion.cue.BusinessFeatures;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BOBusinessHomePage extends AppCompatActivity {

    private static final int EDIT = 1;

    public void openBusinessCalendar(View view) {
        final Intent intent = new Intent(getBaseContext(),BusinessSchedule.class);
        startActivity(intent);
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser() ;
    private BusinessLoader loader;

    private View fragment_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bohome_page);
        fragment_view = findViewById(R.id.business_info);
        loader = new BusinessLoader(fragment_view, db, currentUser.getUid());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT) {
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(data.getData());
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        CircularImageView logo = fragment_view.findViewById(R.id.business_logo);
                        logo.setImageBitmap(selectedImage);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                TextView businessName =
                        fragment_view.findViewById(R.id.homepageBusinessName);
                TextView businessDescription =
                        fragment_view.findViewById(R.id.homepageBusinessDescription);
                businessName.setText(data.getStringExtra("businessName"));
                businessDescription.setText(data.getStringExtra("businessDescription"));

                TextView location = fragment_view.findViewById(R.id.address_text);
                String full_address = data.getStringExtra("address") + ", "
                        + data.getStringExtra("city") + ", "
                        + data.getStringExtra("state");
                location.setText(full_address);
                TextView phone = fragment_view.findViewById(R.id.phone_text);
                phone.setText(data.getStringExtra("phone"));

                TextView current_day_hours = fragment_view.findViewById(R.id.current_day_hours);
                Calendar c = Calendar.getInstance();
                String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                String open_hours_today = data.getStringExtra(days[c.get(Calendar.DAY_OF_WEEK) - 1]);
                c.add(Calendar.DAY_OF_WEEK, 1);

                String open_hours_tomorrow = data.getStringExtra(days[c.get(Calendar.DAY_OF_WEEK) - 1]);

                // TODO: create code to handle cases where this assertion doesn't hold
                assert open_hours_tomorrow.contains("-");

                if (!open_hours_today.contains("-"))
                    current_day_hours.setText("Close.");
                else {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        Date when_opens = sdf.parse(open_hours_today.split("-")[0]);
                        Date when_closes = sdf.parse(open_hours_today.split("-")[1]);
                        Date currentDate = new Date();
                        Calendar currentTimeCalendar = Calendar.getInstance();
                        Calendar calendarCloseHour = Calendar.getInstance();
                        Calendar calendarOpenHour = Calendar.getInstance();
                        calendarOpenHour.setTime(when_opens);
                        calendarOpenHour.set(currentTimeCalendar.get(Calendar.YEAR),
                                currentTimeCalendar.get(Calendar.MONTH),
                                currentTimeCalendar.get(Calendar.DAY_OF_MONTH));
                        calendarCloseHour.setTime(when_closes);
                        calendarCloseHour.set(currentTimeCalendar.get(Calendar.YEAR),
                                currentTimeCalendar.get(Calendar.MONTH),
                                currentTimeCalendar.get(Calendar.DAY_OF_MONTH));
                        long open_time_millis = calendarOpenHour.getTimeInMillis();
                        long close_time_millis = calendarCloseHour.getTimeInMillis();
                        long currentTime = currentDate.getTime();

                        if (open_time_millis <= currentTime && currentTime <= close_time_millis)
                            current_day_hours
                                    .setText("Open. Closes " + open_hours_today.split("-")[1]);
                        else if (currentTime < open_time_millis)
                            current_day_hours
                                    .setText("Closeed. Opens " + open_hours_today.split("-")[0]);
                        else
                            current_day_hours
                                    .setText("Closed. Opens " + open_hours_tomorrow.split("-")[0]);

                        ((TextView) fragment_view.findViewById(R.id.sunday))
                                .setText(data.getStringExtra("Sunday"));
                        ((TextView) fragment_view.findViewById(R.id.monday))
                                .setText(data.getStringExtra("Monday"));
                        ((TextView) fragment_view.findViewById(R.id.tuesday))
                                .setText(data.getStringExtra("Tuesday"));
                        ((TextView) fragment_view.findViewById(R.id.wednesday))
                                .setText(data.getStringExtra("Wednesday"));
                        ((TextView) fragment_view.findViewById(R.id.thursday))
                                .setText(data.getStringExtra("Thursday"));
                        ((TextView) fragment_view.findViewById(R.id.friday))
                                .setText(data.getStringExtra("Friday"));
                        ((TextView) fragment_view.findViewById(R.id.saturday))
                                .setText(data.getStringExtra("Saturday"));


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * onClick method for the edit button.
     * change BOBusinessHomePage into / from "edit mode",
     * where the BO will able to change the profile of his business
     */
    @ModuleAuthor("Ophir Eyal")
    public void editBOHomePage(View view) {

        Bundle bundle = new Bundle();
        // TODO: add data to bundle
        Intent i = new Intent(this, BusinessProfileEdit.class);
        i.putExtras(bundle);
        startActivityForResult(i, EDIT);

    }


    public void openClientele(View view) {
        Intent i = new Intent(this, ClienteleList.class);
        startActivity(i);
    }
}

