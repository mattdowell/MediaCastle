package org.dowell.mediacastle;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Toast;

import org.dowell.mediacastle.user.Session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Properties;

public abstract class AbstractActivity extends Activity {

    public static final int DISP_MED_ACT = 90;
    public static final int DEL_IMAGE_ACT = 100;
    public static final int ENT_PWD_ACT = 110;
    public static final int IMP_MED_ACT = 120;
    public static final int NEW_PWD_ACT = 130;
    public static final int RET_IMAGE_ACT = 140;
    private static final String PWD_PREF_NAME = "mcpwd";
    private static final String MC_PREFS_FILE = "mc_prefs_file";
    // PROPERTIES
    private static final String VERSION = "version";
    private static Properties appProperties = null;

    boolean hasValidSession() {
        if (Session.getPasscode() != null &&
                Session.getPasscode().equals(getPasswordFromStorage())) {
            return true;
        } else {
            return false;
        }
    }

    void clearSession() {
        Session.setPasscode(null);
    }

    void hideKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    void showKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void showShortToast(String text) {
        Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        t.show();
    }

    public void showLongToast(String text) {
        Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        t.show();
    }

    void startDisplayCreatePwd() {
        Intent intent = new Intent(getApplicationContext(), NewPwdActivity.class);
        intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        startActivityForResult(intent, NEW_PWD_ACT);
    }

    void startDisplayEnterPwd() {
        Intent intent = new Intent(getApplicationContext(), EnterPwdActivity.class);
        // TODO: Need to clear history
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, ENT_PWD_ACT);
    }

    void startDisplayMediaActivity(Boolean hasPwdBeenEntered) {
        Intent intent = new Intent(getApplicationContext(), DisplayMediaActivity.class);
        intent.putExtra(EnterPwdActivity.MC_PWD_STATE, "true");
        startActivity(intent);
    }

    void startDeleteImagesActivity() {
        Intent intent = new Intent(getApplicationContext(), DeleteImagesActivity.class);
        startActivityForResult(intent, DEL_IMAGE_ACT);
    }

    void startReturnImagesActivity() {
        Intent intent = new Intent(getApplicationContext(), RetImgToGalleryActivity.class);
        startActivityForResult(intent, RET_IMAGE_ACT);
    }

    void startDisplayMediaActivity() {
        startDisplayMediaActivity(false);
    }

    void startDisplayImportMediaActivity() {
        Intent intent = new Intent(getApplicationContext(), ImportMediaActivity.class);
        startActivityForResult(intent, IMP_MED_ACT);
    }

    SharedPreferences getPrefs() {
        return this.getApplicationContext().getSharedPreferences(MC_PREFS_FILE, MODE_PRIVATE);
    }

    String getPasswordFromStorage() {
        return getPrefs().getString(PWD_PREF_NAME, null);
    }

    void putPasswordInStorage(String inPwd) {
        SharedPreferences.Editor edit = getPrefs().edit();
        edit.putString(PWD_PREF_NAME, inPwd);
        edit.commit();
    }

    boolean hasPwdBeenCreated() {
        String pwd = getPasswordFromStorage();
        if (pwd == null || pwd.equalsIgnoreCase("")) {
            return false;
        }
        return true;
    }

    void clearPwd() {
        putPasswordInStorage(null);
    }

    void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    void deleteSecuredFile(String inPath) {
        File f = new File(inPath);
        f.delete();
    }

    /**
     * Is this the lite version of the app?
     *
     * @return
     */
    boolean isLiteVersion() {
        if (appProperties == null) {
            loadProperties();
        }
        String ver = appProperties.getProperty(VERSION);
        if (ver != null && ver.equalsIgnoreCase("lite")) {
            return true;
        }
        return false;
    }

    /**
     * Loads the app properties file
     */
    private void loadProperties() {
        Resources resources = this.getResources();
        AssetManager assetManager = resources.getAssets();

        try {
            InputStream inputStream = assetManager.open("mc.properties");
            appProperties = new Properties();
            appProperties.load(inputStream);
            System.out.println("The properties are now loaded");
            System.out.println("properties: " + appProperties);
        } catch (IOException e) {
            System.err.println("Failed to open app property file");
            e.printStackTrace();
        }
    }

}
