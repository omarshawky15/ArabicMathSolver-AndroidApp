package com.arabic.math.solver.drawview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.arabic.math.solver.R;

public class ClearDialogFragment extends DialogFragment {

    ClearDialogListener myListener;

    public ClearDialogFragment(ClearDialogListener listener) {
        myListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_clear_all_msg_str)
                .setTitle(R.string.dialog_clear_all_title_str)
                .setPositiveButton(R.string.confirm_str, (dialog, id) -> myListener.onDialogPositiveClick(ClearDialogFragment.this))
                .setNegativeButton(R.string.cancel_str, (dialog, id) -> myListener.onDialogNegativeClick(ClearDialogFragment.this));
        return builder.create();
    }

    public interface ClearDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }
}
