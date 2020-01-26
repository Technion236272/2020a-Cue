package com.technion.cue.BusinessFeatures;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.technion.cue.R;

import java.util.HashMap;
import java.util.Map;

public class TypesDialog extends DialogFragment {

    private final BusinessSignUpContainer businessSignUpContainer;

    TypesDialog(BusinessSignUpContainer businessSignUpContainer) {
        this.businessSignUpContainer = businessSignUpContainer;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View v = inflater.inflate(R.layout.sign_up_type_change_holder, container, false);
        v.findViewById(R.id.save_type_changes).setOnClickListener(cl -> {
            Map<String, String> m = new HashMap<>();
            if (!isInputNotEmpty(v.findViewById(R.id.businessTypeEditText),
                    v.findViewById(R.id.businessTypeDurationEditText),
                    v.findViewById(R.id.businessNotesEditText))) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = ((TextInputEditText)
                    v.findViewById(R.id.businessTypeEditText)).getText().toString();

            // checking that no appointment type of this name exists
            for (Map<String, String> map : businessSignUpContainer.types_fields) {
                if (map.containsKey("name") && map.get("name").equals(name)) {
                    Toast.makeText(getContext(),
                            "An appointment type of this name already exists. Please choose a different name",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            m.put("name", name);
            m.put("duration", ((TextInputEditText)
                    v.findViewById(R.id.businessTypeDurationEditText)).getText().toString());
            m.put("notes", ((TextInputEditText)
                    v.findViewById(R.id.businessNotesEditText)).getText().toString());
            m.put("sunday", ((Boolean)((CheckBox)
                    v.findViewById(R.id.type_sunday)).isChecked()).toString());
            m.put("monday", ((Boolean)((CheckBox)
                    v.findViewById(R.id.type_monday)).isChecked()).toString());
            m.put("tuesday", ((Boolean)((CheckBox)
                    v.findViewById(R.id.type_tuesday)).isChecked()).toString());
            m.put("wednesday", ((Boolean)((CheckBox)
                    v.findViewById(R.id.type_wednesday)).isChecked()).toString());
            m.put("thursday", ((Boolean)((CheckBox)
                    v.findViewById(R.id.type_thursday)).isChecked()).toString());
            m.put("friday", ((Boolean)((CheckBox)
                    v.findViewById(R.id.type_friday)).isChecked()).toString());
            m.put("saturday", ((Boolean)((CheckBox)
                    v.findViewById(R.id.type_saturday)).isChecked()).toString());
            businessSignUpContainer.types_fields.add(m);
            businessSignUpContainer.types_adapter.notifyDataSetChanged();
            dismiss();
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private boolean isInputNotEmpty(TextInputEditText ... fields){
        for (TextInputEditText f : fields) {
            if (f.getText().toString().isEmpty())
                return false;
        }

        return true;
    }
}
