package me.zhanghai.android.seekbarpreference;

/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class SeekBarPreference extends DialogPreference {

    // As in PreferenceFragmentCompat, because we want to ensure that at most one dialog is showing.
    private static final String DIALOG_FRAGMENT_TAG =
            "android.support.v7.preference.PreferenceFragment.DIALOG";

    private SeekBar mSeekBar;

    private int mProgress;

    /**
     * Users should override {@link PreferenceFragmentCompat#onDisplayPreferenceDialog(Preference)}
     * and check the return value of this method, only call through to super implementation if
     * {@code false} is returned.
     *
     * @param preferenceFragment The preference fragment
     * @param preference The preference, as in
     * {@link PreferenceFragmentCompat#onDisplayPreferenceDialog(Preference)}
     * @return Whether the call has been handled by this method.
     */
    public static boolean onDisplayPreferenceDialog(PreferenceFragmentCompat preferenceFragment,
                                                    Preference preference) {

        if (preference instanceof SeekBarPreference) {
            // getChildFragmentManager() will lead to looking for target fragment in the child
            // fragment manager.
            FragmentManager fragmentManager = preferenceFragment.getFragmentManager();
            if(fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
                SeekBarPreferenceDialogFragment dialogFragment =
                        SeekBarPreferenceDialogFragment.newInstance(preference.getKey());
                dialogFragment.setTargetFragment(preferenceFragment, 0);
                dialogFragment.show(fragmentManager, DIALOG_FRAGMENT_TAG);
            }
            return true;
        }

        return false;
    }

    public SeekBarPreference(Context context) {
        super(context);

        init(context, null);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr,
                             int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        // Catch the case when no style is provided.
        if (getDialogLayoutResource() == 0) {
            setDialogLayoutResource(R.layout.sbp_preference_dialog_seekbar);
        }

        mSeekBar = new SeekBar(context, attrs);
        mSeekBar.setId(R.id.sbp_seekbar);
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
