package com.example.yardassist.activities.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.yardassist.activities.MapActivity;
import com.example.yardassist.classes.Taskitem;

public class poptask extends AppCompatDialogFragment {

    Taskitem item;

    public poptask(Taskitem item) {
        this.item=item;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Starting the task").setMessage("Do you want to start the task for vehical with id "+item.getId().toString()).setPositiveButton("Start the Task", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialog, int which) {
                String name = getActivity().getLocalClassName();
                Intent i = new Intent(getActivity(), MapActivity.class);
                i.putExtra("activity_ID", name);
                i.putExtra("item", item);
                startActivity(i);
            }
        });
        return builder.create();

    }
}

