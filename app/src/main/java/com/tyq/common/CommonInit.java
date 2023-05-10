package com.tyq.common;

import android.content.Context;

/**
 * @author typw
 */
public class CommonInit {

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    public static Context getContext() {
        return mContext;
    }
}