package org.dowell.mediacastle;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.dowell.mediacastle.user.Session;

public class EnterPwdActivity extends AbstractActivity implements OnClickListener {

    public static final String MC_PWD_STATE = "MC_PWD_STATE";
    private static int incorrectTries = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasPwdBeenCreated()) {
            setContentView(R.layout.enterpwd);
            Button sumbitButton = (Button) findViewById(R.id.submitbutton);
            sumbitButton.setOnClickListener(this);
            showKeyboard();
        } else {
            startDisplayCreatePwd();
        }
    }


    /**
     * Override the back key, can be used to sneak in.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Override the back key. Can be used to sneak in.
     */
    public void onBackPressed() {
    }

    /**
     *
     */
    public void onClick(View v) {
        EditText passwordEditText = (EditText) findViewById(R.id.password);

        // if people are allowed to set the password on the first run uncomment
        // the following and delete the uncommented section of this function
        String password = getPasswordFromStorage();

        if (password == null || password.equals("")) {
            // Have not created a pwd yet, redirect to that act.
            startDisplayCreatePwd();
        } else if (passwordEditText.getText().toString().equals(password)) {

            hideKeyboard();
            setResult(RESULT_OK);
            Session.setPasscode(password);
            finish();

        } else {
            showShortToast("Password is incorrect");
            incorrectTries++;

            if (incorrectTries >= 4) {
                incorrectTries = 0;
                clearSession();
                try {
                    super.onDestroy();
                    finish();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }
    }

}
