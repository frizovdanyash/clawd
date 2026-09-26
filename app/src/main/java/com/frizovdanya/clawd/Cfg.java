package com.frizovdanya.clawd;

import android.content.Context;
import android.content.SharedPreferences;

/* loaded from: classes.dex */
public class Cfg {
    private static final String NAME = "clawd_cfg";
    private final SharedPreferences sp;

    public Cfg(Context context) {
        this.sp = context.getApplicationContext().getSharedPreferences(NAME, 0);
    }

    public boolean b(String str, boolean z) {
        return this.sp.getBoolean(str, z);
    }

    public int i(String str, int i) {
        try {
            return this.sp.getInt(str, i);
        } catch (ClassCastException unused) {
            return i;
        }
    }

    public String s(String str, String str2) {
        return this.sp.getString(str, str2);
    }

    public void putBool(String str, boolean z) {
        this.sp.edit().putBoolean(str, z).apply();
    }

    public void putInt(String str, int i) {
        this.sp.edit().putInt(str, i).apply();
    }

    public void putStr(String str, String str2) {
        this.sp.edit().putString(str, str2).apply();
    }

    public void putLong(String str, long j) {
        this.sp.edit().putLong(str, j).apply();
    }

    public long lng(String str, long j) {
        try {
            return this.sp.getLong(str, j);
        } catch (ClassCastException unused) {
            return j;
        }
    }
}
