package com.technion.cue.ClientFeatures;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.net.UnknownHostException;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

/**
 * This class is used to load data about the business from Firestore
 */
@ModuleAuthor("Ben Siso")
public class ClientBusinessLoader {

    private View view;
    private FirebaseFirestore db;
    private String business_id;
    String full_address;


    public Business business;

    public ClientBusinessLoader(FirebaseFirestore db, String business_to_fetch) {
        this.db = db;
        this.business_id = business_to_fetch;
    }

    ClientBusinessLoader(View view, FirebaseFirestore db, String business_to_fetch) {
        this(db, business_to_fetch);
        this.view = view;
    }

    ClientBusinessLoader() {
    }



    /**
         * loads data from Firebase into matching fields in the BO homepage activity
         */
    @ModuleAuthor("Ben Siso")
    void load() {
        db.collection(BUSINESSES_COLLECTION)
                .document(business_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    business = documentSnapshot.toObject(Business.class);
                    loadBusinessData();
                    loadLogoFromFireBase();
                });
    }


    private String getLogoPath() {
        return business.logo_path.substring(business.logo_path.indexOf("business_logos"));
    }

    /**
     * loads fields from Firebase into BO object
     */
    private void loadBusinessData() {

        TextView name = view.findViewById(R.id.clientBusinessTitleName);
        TextView desc = view.findViewById(R.id.clientBusinessTitleDes);
        name.setText(business.business_name);
        desc.setText(business.description);

        TextView location = view.findViewById(R.id.ClientBusinessTitleLocation);
        full_address = business.location.get("address") + ", "
                + business.location.get("city") + ", "
                + business.location.get("state");
        location.setText(full_address +"("+ business.phone_number+")");


        TextView openHours = view.findViewById(R.id.openHoursTime);
        String Sunday = business.open_hours.get("Sunday")!= "" ? business.open_hours.get("Sunday") : "Close";
        String Monday = (business.open_hours.get("Monday") != "" ? business.open_hours.get("Monday") : "Close");
        String Tuesday =(business.open_hours.get("Tuesday")!= "" ? business.open_hours.get("Tuesday") : "Close");
        String Wednesday =  (business.open_hours.get("Wednesday")!= "" ? business.open_hours.get("Wednesday") : "Close");
        String Thursday= (business.open_hours.get("Thursday")!= "" ? business.open_hours.get("Thursday") : "Close");
        String Friday =   (business.open_hours.get("Friday")!= "" ? business.open_hours.get("Friday") : "Close");
        String Saturday = (business.open_hours.get("Saturday")!= "" ? business.open_hours.get("Saturday") : "Close");

        openHours.setText(Sunday+"\n"+Monday+"\n"+Tuesday +"\n"+ Wednesday+"\n"+Thursday +"\n"+Friday +"\n"+Saturday);


    }




    /**
     * loads logo from fireBase into the "business_logo" ImageView
     * uses the Glide framework for image download & processing
     */
    private void loadLogoFromFireBase()
    {
        CircularImageView logo = view.findViewById(R.id.clientBusinessLogo);
        StorageReference logoRef = null;
        if (business != null && !business.logo_path.equals("")) { // ben - adding checking if null
            logoRef = FirebaseStorage.getInstance().getReference().child(getLogoPath());
        }
        Glide.with(logo.getContext())
                .load(logoRef)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        view.findViewById(R.id.client_business_hp_progress_bar).setVisibility(View.GONE);
                        return false;
                    }
                })
                .error(R.drawable.person_icon)
                .into(logo);

    }

    public String getPhoneNumber(){
        return business.phone_number;
    }

    public String getLocation(){
        return full_address;
    }


}
