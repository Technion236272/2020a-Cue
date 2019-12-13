package com.technion.cue;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;


public class MyFavoriteListAdapter extends FirestoreRecyclerAdapter<BFavoriteListItem,MyFavoriteListAdapter.itemHolder > {

    private FirebaseFirestore db;


    public MyFavoriteListAdapter(@NonNull FirestoreRecyclerOptions<BFavoriteListItem> options) {
        super(options);

    }

    @Override
    protected void onBindViewHolder(final @NonNull itemHolder holder, int position, @NonNull BFavoriteListItem model) {
//        holder.num_item.setText(model.getNum_item());
//
//        if (model.getLogo_path().toString().length() > 0) {
//            holder.logo.setImageResource(model.getLogo_path());
//        }
        db = FirebaseFirestore.getInstance();
        System.out.println("-----------   -------------------  " +  model.getB_id() + "-----------   -------------------  ");
        DocumentReference docRef = db.collection("Businesses").document(model.getB_id());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        holder.businessName.setText(document.getString("business_name"));
                        StorageReference sRef = FirebaseStorage.getInstance().getReference().child((document.getString("logo_path")));
                        Glide.with(holder.logo.getContext())
                                .load(sRef)
                                .into(holder.logo);
                    }
                }
            }
        });


    }

    @NonNull
    @Override
    public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.my_favorite_list_adapter,parent,false);
        return new itemHolder(v);
    }

    class itemHolder extends RecyclerView.ViewHolder {
        TextView businessName;
        CircleImageView logo;


        public itemHolder(@NonNull View itemView) {
            super(itemView);
            businessName = itemView.findViewById(R.id.businessName);
            logo = itemView.findViewById(R.id.businessLogo);

        }
    }
}
