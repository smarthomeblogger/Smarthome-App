package de.smarthome_blogger.smarthome.system;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Map;

/**
 * Created by Sascha on 19.09.2016.
 */
public class HTTPRequest {

    /**
     * Sendet eine HTTP-Post-Request an den Server
     * @param context
     * @param requestData
     * @param serverIp
     * @param callback
     */
    public static void sendRequest(final Context context, final Map<String,String> requestData, String serverIp,
                                   final HTTPRequestCallback callback){

        RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

        String url = "http://"+serverIp+"/api.php";

        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RequestResult", response);

                callback.onRequestResult(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError("Server nicht erreichbar");
            }
        }){
            protected Map<String, String> getParams(){
                return requestData;
            }
        };

        MyRequestQueue.add(MyStringRequest);

    }

    /**
     * Führt Callback in implementierenden Klassen aus
     */
    public interface HTTPRequestCallback{
        void onRequestResult(String result);

        void onError(String msg);
    }

}
