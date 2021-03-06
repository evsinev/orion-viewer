/*
 * Orion Viewer - pdf, djvu, xps and cbz file viewer for android devices
 *
 * Copyright (C) 2011-2013  Michael Bogdanov & Co
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package universe.constellation.orion.viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import universe.constellation.orion.viewer.device.AndroidDevice;
import pl.polidea.customwidget.TheMissingTabHost;
import universe.constellation.orion.viewer.prefs.GlobalOptions;
import universe.constellation.orion.viewer.prefs.OrionApplication;

/**
 * User: mike
 * Date: 24.12.11
 * Time: 17:00
 */
public class OrionBaseActivity extends Activity {

    public static final String DONT_OPEN_RECENT = "DONT_OPEN_RECENT";

    protected Device device ;

    protected SharedPreferences.OnSharedPreferenceChangeListener listener;

    public OrionBaseActivity() {
        if (supportDevice()) {
            device = Common.createDevice();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getOrionContext().applyTheme(this);

        if (this instanceof OrionViewerActivity || this instanceof OrionFileManagerActivity) {
            int screenOrientation = getScreenOrientation(getApplicationDefaultOrientation());
            changeOrientation(screenOrientation);
        }

        super.onCreate(savedInstanceState);

        if (device != null) {
            device.onCreate(this);
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if (device != null) {
            device.onDestroy();
        }
    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (device != null) {
//            device.onResume();
//        }
//    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (device != null) {
                device.onWindowGainFocus();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (device != null) {
            device.onPause();
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (device != null) {
            device.onUserInteraction();
        }
    }

    public Device getDevice() {
        return device;
    }

    public View getView() {
        return null;
    }

    public int getViewerType() {
        return Device.DEFAULT_ACTIVITY;
    }

    protected void initHelpScreen() {
        TheMissingTabHost host = (TheMissingTabHost) findMyViewById(R.id.helptab);

        host.setup();

        TheMissingTabHost.TheMissingTabSpec spec = host.newTabSpec("general_help");
        spec.setContent(R.id.general_help);
        spec.setIndicator("", getResources().getDrawable(R.drawable.help));
        host.addTab(spec);

        TheMissingTabHost.TheMissingTabSpec recent = host.newTabSpec("app_info");
        recent.setContent(R.id.app_info);
        recent.setIndicator("", getResources().getDrawable(R.drawable.info));
        host.addTab(recent);
        host.setCurrentTab(0);

//        WebView view = (WebView) host.findViewById(R.id.webview_about);
//
//        view.getSettings().setJavaScriptEnabled(true);
//        view.addJavascriptInterface(new Localizer(getResources()), "ls");
//        view.loadUrl("file:///android_asset/about.html");

        ImageButton btn = (ImageButton) findMyViewById(R.id.help_close);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //animator.setDisplayedChild(MAIN_SCREEN);
                onAnimatorCancel();
            }
        });

        btn = (ImageButton) findMyViewById(R.id.info_close);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onAnimatorCancel();
                //animator.setDisplayedChild(MAIN_SCREEN);
            }
        });

        Device device = this.device != null ? this.device : Common.createDevice();
        if (AndroidDevice.class.equals(device.getClass())) {
            TextView tx = (TextView) findViewById(R.id.help_rotation_entry);
            tx.setText(R.string.rotation_android);

            tx = (TextView) findViewById(R.id.help_next_page_entry);
            tx.setText(R.string.next_page_android);

            TableRow tr = (TableRow) findViewById(R.id.help_prev_page_row);
            ((TableLayout)tr.getParent()).removeView(tr);
        }
    }

    protected View findMyViewById(int id) {
        return findViewById(id);
    }

    protected void onAnimatorCancel() {

    }

    protected void onApplyAction() {

    }

    public boolean supportDevice() {
        return true;
    }

    public OrionApplication getOrionContext() {
        return (OrionApplication) getApplicationContext();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (device != null) {
            device.onSetContentView();
        }
    }


    public void showWarning(String warning) {
        Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
    }

    public void showWarning(int stringId) {
        showWarning(getResources().getString(stringId));
    }

    public void showFastMessage(int stringId) {
        showWarning(getResources().getString(stringId));
    }

    public void showLongMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void showFastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showError(Exception e) {
        showError("Error", e);
    }

    public void showError(String error, Exception ex) {
        Toast.makeText(this, error + ": " + ex.getMessage(), Toast.LENGTH_LONG).show();
        Common.d(ex);
    }

    class Localizer {
        private Resources res;

        public Localizer(Resources res) {
            this.res = res;
        }
        public String get(String key) {
            try {
                int id = res.getIdentifier(key, "string", "universe.constellation.orion.viewer");
                return res.getString(id);
            } catch (Exception e) {
                return key;
            }
        }
    }

    public void changeOrientation(int orientationId) {
        System.out.println("Display orientation "+ getRequestedOrientation() + " screenOrientation " + getWindow().getAttributes().screenOrientation);
        if (getRequestedOrientation() != orientationId) {
            setRequestedOrientation(orientationId);
        }
    }

    public int getScreenOrientation(String id) {
        int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if ("LANDSCAPE".equals(id)) {
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if ("PORTRAIT".equals(id)) {
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if ("LANDSCAPE_INVERSE".equals(id)) {
            screenOrientation = getOrionContext().getSdkVersion() < 9 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : 8;
        } else if ("PORTRAIT_INVERSE".equals(id)) {
            screenOrientation = getOrionContext().getSdkVersion() < 9 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : 9;
        }
        return screenOrientation;
    }

    public int getScreenOrientationItemPos(String id) {
        int screenOrientation = 0;
        if ("LANDSCAPE".equals(id)) {
            screenOrientation = 2;
        } else if ("PORTRAIT".equals(id)) {
            screenOrientation = 1;
        } else if ("LANDSCAPE_INVERSE".equals(id)) {
            screenOrientation = getOrionContext().getSdkVersion() < 9 ? 2 : 4;
        } else if ("PORTRAIT_INVERSE".equals(id)) {
            screenOrientation = getOrionContext().getSdkVersion() < 9 ? 1 : 3;
        }
        return screenOrientation;
    }

    public String getApplicationDefaultOrientation() {
        return getOrionContext().getOptions().getStringProperty(GlobalOptions.SCREEN_ORIENTATION, "DEFAULT");
    }

    public static void setContentView(final Activity activity, final  int layout) {
        if (Device.Info.NOOK_CLASSIC) {
            activity.setContentView(R.layout.nook_template);
            View view = activity.findViewById(R.id.nook_sensor2);
            LayoutInflater inflater = activity.getLayoutInflater();
            inflater.inflate(layout, (ViewGroup) view);

            ImageButton menu = (ImageButton) activity.findViewById(R.id.nook_menu);
            menu.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    activity.openOptionsMenu();
                }
            });

            ImageButton cancel = (ImageButton) activity.findViewById(R.id.nook_cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    activity.finish();
                }
            });

        } else {
            activity.setContentView(layout);
        }
    }

    public void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
