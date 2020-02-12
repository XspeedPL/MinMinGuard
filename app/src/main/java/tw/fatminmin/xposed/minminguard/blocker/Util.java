package tw.fatminmin.xposed.minminguard.blocker;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import tw.fatminmin.xposed.minminguard.BuildConfig;
import tw.fatminmin.xposed.minminguard.Common;
import tw.fatminmin.xposed.minminguard.Main;

import java.io.File;

public final class Util
{
    // change it to false for release build
    public static boolean DEBUG = BuildConfig.DEBUG;

    public static final String TAG = "MinMinGuard";
    private static final String PACKAGE = "tw.fatminmin.xposed.minminguard";

    private Util() throws InstantiationException
    {
        throw new InstantiationException("This class is not for instantiation");
    }

    public static Boolean xposedEnabled()
    {
        return false;
    }

    public static void log(String packageName, String msg)
    {
        if (DEBUG)
        {
            //Log.d(TAG, packageName + ": " + msg);
            XposedBridge.log(packageName + ": " + msg);
        }
    }

    public static Application getCurrentApplication()
    {
        try
        {
            return AndroidAppHelper.currentApplication();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static void hookAllMethods(String className, ClassLoader classLoader, String method, XC_MethodHook callBack)
    {
        Class<?> clazz = XposedHelpers.findClass(className, classLoader);

        XposedBridge.hookAllMethods(clazz, method, callBack);
    }
}
