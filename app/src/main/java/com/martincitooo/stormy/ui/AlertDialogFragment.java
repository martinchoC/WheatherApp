package com.martincitooo.stormy.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.martincitooo.stormy.R;

public class AlertDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.ettot_title)
               .setMessage(R.string.error_message)
               .setPositiveButton(R.string.error_ok_button_text,null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
