package org.mian.gitnex.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.notifications.SetupNotifier;
import org.mian.gitnex.util.TinyDB;

/**
 * Author M M Arif
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        if(tinyDb.getInt("themeId") == 1) {
            setTheme(R.style.AppThemeLight);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        if(tinyDb.getInt("customFontId") == 0) {

            FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/roboto.ttf");
            FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/roboto.ttf");
            FontsOverride.setDefaultFont(this, "SERIF", "fonts/roboto.ttf");
            FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/roboto.ttf");

        }
        else if (tinyDb.getInt("customFontId") == 1) {

            FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/manroperegular.ttf");
            FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/manroperegular.ttf");
            FontsOverride.setDefaultFont(this, "SERIF", "fonts/manroperegular.ttf");
            FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/manroperegular.ttf");

        }
        else if (tinyDb.getInt("customFontId") == 2) {

            FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/sourcecodeproregular.ttf");
            FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/sourcecodeproregular.ttf");
            FontsOverride.setDefaultFont(this, "SERIF", "fonts/sourcecodeproregular.ttf");
            FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/sourcecodeproregular.ttf");

        }
        else {

            FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/roboto.ttf");
            FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/roboto.ttf");
            FontsOverride.setDefaultFont(this, "SERIF", "fonts/roboto.ttf");
            FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/roboto.ttf");

        }

        TinyDB tinyDB = new TinyDB(getApplicationContext());
        tinyDB.putInt("pollingDelaySeconds", 5); // DEBUGGING

        if(tinyDB.getInt("pollingDelaySeconds") == 0) {
            tinyDB.putInt("pollingDelaySeconds", 50);
        }

        SetupNotifier.setup(getApplicationContext());

    }

    protected abstract int getLayoutResourceId();

}


