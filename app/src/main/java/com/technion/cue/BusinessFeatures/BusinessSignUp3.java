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

//        businessSignUpContainer.types_list = view.findViewById(R.id.types_list);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
//        businessSignUpContainer.types_list.setLayoutManager(layoutManager);
//
//        businessSignUpContainer.types_list.setHasFixedSize(true);

//        businessSignUpContainer.types_adapter = new AppointmentTypesListAdapter();
//        businessSignUpContainer.types_adapter.setHasStableIds(true);

//        businessSignUpContainer.types_list.setAdapter(businessSignUpContainer.types_adapter);

        ImageButton another_appointment_button = view.findViewById(R.id.add_another_type_button);
        another_appointment_button.setOnClickListener(cl -> {
            businessSignUpContainer.num_of_types++;
            Fragment types = new typesFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_setting, types)
                    .addToBackStack(null)
                    .commit();

//                final Intent intent = new Intent(MySettingsFragment.super.getContext(),AppoitmentTypes.class);
//                startActivity(intent);
//            businessSignUpContainer.types_adapter.notifyDataSetChanged();
        });
    }

    public static class typesFragment extends Fragment {
        public FirebaseFirestore db;
        public FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.types_fragment, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);


            Button add_button = view.findViewById(R.id.button_add_type);
            RecyclerView types = view.findViewById(R.id.types_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            types.setLayoutManager(layoutManager);
            types.setHasFixedSize(true);

            Query query = FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION)
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection(TYPES_COLLECTION)
                    .orderBy("name");

            FirestoreRecyclerOptions<Business.AppointmentType> options =
                    new FirestoreRecyclerOptions.Builder<Business.AppointmentType>()
                            .setQuery(query, Business.AppointmentType.class)
                            .build();

            AppointmentTypesListAdapter tAdapter = new AppointmentTypesListAdapter(options);
            types.setAdapter(tAdapter);

            add_button.setOnClickListener(v->{
                Fragment new_types = new newTypesFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.types_fragment, new_types)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    public static class newTypesFragment extends Fragment{
        public FirebaseFirestore db;
        public FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.new_appointment_type, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Button done = view.findViewById(R.id.button_done);
            done.setOnClickListener(l->{
                boolean isActive = ((Switch) view.findViewById(R.id.active_type)).isChecked();
                TextInputEditText type_name = view.findViewById(R.id.bTypeNameEdit);
                TextInputEditText duration = view.findViewById(R.id.bTypeDurationEdit);
                boolean isChecked1 = ((CheckBox) view.findViewById(R.id.type_sunday)).isChecked();
                boolean isChecked2 = ((CheckBox) view.findViewById(R.id.type_monday)).isChecked();
                boolean isChecked3 = ((CheckBox) view.findViewById(R.id.type_tuesday)).isChecked();
                boolean isChecked4 = ((CheckBox) view.findViewById(R.id.type_wednesday)).isChecked();
                boolean isChecked5 = ((CheckBox) view.findViewById(R.id.type_thursday)).isChecked();
                boolean isChecked6 = ((CheckBox) view.findViewById(R.id.type_friday)).isChecked();
                boolean isChecked7 = ((CheckBox) view.findViewById(R.id.type_saturday)).isChecked();
                TextInputEditText notes = view.findViewById(R.id.bTypeNotesEdit);


                Map<String, String> attributes = new HashMap<>();
                attributes.put("active", String.valueOf(isActive));
                attributes.put("duration", duration.getText().toString());
                attributes.put("sunday",String.valueOf(isChecked1));
                attributes.put("monday",String.valueOf(isChecked2));
                attributes.put("tuesday",String.valueOf(isChecked3));
                attributes.put("wednesday",String.valueOf(isChecked4));
                attributes.put("thursday",String.valueOf(isChecked5));
                attributes.put("friday",String.valueOf(isChecked6));
                attributes.put("saturday",String.valueOf(isChecked7));
                attributes.put("notes",notes.getText().toString());
                Business.AppointmentType type = new Business.AppointmentType(type_name.getText().toString(),attributes);

                FirebaseFirestore.getInstance()
                        .collection(BUSINESSES_COLLECTION+"/"+FirebaseAuth.getInstance().getUid()
                                +"/Types").document().set(type);
            });

        }
    }

//    class AppointmentTypesListAdapter
//            extends RecyclerView.Adapter<AppointmentTypesListAdapter.TypeHolder> {
//
//        @NonNull
//        @Override
//        public TypeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            return new TypeHolder(LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.sign_up_type_holder, parent, false));
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull TypeHolder holder, int position) {
//            if (businessSignUpContainer.types_fields.size() > position) {
//                holder.type_text.setText(businessSignUpContainer.types_fields.get(position).get("name"));
//                holder.duration.setText(businessSignUpContainer.types_fields.get(position).get("duration"));
//                holder.notes_text.setText(businessSignUpContainer.types_fields.get(position).get("notes"));
//                if (businessSignUpContainer.types_fields.get(position).containsKey("isAvailableSunday")) {
//                    holder.sunday.setChecked(true);
//                }
//                // for all of them
//            } else {
//                businessSignUpContainer.types_fields.add(new HashMap<>());
//            }
//
//            holder.type_text.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    Map<String, String> fields = businessSignUpContainer.types_fields.get(position);
//                    fields.put("name", s.toString());
//                    businessSignUpContainer.types_fields.set(position, fields);
//                }
//            });
//
//            holder.duration.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    Map<String, String> fields = businessSignUpContainer.types_fields.get(position);
//                    fields.put("duration", s.toString());
//                    businessSignUpContainer.types_fields.set(position, fields);
//                }
//            });
//
//            holder.notes_text.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    Map<String, String> fields = businessSignUpContainer.types_fields.get(position);
//                    fields.put("notes", s.toString());
//                    businessSignUpContainer.types_fields.set(position, fields);
//                }
//            });
//
//            holder.sunday.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                Map<String, String> fields = businessSignUpContainer.types_fields.get(position);
//                if (isChecked) {
//                    fields.put("isAvailableSunday", "true");
//                    businessSignUpContainer.types_fields.set(position, fields);
//                } else {
//                    fields.remove("isAvailableSunday");
//                }
//                // for all of them
//            });
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return businessSignUpContainer.num_of_types;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return position;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        class TypeHolder extends RecyclerView.ViewHolder {
//            TextInputEditText duration;
//            TextInputEditText type_text;
//            TextInputEditText notes_text;
//            CheckBox sunday;
//
//            TypeHolder(@NonNull View itemView) {
//                super(itemView);
//                type_text = itemView.findViewById(R.id.businessTypeEditText);
//                duration = itemView.findViewById(R.id.businessTypeDurationEditText);
//                notes_text = itemView.findViewById(R.id.businessNotesEditText);
//                sunday = itemView.findViewById(R.id.type_sunday);
//                // for all of them
//            }
//        }
//    }
}


