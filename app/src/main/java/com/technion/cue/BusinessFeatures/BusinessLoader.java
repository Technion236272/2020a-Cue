package com.technion.cue.BusinessFeatures;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;


/**
 * This class is used to load data about the business from Firestore
 */
@ModuleAuthor("Ophir Eyal")
class BusinessLoader {

    private View view;
    private FirebaseFirestore db;
    private String business_id;
    private FragmentActivity activity;

    public static Business business = null;

    BusinessLoader(View view, FirebaseFirestore db, String business_to_fetch, FragmentActivity activity) {
        this.db = db;
        this.business_id = business_to_fetch;
        this.view = view;
        this.activity = activity;
    }

    /**
     * loads data from Firebase into matching fields in the BO homepage activity
     */
    @ModuleAuthor("Ophir Eyal")
     void load() {
        if (business == null || !business.id.equals(FirebaseAuth.getInstance().getUid())) {
            db.collection(BUSINESSES_COLLECTION)
                    .document(business_id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        business = documentSnapshot.toObject(Business.class);
                        loadBusinessData();
                        loadLogoFromFireBase();
                    });
        } else {
            loadBusinessData();
            loadLogoFromFireBase();
        }

    }


    private String getLogoPath() {
        return business.logo_path.substring(business.logo_path.indexOf("business_logos"));
    }

    /**
     * loads fields from Firebase into BO object
     */
    private void loadBusinessData() {

        TextView name = view.findViewById(R.id.homepageBusinessName);
        TextView desc = view.findViewById(R.id.homepageBusinessDescription);
        name.setText(business.business_name);
        desc.setText(business.description);

        TextView location = view.findViewById(R.id.address);
        String full_address = business.location.get("address") + ", "
                + business.location.get("city") + ", "
                + business.location.get("state");
        location.setText(full_address);

        TextView phone = view.findViewById(R.id.phone);
        phone.setText(business.phone_number);

        TextView current_day_hours = view.findViewById(R.id.current_day_hours);
        Calendar c = Calendar.getInstance();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String open_hours_today = business.open_hours.get(days[c.get(Calendar.DAY_OF_WEEK) - 1]);
        String open_hours_next = null;

        for (int i = 1 ; i < 7 ; i++) {
            c.add(Calendar.DAY_OF_WEEK, 1);
            open_hours_next = business.open_hours.get(days[c.get(Calendar.DAY_OF_WEEK) - 1]);
            if (open_hours_next != null && open_hours_next.contains("-"))
                break;
            if (i == 6)
                open_hours_next = null;
        }

        if (open_hours_next == null)
            current_day_hours.setText("Now Close.");
        else if (open_hours_today == null || !open_hours_today.contains("-")) {
            current_day_hours.setText(
                    "Now Close. Opens on " +
                            days[c.get(Calendar.DAY_OF_WEEK) - 1] +
                            " at " + open_hours_next.split("-")[0]
                    );
        } else {
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
                    current_day_hours.setText("Now Open. Closes " + open_hours_today.split("-")[1]);
                else if (currentTime < open_time_millis)
                    current_day_hours.setText("Now Close. Opens " + open_hours_today.split("-")[0]);
                else
                    current_day_hours.setText("Now Close. Opens " + open_hours_next.split("-")[0]);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        ((TextView) view.findViewById(R.id.sunday))
                .setText(business.open_hours.get("Sunday"));
        ((TextView) view.findViewById(R.id.monday))
                .setText(business.open_hours.get("Monday"));
        ((TextView) view.findViewById(R.id.tuesday))
                .setText(business.open_hours.get("Tuesday"));
        ((TextView) view.findViewById(R.id.wednesday))
                .setText(business.open_hours.get("Wednesday"));
        ((TextView) view.findViewById(R.id.thursday))
                .setText(business.open_hours.get("Thursday"));
        ((TextView) view.findViewById(R.id.friday))
                .setText(business.open_hours.get("Friday"));
        ((TextView) view.findViewById(R.id.saturday))
                .setText(business.open_hours.get("Saturday"));
    }

    /**
     * loads logo from fireBase into the "business_logo" ImageView
     * uses the Glide framework for image download & processing
     */
    private void loadLogoFromFireBase()
    {
        CircularImageView logo = view.findViewById(R.id.business_logo);
        StorageReference logoRef = null;
        if (business != null && !business.logo_path.equals("")) { // ben - adding checking if null
            logoRef = FirebaseStorage.getInstance().getReference().child(getLogoPath());
        }
        Glide.with(logo.getContext())
                .load(logoRef)
                .error(R.drawable.person_icon)
                .into(logo);
    }
}
