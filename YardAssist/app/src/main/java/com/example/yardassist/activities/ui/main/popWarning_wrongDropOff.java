package com.example.yardassist.activities.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.yardassist.classes.Vehicle;
import com.google.firebase.database.annotations.Nullable;

public class popWarning_wrongDropOff extends AppCompatDialogFragment {
    private popWarning_wrongDropOff.popDropOffWarningListener listener;
    Vehicle vehicle;

    public popWarning_wrongDropOff(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning!").setMessage("You are trying to drop off vehicle "+vehicle.getRegNumber()+" in a cell not matching the tasks designated end cell");
        builder.setPositiveButton("Drop Off", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialog, int which) {

                listener.onDropOff(vehicle);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        return builder.create();

    }
    public interface popDropOffWarningListener {
        void onDropOff( Vehicle vehicle);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            listener = (popWarning_wrongDropOff.popDropOffWarningListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement popWarningListener");
        }
    }
}