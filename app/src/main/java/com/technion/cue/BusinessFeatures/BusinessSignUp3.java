package com.technion.cue.BusinessFeatures;

import android.app.TimePickerDialog;
import java.text.ParseException;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.technion.cue.R;
import com.technion.cue.SignInActivity;
import com.technion.cue.annotations.ModuleAuthor;
import com.technion.cue.data_classes.Business;

import java.util.Calendar;
import java.util.Date;

import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;

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
                holder.type_text.setText(businessSignUpContainer.types_fields.get(position).first);
                holder.duration.setText(businessSignUpContainer.types_fields.get(position).second);
            } else {
                businessSignUpContainer.types_fields.add(new Pair<>("", ""));
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
                    businessSignUpContainer.types_fields.set(position, new Pair<>(
                            s.toString(), businessSignUpContainer.types_fields.get(position).second
                    ));
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
                    businessSignUpContainer.types_fields.set(position, new Pair<>(
                            businessSignUpContainer.types_fields.get(position).first, s.toString()
                    ));
                }
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

            TypeHolder(@NonNull View itemView) {
                super(itemView);
                type_text = itemView.findViewById(R.id.businessTypeEditText);
                duration = itemView.findViewById(R.id.businessTypeDurationEditText);
            }
        }
    }
}


