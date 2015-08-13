package ar.rulosoft.custompref;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Johndeep on 13.08.15.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import ar.rulosoft.mimanganu.R;

public class NumberPickerPref extends DialogPreference {

    private NumberPicker mNumberPicker;
    private int mMin;
    private int mMax;
    private boolean mWrapAround;
    private int mValue = 0;

    private String mSummary;

    public NumberPickerPref( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes ) {
        super( context, attrs );

        TypedArray a =
                context.obtainStyledAttributes( attrs, R.styleable.NumberPickerPref, defStyleAttr, defStyleRes );
        mMin = a.getInteger( R.styleable.NumberPickerPref_val_min, 0 );
        mMax = a.getInteger( R.styleable.NumberPickerPref_val_max, 9 );
        mWrapAround =
                a.getBoolean( R.styleable.NumberPickerPref_wrap_around, false );
        a.recycle();

        /** In this case, I retrieve the summary, so I can simulate the
         * behavior of the other pref widgets */
        mSummary = (String) super.getSummary();
    }

    public NumberPickerPref( Context context, AttributeSet attrs, int defStyleAttr ) {
        this( context, attrs, defStyleAttr, 0 );
    }

    public NumberPickerPref( Context context, AttributeSet attrs ) {
        this( context, attrs, 0 );
    }

    public NumberPickerPref( Context context ) {
        this( context, null );
    }

    @Override
    public CharSequence getSummary() {
        final Integer entry = mValue;
        if ( mSummary == null ) {
            return super.getSummary();
        } else {
            return String.format( mSummary, entry );
        }
    }

    @Override
    public void setSummary( CharSequence summary ) {
        super.setSummary( summary );
        if ( summary == null && mSummary != null ) {
            mSummary = null;
        } else if ( summary != null && !summary.equals( mSummary ) ) {
            mSummary = summary.toString();
        }
    }

    @Override
    protected View onCreateDialogView() {
        mNumberPicker = new NumberPicker( getContext() );
        return ( mNumberPicker );
    }

    @Override
    protected void onBindDialogView( @NonNull View view ) {
        super.onBindDialogView( view );
        mNumberPicker.setMinValue( mMin );
        mNumberPicker.setMaxValue( mMax );
        mNumberPicker.setValue( mValue );
        mNumberPicker.setWrapSelectorWheel( mWrapAround );
    }

    @Override
    protected void onDialogClosed( boolean positiveResult ) {
        super.onDialogClosed( positiveResult );
        if ( positiveResult ) {
            mValue = mNumberPicker.getValue();
            String pushValue = String.valueOf( mValue );
            if ( callChangeListener( pushValue ) ) {
                persistString( pushValue );
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue( TypedArray a, int index ) {
        return ( a.getString( index ) );
    }

    @Override
    protected void onSetInitialValue( boolean restorePrefValue, Object defaultValue ) {
        String getValue;
        if ( restorePrefValue ) {
            if ( defaultValue == null ) {
                getValue = getPersistedString( "0" );
            } else {
                getValue = getPersistedString( String.valueOf( defaultValue ) );
            }
        } else {
            getValue = String.valueOf( defaultValue );
        }
        mValue = Integer.parseInt( getValue );
    }
}
