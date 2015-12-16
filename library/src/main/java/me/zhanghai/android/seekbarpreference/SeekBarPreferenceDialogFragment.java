/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.seekbarpreference;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;

public class SeekBarPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private SeekBar mSeekBar;

    public static SeekBarPreferenceDialogFragment newInstance(String key) {
        SeekBarPreferenceDialogFragment fragment = new SeekBarPreferenceDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_KEY, key);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSeekBar = getSeekBarPreference().getSeekBar();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_PLUS
                            || keyCode == KeyEvent.KEYCODE_EQUALS) {
                        mSeekBar.setProgress(mSeekBar.getProgress() + 1);
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_MINUS) {
                        mSeekBar.setProgress(mSeekBar.getProgress() - 1);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mSeekBar.setProgress(getSeekBarPreference().getProgress());

        ViewParent oldParent = mSeekBar.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(mSeekBar);
            }
            onAddSeekBarToDialogView(view, mSeekBar);
        }
    }

    /**
     * Adds the SeekBar widget of this preference to the dialog's view.
     *
     * @param dialogView The dialog view.
     */
    private void onAddSeekBarToDialogView(View dialogView, SeekBar seekBar) {
        ViewGroup container = (ViewGroup) dialogView.findViewById(R.id.sbp_seekbar_container);
        if (container != null) {
            container.addView(seekBar, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        int value = mSeekBar.getProgress();
        SeekBarPreference preference = getSeekBarPreference();
        if (preference.callChangeListener(value)) {
            preference.setProgress(value);
        }
    }

    private SeekBarPreference getSeekBarPreference() {
        return (SeekBarPreference) getPreference();
    }
}
