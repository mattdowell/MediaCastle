package org.dowell.mediacastle;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewPwdActivity extends AbstractActivity implements OnClickListener {

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createpassword);
        Button sumbitButton = (Button) findViewById(R.id.setpwd);
        sumbitButton.setOnClickListener(this);
        showKeyboard();
    }

    public void onClick(View v) {

        EditText pwdOne = (EditText) findViewById(R.id.password_one);
        EditText pwdTwo = (EditText) findViewById(R.id.password_two);

        if ((pwdOne != null && pwdTwo != null) && (pwdOne.getText().toString().equals(pwdTwo.getText().toString()))) {

            if (pwdOne.length() > 2) {

                clearPwd();
                putPasswordInStorage(pwdOne.getText().toString());

                if (super.hasPwdBeenCreated()) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    showShortToast("There was a problem saving the password");
                }
            } else {
                showShortToast("The password must be greater than three characters");
            }

        } else {
            showShortToast("Pwd is empty, or wrong");
        }
    }
}
