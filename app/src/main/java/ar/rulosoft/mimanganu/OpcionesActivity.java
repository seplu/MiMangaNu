package ar.rulosoft.mimanganu;

import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import java.io.File;
import java.io.IOException;

import ar.rulosoft.custompref.NumberPickerPref;
import ar.rulosoft.mimanganu.services.AlarmReceiver;
import ar.rulosoft.mimanganu.services.ChapterDownload;
import ar.rulosoft.mimanganu.services.DownloadPoolService;
import ar.rulosoft.mimanganu.services.SingleDownload;

public class OpcionesActivity extends PreferenceActivity {
    @SuppressWarnings( "deprecation" )
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        addPreferencesFromResource( R.xml.fragment_preferences );

        /** This enables to hide finished mangas, just a toggle */
        final CheckBoxPreference cBoxPref =
                (CheckBoxPreference) getPreferenceManager().findPreference( "mostrar_en_galeria" );
        cBoxPref.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                boolean valor = (Boolean) newValue;
                File f = new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/MiMangaNu/", ".nomedia" );
                if ( !valor ) if ( f.exists() ) f.delete();
                else if ( !f.exists() ) try {
                    f.createNewFile();
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                return true;
            }
        } );

        /** This sets the download threads (parallele Downloads) */
        final NumberPickerPref listPreferenceDT =
                (NumberPickerPref) getPreferenceManager().findPreference( "download_threads" );
        listPreferenceDT.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                int threads = Integer.parseInt( (String) newValue );
                int antes = DownloadPoolService.SLOTS;
                DownloadPoolService.SLOTS = threads;
                if ( DownloadPoolService.actual != null )
                    DownloadPoolService.actual.slots += ( threads - antes );
                return true;
            }
        } );

        /** This sets the Maximum number of errors (Maximale Anzahl der Fehler) */
        final NumberPickerPref listPrefET =
                (NumberPickerPref) getPreferenceManager().findPreference( "error_tolerancia" );
        listPrefET.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                ChapterDownload.MAX_ERRORS =
                        Integer.parseInt( (String) newValue );
                return true;
            }
        } );

        /** This sets the Number of retries by image */
        NumberPickerPref listPrefRT =
                (NumberPickerPref) getPreferenceManager().findPreference( "reintentos" );
        listPrefRT.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                SingleDownload.RETRY = Integer.parseInt( (String) newValue );
                return true;
            }
        } );

        /** This sets the Update Interval of the mangas (i.e. once per week) */
        final ListPreference listPrefCU =
                (ListPreference) getPreferenceManager().findPreference( "update_interval" );
        listPrefCU.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                long time = Long.parseLong( (String) newValue );
                if ( time > 0 ) {
                    AlarmReceiver.setAlarms( getApplicationContext(),
                            System.currentTimeMillis() + time, time );
                } else {
                    AlarmReceiver.stopAlarms( getApplicationContext() );
                }
                return true;
            }
        } );
    }
}
