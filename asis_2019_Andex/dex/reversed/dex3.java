package com.asisctf.config;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class SayHelloToYourLittleFriend {
    public void order_as_gold_user(String as_gold_url, String user_id, String item_id, Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringBuilder sb = new StringBuilder();
        sb.append(as_gold_url);
        sb.append(item_id);
        final String str = user_id;
        C02803 r2 = new JsonObjectRequest(0, sb.toString(), null, new Listener<JSONObject>() {
            public void onResponse(JSONObject response) {
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("uuid", str);
                return params;
            }
        };
        queue.add(r2);
    }

    public String flag_response() {
        return "you are not gold user to buy this item";
    }

    public String[] configuration() {
        return new String[]{"/api/userClass/u/[user_id]/get/[property_name]"};
    }
}