package com.investmango.hrconsole.user.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog {
    private static ProgressDialog progress;
    private static Context ctx;

    public static ProgressDialog getProgressDialog(Context context, String msg) {

        ctx = context;
        progress = new ProgressDialog(ctx);
        progress.setTitle("Loading");
        progress.setMessage(msg);
        progress.setCancelable(false);

        return progress;
    }
}
