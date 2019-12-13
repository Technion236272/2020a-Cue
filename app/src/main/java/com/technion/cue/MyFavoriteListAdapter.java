package com.technion.cue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.technion.cue.data_classes.Client;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Objects;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

public class MyFavoriteListAdapter extends
        FirestoreRecyclerAdapter<Client.Favorite, MyFavoriteListAdapter.itemHolder > {

    MyFavoriteListAdapter(@NonNull FirestoreRecyclerOptions<Client.Favorite> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(final @NonNull itemHolder holder,
                                    int position, @NonNull Client.Favorite model) {
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
        return new itemHolder(v);
    }

    class itemHolder extends RecyclerView.ViewHolder {
        TextView businessName;
        CircularImageView logo;

        itemHolder(@NonNull View itemView) {
            super(itemView);
            businessName = itemView.findViewById(R.id.businessName);
            logo = itemView.findViewById(R.id.businessLogo);
        }
    }
}
