package com.technion.cue.BusinessFeatures;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.REVIEWS_COLLECTION;

@ModuleAuthor("Ophir Eyal")
public class BOBusinessHomePage extends AppCompatActivity implements BusinessBottomMenu {

    private static final int EDIT_RESULT = 1;

    private View business_info_fragment;
    private String[] days =
            {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private Uri logoData;

    Business business;

    private RecyclerView reviews_list;
    private BOBusinessHomePage.ReviewsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bo_homepage);

        FirebaseMessaging.getInstance()
                .subscribeToTopic(FirebaseAuth.getInstance().getUid());

        business_info_fragment = findViewById(R.id.business_info);
        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        // check the homepage item in the bottom menu
        bnv.getMenu().getItem(1).setChecked(true);
        // load business data from Firebase Firestore, that will be used with the profile edit activity
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(ds -> business = ds.toObject(Business.class));

        reviews_list = findViewById(R.id.reviews_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        reviews_list.setLayoutManager(layoutManager);

        // a query to get all the 10 most recent reviews in the business'es schedule
        Query query = FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(FirebaseAuth.getInstance().getUid())
                .collection(REVIEWS_COLLECTION)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10);

        FirestoreRecyclerOptions<Business.Review> options =
                new FirestoreRecyclerOptions.Builder<Business.Review>()
                        .setQuery(query, Business.Review.class)
                        .build();

        mAdapter = new BOBusinessHomePage.ReviewsAdapter(options);
        reviews_list.setAdapter(mAdapter);

        getSupportActionBar().setElevation(0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.business_menu, menu);
        menu.getItem(0).setOnMenuItemClickListener(cl -> generateDynamicLink());

        menu.getItem(1).setOnMenuItemClickListener(cl -> {
            startActivityForResult(new Intent(this, BusinessSettings.class), EDIT_RESULT);
            return true;
        });

        // go the profile edit activity
        menu.getItem(2).setOnMenuItemClickListener(cl -> {
            Intent intent = new Intent(this, BusinessProfileEdit.class);
            intent.putExtra("business", business);
            intent.putExtra("logo", logoData);
            startActivityForResult(intent, EDIT_RESULT);
            return true;
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_RESULT) {

            if (resultCode == RESULT_OK) {
                business = (Business) data.getSerializableExtra("business");
                logoData = data.getData();
                if (logoData != null) {
                    CircularImageView logo = business_info_fragment.findViewById(R.id.business_logo);
                    Glide.with(logo.getContext())
                            .load(logoData)
                            .error(R.drawable.person_icon)
                            .into(logo);
                }

                TextView businessName = business_info_fragment.findViewById(R.id.homepageBusinessName);
                TextView businessDescription = business_info_fragment
                        .findViewById(R.id.homepageBusinessDescription);
                businessName.setText(business.business_name);
                businessDescription.setText(business.description);

                TextView location = business_info_fragment.findViewById(R.id.address);


                String full_address = business.location.get("address") + ", "
                        + business.location.get("city") + ", "
                        + business.location.get("state");
                location.setText(full_address);

                TextView phone = business_info_fragment.findViewById(R.id.phone);
                phone.setText(business.phone_number);

                TextView current_day_hours = business_info_fragment.findViewById(R.id.current_day_hours);
                Calendar c = Calendar.getInstance();

                // operation hours of the business in the current day
                String open_hours_today = business.open_hours.get(days[c.get(Calendar.DAY_OF_WEEK) - 1]);
                c.add(Calendar.DAY_OF_WEEK, 1);
                // operation hours of the business in the the following day
                String open_hours_tomorrow = business.open_hours.get(days[c.get(Calendar.DAY_OF_WEEK) - 1]);

                // if the business isn't operating today (no opening hours are found),
                // then we assume it's closed for the day
                if (!open_hours_today.contains("-"))
                    current_day_hours.setText(R.string.closed_business_message);
                else {
                    Date when_opens = null, when_closes = null;
                    try {
                        when_opens = sdf.parse(open_hours_today.split("-")[0]);
                        when_closes = sdf.parse(open_hours_today.split("-")[1]);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (when_opens == null || when_closes == null)
                        return;

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
                    long currentTime = new Date().getTime();

                    // check whether the business is currently open, and display a message accordingly
                    if (open_time_millis <= currentTime && currentTime <= close_time_millis)
                        //  business is open
                        current_day_hours
                                .setText("Now Open. Closes " + open_hours_today.split("-")[1]);
                    else if (currentTime < open_time_millis)
                        // business is closed and will reopen later on today
                        current_day_hours
                                .setText("Now Closed. Opens " + open_hours_today.split("-")[0]);
                    else
                        // business is closed and will reopen tomorrow
                        current_day_hours
                                .setText("Now Closed. Opens " + open_hours_tomorrow.split("-")[0]);
                }

                int[] res_days =
                        {R.id.sunday, R.id.monday, R.id.tuesday, R.id.wednesday,
                                R.id.thursday, R.id.friday, R.id.saturday};

                for (int i = 0; i < 7; i++) {
                    ((TextView) business_info_fragment.findViewById(res_days[i]))
                            .setText(business.open_hours.get(days[i]));
                }
            }
        }
    }

    public void openBusinessSchedule(MenuItem item) {
        Intent intent = new Intent(getBaseContext(),BusinessSchedule.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    public void openBusinessHomepage(MenuItem item) { }

    public void openBusinessClientele(MenuItem item) {
        Intent intent = new Intent(this, ClienteleList.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private boolean generateDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLink(Uri.parse("https://cueapp2.com/?name=" +
                        FirebaseAuth.getInstance().getUid()))
                .setDomainUriPrefix("https://cueapp2.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters
                                .Builder("com.technion.cue")
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(this, shortLink -> {
                    // Short link was created, and will be copied to the clipboard
                    ClipboardManager clipboard = (ClipboardManager)
                            getBaseContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("copied to clipboard",
                            shortLink.getShortLink().toString());
                    Toast.makeText(getBaseContext(), "copied link to clipboard",
                            Toast.LENGTH_SHORT).show();
                    clipboard.setPrimaryClip(clip);
                });
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    private class ReviewsAdapter extends
            FirestoreRecyclerAdapter<Business.Review, BOBusinessHomePage.ReviewsAdapter.itemHolder> {

        ReviewsAdapter(@NonNull FirestoreRecyclerOptions<Business.Review> options) {
            super(options);
        }

        @Override
        public void onDataChanged() {
            if (getItemCount() == 0)
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
        }

        @Override
        protected void onBindViewHolder(@NonNull itemHolder holder,
                                        int position,
                                        @NonNull Business.Review model) {
            holder.reviewContent.setText(model.content);
            findViewById(R.id.progress_bar).setVisibility(View.GONE);
        }

        @NonNull
        @Override
        public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reviews_row, parent,false);
            return new itemHolder(v);
        }

        private class itemHolder extends RecyclerView.ViewHolder {

            TextView reviewContent;

            itemHolder(@NonNull View itemView) {
                super(itemView);
                reviewContent = itemView.findViewById(R.id.review_content);
            }
        }
    }
}