package com.asisctf.Andex;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;

public class DexJob {
    public static void printAllClassName(String str, Context context) {
        try {
            Enumeration entries = DexFile.loadDex(str, File.createTempFile("opt", "dex", context.getCacheDir()).getPath(), 0).entries();
            while (entries.hasMoreElements()) {
                String str2 = (String) entries.nextElement();
                PrintStream printStream = System.out;
                StringBuilder sb = new StringBuilder();
                sb.append("class: ");
                sb.append(str2);
                printStream.println(sb.toString());
            }
        } catch (IOException e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Error opening ");
            sb2.append(str);
            Log.w("dexjob", sb2.toString(), e);
        }
    }

    public static Class<Object> getClassFromDex(String str, String str2, Context context) {
        try {
            return new DexClassLoader(str, context.getCodeCacheDir().getAbsolutePath(), null, context.getClass().getClassLoader()).loadClass(str2);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
