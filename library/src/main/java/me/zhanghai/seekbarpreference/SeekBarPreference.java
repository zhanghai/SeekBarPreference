package me.zhanghai.seekbarpreference;

/*
 * Copyright (c) 2014 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;

public class SeekBarPreference extends DialogPreference {

    private static final int[] R_styleable_DialogPreference =
            new int[] {android.R.attr.dialogLayout};
    private static final int R_styleable_DialogPreference_dialogLayout = 0;

    private SeekBar mSeekBar;

    private int mProgress;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs) {

        // HACK: We cannot easily use defStyleAttr, so we set the default dialog layout resource
        // here.
        // No defStyleAttr or defStyleRes to obtain the value specified in XML.
        final TypedArray a = context.obtainStyledAttributes(attrs, R_styleable_DialogPreference);
        int dialogLayoutResId = a.getResourceId(R_styleable_DialogPreference_dialogLayout, 0);
        a.recycle();
        if (dialogLayoutResId == 0) {
            setDialogLayoutResource(R.layout.preference_dialog_seekbar);
        }

        mSeekBar = new SeekBar(context, attrs);
        mSeekBar.setId(R.id.seekbar);
        /*
         * The preference framework and view framework both have an 'enabled'
         * attribute. Most likely, the 'enabled' specified in this XML is for
         * the preference framework, but it was also given to the view framework.
         * We reset the enabled state.
         */
        mSeekBar.setEnabled(true);
    }

    /**
     * Saves the progress to the {@link android.content.SharedPreferences}.
     *
     * @param progress The progress to save
     */
    public void setProgress(int progress) {

        if (progress > mSeekBar.getMax()) {
            progress = mSeekBar.getMax();
        } else if (progress < 0) {
            progress = 0;
        }

        if (progress != mProgress) {
            mProgress = progress;
            if (shouldPersist()) {
                persistInt(progress);
            }
            notifyChanged();
        }
    }

    /**
     * Gets the progress from the {@link android.content.SharedPreferences}.
     *
     * @return The current preference value.
     */
    public int getProgress() {
        return mProgress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence getSummary() {
        CharSequence summary = super.getSummary();
        if (summary == null) {
            return null;
        } else {
            return String.format(summary.toString(), getProgress(), mSeekBar.getMax());
        }
    }

    /**
     * Returns the raw summary of this Preference, not formatted as in getSummary().
     *
     * @return The raw summary.
     * @see #getSummary()
     */
    public CharSequence getRawSummary() {
        return super.getSummary();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        SeekBar seekBar = mSeekBar;
        seekBar.setProgress(mProgress);

        ViewParent oldParent = seekBar.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(seekBar);
            }
            onAddSeekBarToDialogView(view, seekBar);
        }
    }

    /**
     * Adds the SeekBar widget of this preference to the dialog's view.
     *
     * @param dialogView The dialog view.
     */
    protected void onAddSeekBarToDialogView(View dialogView, SeekBar seekBar) {
        ViewGroup container = (ViewGroup) dialogView.findViewById(R.id.seekbar_container);
        if (container != null) {
            container.addView(seekBar, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_UP) {
                    if (keyCode == KeyEvent.KEYCODE_PLUS
                            || keyCode == KeyEvent.KEYCODE_EQUALS) {
                        mSeekBar.setProgress(getProgress() + 1);
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_MINUS) {
                        mSeekBar.setProgress(getProgress() - 1);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int value = mSeekBar.getProgress();
            if (callChangeListener(value)) {
                setProgress(value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setProgress(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);
    }

    /**
     * Returns the {@link android.widget.SeekBar} widget that will be shown in the dialog.
     *
     * @return The {@link android.widget.SeekBar} widget that will be shown in the dialog.
     */
    public SeekBar getSeekBar() {
        return mSeekBar;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }
        // Save the instance state
        final SavedState myState = new SavedState(superState);
        myState.progress = mProgress;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setProgress(myState.progress);
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {

        int progress;

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        public SavedState(Parcel source) {
            super(source);
            progress = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(progress);
        }
    }
}
