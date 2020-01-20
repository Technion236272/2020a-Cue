package com.technion.cue.ClientFeatures;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.technion.cue.R;
import com.technion.cue.data_classes.Business;

 public class BusinessesListAdapter extends FirestoreRecyclerAdapter<Business, BusinessesListAdapter.itemHolder> {

        private ViewGroup parentView;

    BusinessesListAdapter(@NonNull FirestoreRecyclerOptions<Business> options) {
            super(options);
        }

    BusinessesListAdapter(@NonNull FirestoreRecyclerOptions<Business> options,ViewGroup parent) {
            super(options);
            this.parentView = parent;
        }

        @Override
        protected void onBindViewHolder(final @NonNull itemHolder holder,
        int position, @NonNull Business model) {
            holder.id = model.id;
            holder.business_name.setText(model.business_name);
            holder.description.setText(model.description);
            holder.location.setText(model.location.get("address") + ", "
                    + model.location.get("city") + ", "
                    + model.location.get("state"));

        }

        @NonNull
        @Override
        public itemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.client_business_list,parent,false);

            itemHolder  holder = new itemHolder(v);
            v.setOnClickListener(v1 -> {
                Intent getIntentBOPage = new Intent(parent.getContext(), ClientBusinessHomepage.class);
                // TODO: start using appointmet object !!! - refactoring later.
                getIntentBOPage.putExtra("business_id",holder.id);
                parent.getContext().startActivity(getIntentBOPage);

            });
            return holder;

        }

        @Override
        public void onDataChanged() {
            super.onDataChanged();
            if ( parentView != null) {
                parentView.findViewById(R.id.client_business_progress_bar).setVisibility(View.GONE);
                if (getItemCount() == 0) {
                    parentView.findViewById(R.id.no_businesses_message).setVisibility(View.VISIBLE);
                } else {
                    parentView.findViewById(R.id.no_businesses_message).setVisibility(View.GONE);
                }
            }
        }

        class itemHolder extends RecyclerView.ViewHolder {
            TextView business_name;
            TextView description;
            TextView location;
            String id;

            itemHolder(@NonNull View itemView) {
                super(itemView);
                business_name = itemView.findViewById(R.id.business_name);
                description = itemView.findViewById(R.id.business_description);
                location = itemView.findViewById(R.id.business_location);


            }

        }
}
