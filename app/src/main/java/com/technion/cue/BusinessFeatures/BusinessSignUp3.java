package com.technion.cue.BusinessFeatures;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.technion.cue.R;
import com.technion.cue.annotations.ModuleAuthor;

import java.util.HashMap;
import java.util.Map;

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
            businessSignUpContainer.types_adapter.notifyDataSetChanged();
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
                holder.type_text.setText(businessSignUpContainer.types_fields.get(position).get("name"));
                holder.duration.setText(businessSignUpContainer.types_fields.get(position).get("duration"));
                holder.notes_text.setText(businessSignUpContainer.types_fields.get(position).get("notes"));
                if (businessSignUpContainer.types_fields.get(position).containsKey("isAvailableSunday")) {
                    holder.sunday.setChecked(true);
                }
                // for all of them
            } else {
                businessSignUpContainer.types_fields.add(new HashMap<>());
            }

            holder.type_text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Map<String, String> fields = businessSignUpContainer.types_fields.get(position);
                    fields.put("name", s.toString());
                    businessSignUpContainer.types_fields.set(position, fields);
                }
            });

            holder.duration.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Map<String, String> fields = businessSignUpContainer.types_fields.get(position);
                    fields.put("duration", s.toString());
                    businessSignUpContainer.types_fields.set(position, fields);
                }
            });

            holder.notes_text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Map<String, String> fields = businessSignUpContainer.types_fields.get(position);
                    fields.put("notes", s.toString());
                    businessSignUpContainer.types_fields.set(position, fields);
                }
            });

            holder.sunday.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Map<String, String> fields = businessSignUpContainer.types_fields.get(position);
                if (isChecked) {
                    fields.put("isAvailableSunday", "true");
                    businessSignUpContainer.types_fields.set(position, fields);
                } else {
                    fields.remove("isAvailableSunday");
                }
                // for all of them
            });

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
            TextInputEditText duration;
            TextInputEditText type_text;
            TextInputEditText notes_text;
            CheckBox sunday;

            TypeHolder(@NonNull View itemView) {
                super(itemView);
                type_text = itemView.findViewById(R.id.businessTypeEditText);
                duration = itemView.findViewById(R.id.businessTypeDurationEditText);
                notes_text = itemView.findViewById(R.id.businessNotesEditText);
                sunday = itemView.findViewById(R.id.type_sunday);
                // for all of them
            }
        }
    }
}


