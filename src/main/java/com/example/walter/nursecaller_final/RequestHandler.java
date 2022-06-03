package com.example.walter.nursecaller_final;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
//
public class RequestHandler {
    private static com.example.walter.nursecaller_final.RequestHandler mInstance;
    private RequestQueue  mRequestQueue;
    private static Context mCtx;
    private RequestHandler(Context context)
    {
        mCtx=context;
        mRequestQueue=getRequestQueue();

    }
    public static synchronized com.example.walter.nursecaller_final.RequestHandler getInstance(Context context){
        if (mInstance==null)
        {
            mInstance= new com.example.walter.nursecaller_final.RequestHandler(context);
        }
        return  mInstance;
    }
    public RequestQueue getRequestQueue(){
        if (mRequestQueue==null){
            mRequestQueue= Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req)
    {
        getRequestQueue().add(req);
    }
}
