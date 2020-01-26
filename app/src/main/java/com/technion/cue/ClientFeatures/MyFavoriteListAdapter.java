package com.technion.cue.ClientFeatures;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.technion.cue.R;
import com.technion.cue.data_classes.Client;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Objects;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

/**
 * MyFavoriteListAdapter - adapter for favorite list
 *  * which load information from firebase firestore into cell using
 *  * object type(Business) that we set before.
 * */
public class MyFavoriteListAdapter extends
        FirestoreRecyclerAdapter<Client.Favorite, MyFavoriteListAdapter.itemHolder > {
    private ViewGroup parentView;

    MyFavoriteListAdapter(@NonNull FirestoreRecyclerOptions<Client.Favorite> options) {
        super(options);
    }
    /**
     * keep parent view in parentView so changing UI in parent will be possible
     * like progress bar and etc.
     * */
    MyFavoriteListAdapter(@NonNull FirestoreRecyclerOptions<Client.Favorite> options,ViewGroup parent) {
        super(options);
        this.parentView = parent;
    }
    /**
     * onBindViewHolder - load  business name and image using glide
     * from Firebase.
     * */
    @Override
    protected void onBindViewHolder(final @NonNull itemHolder holder,
                                    int position, @NonNull Client.Favorite model) {
        holder.business_id = model.business_id;
        FirebaseFirestore.getInstance()
                .collection(BUSINESSES_COLLECTION)
                .document(model.business_id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            holder.businessName.setText(document.getString("business_name"));
                            StorageReference sRef = FirebaseStorage.getInstance()
                                    .getReference()
                                    .child((Objects.requireNonNull(document.getString("logo_path"))));
                            Glide.with(holder.logo.getContext())
                                    .load(sRef)
                                    .listener(new RequestListener<Drawable>() {

                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            parentView.findViewById(R.id.client_homepage_progress_bar).setVisibility(View.GONE);
                                            return false;
                                        }

                            })
                                    .into(holder.logo);

                        }
                    }
                });

    }


    @NonNull
    @Override
    public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.my_favorite_list_adapter,parent,false);

        itemHolder  holder = new itemHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntentBOPage = new Intent(parent.getContext(), ClientBusinessHomepage.class);
                // TODO: start using appointmet object !!! - refactoring later.
                getIntentBOPage.putExtra("business_id",holder.business_id);
                getIntentBOPage.putExtra("favorite","true");
                parent.getContext().startActivity(getIntentBOPage);

            }
        });
        return holder;

    }
    /**
     * onDataChanged - abort progress bar and if not favorties then show
     * "No Favorites yet."
     * */
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        if ( parentView != null) {
            if (getItemCount() == 0) {
                parentView.findViewById(R.id.client_homepage_progress_bar).setVisibility(View.GONE);
                parentView.findViewById(R.id.client_no_appointments_message_fav).setVisibility(View.VISIBLE);
                parentView.findViewById(R.id.client_homepage_progress_bar).setVisibility(View.GONE);
            } else {
                parentView.findViewById(R.id.client_no_appointments_message_fav).setVisibility(View.GONE);
            }
        }
    }

    class itemHolder extends RecyclerView.ViewHolder {
        TextView businessName;
        String business_id;
        CircularImageView logo;

        itemHolder(@NonNull View itemView) {
            super(itemView);
            businessName = itemView.findViewById(R.id.businessName);
            logo = itemView.findViewById(R.id.businessLogo);
            business_id = "";
        }

    }


}
