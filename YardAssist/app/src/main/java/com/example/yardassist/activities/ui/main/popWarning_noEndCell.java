package com.example.yardassist.activities.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class popWarning_noEndCell extends AppCompatDialogFragment {
    private popWarning_noEndCell.popWarningListener listener;
    String id;

    public popWarning_noEndCell(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning!").setMessage("You are trying to registrate a task for "+id+ " without an location");
        builder.setPositiveButton("Register without an location", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialog, int which) {
                listener.onRegisterClicked();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();

    }
    public interface popWarningListener {
        void onRegisterClicked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (popWarning_noEndCell.popWarningListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement popWarningListener");
        }
    }
}