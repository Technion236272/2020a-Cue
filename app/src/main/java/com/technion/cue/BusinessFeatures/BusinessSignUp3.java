package com.technion.cue.BusinessFeatures;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.util.HashMap;
import java.util.Map;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

@ModuleAuthor("Topaz & Ophir")
public class BusinessSignUp3 extends Fragment {

    private final BusinessSignUpContainer businessSignUpContainer;

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

        businessSignUpContainer.types_list = view.findViewById(R.id.types_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        businessSignUpContainer.types_list.setLayoutManager(layoutManager);

        businessSignUpContainer.types_list.setHasFixedSize(true);

        businessSignUpContainer.types_adapter = new AppointmentTypesListAdapter();
        businessSignUpContainer.types_adapter.setHasStableIds(true);

        businessSignUpContainer.types_list.setAdapter(businessSignUpContainer.types_adapter);

        ImageButton another_appointment_button = view.findViewById(R.id.add_another_type_button);
        another_appointment_button.setOnClickListener(cl -> {
            businessSignUpContainer.num_of_types++;
            new TypesDialog(businessSignUpContainer).show(getFragmentManager(), null);
        });
    }

    class AppointmentTypesListAdapter
            extends RecyclerView.Adapter<AppointmentTypesListAdapter.TypeHolder> {

        @NonNull
        @Override
        public TypeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new TypeHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sign_up_type_holder, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull TypeHolder holder, int position) {
            if (businessSignUpContainer.types_fields.size() > position) {
                holder.type_name.setText(businessSignUpContainer.types_fields
                        .get(position).get("name"));
            }
        }

        @Override
        public int getItemCount() {
            return businessSignUpContainer.num_of_types;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class TypeHolder extends RecyclerView.ViewHolder {
            TextView type_name;

            TypeHolder(@NonNull View itemView) {
                super(itemView);
                type_name = itemView.findViewById(R.id.sign_up_type_name);
            }
        }
    }
}


