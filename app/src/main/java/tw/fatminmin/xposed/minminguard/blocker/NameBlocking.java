package tw.fatminmin.xposed.minminguard.blocker;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import tw.fatminmin.xposed.minminguard.Main;

/**
 * Created by fatminmin on 2015/10/27.
 */
//TODO Use newer XposedHelpers class, Fix formatting
public final class NameBlocking
{

    private static boolean matchBannerName(String clazzName, String banner, String bannerPrefix)
    {
        if (banner != null && banner.equals(clazzName))
        {
            return true;
        }

        return bannerPrefix != null && clazzName.startsWith(bannerPrefix);
    }

    // return adnetwork name
    private static Boolean isAdView(Context context, String pkgName, String clazzName)
    {
        // android widgets
        if (clazzName.startsWith("android"))
            return false;

        // corner case
        if (clazzName.startsWith("com.google.ads"))
            return true;

        for (Blocker blocker : Main.blockers)
        {
            String banner = blocker.getBanner();
            String bannerPrefix = blocker.getBannerPrefix();

            // prefix is used to detect adview obfuscate by proguard
            if (matchBannerName(clazzName, banner, bannerPrefix))
            {
                return true;
            }
        }
        return false;
    }

    private static Boolean isAdView(Context context, String pkgName, View view)
    {
        Class clazz = view.getClass();
        // find also parent classes
        int level = 1;

        for (int i = 0; i < level && clazz != null; i++)
        {
            String clazzName = clazz.getName();

            if (isAdView(context, pkgName, clazzName))
                return true;

            clazz = clazz.getSuperclass();
        }

        return false;
    }

    private static void clearAdViewInLayout(final String packageName, final View view)
    {

        if (isAdView(view.getContext(), packageName, view))
        {
            ViewBlocking.removeAdView(packageName, view);
            Util.log(packageName, "clearAdViewInLayout: " + view.getClass().getName());
        }

        if (view instanceof ViewGroup)
        {
            ViewGroup vg = (ViewGroup) view;

            for (int i = 0; i < vg.getChildCount(); i++)
            {
                clearAdViewInLayout(packageName, vg.getChildAt(i));
            }
        }
    }

    public static void nameBasedBlocking(final String pkgName, final XC_LoadPackage.LoadPackageParam lpparam)
    {
        Util.hookAllMethods("android.view.ViewGroup", lpparam.classLoader, "addView", new XC_MethodHook()
        {

            @Override
            protected void beforeHookedMethod(MethodHookParam param)
            {
                View view = (View) param.args[0];

                if (view != null && isAdView(view.getContext(), pkgName, view))
                {
                    Util.log(pkgName, "NameBasedBlocking before addView: " + view.getClass().getName());
                    ViewBlocking.removeAdView(pkgName, view);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param)
            {
                View view = (View) param.args[0];

                if (view != null && isAdView(view.getContext(), pkgName, view))
                {
                    Util.log(pkgName, "NameBasedBlocking after addView: " + view.getClass().getName());
                    ViewBlocking.removeAdView(pkgName, view);
                }
            }
        });

        Util.hookAllMethods("android.app.Activity", lpparam.classLoader, "setContentView", new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
            {
                Activity ac = (Activity) (param.thisObject);
                ViewGroup root = ac.getWindow().getDecorView().findViewById(android.R.id.content);

                clearAdViewInLayout(pkgName, root);
            }
        });

        Util.hookAllMethods("android.view.LayoutInflater", lpparam.classLoader, "inflate", new XC_MethodHook()
        {

            /*
              http://developer.android.com/intl/zh-tw/reference/android/view/LayoutInflater.html
              inflate(int resource, ViewGroup root)
              inflate(XmlPullParser parser, ViewGroup root)
              inflate(XmlPullParser parser, ViewGroup root, boolean attachToRoot)
              inflate(int resource, ViewGroup root, boolean attachToRoot)

                Returns
                The root View of the inflated hierarchy. If root was supplied and attachToRoot is true,
                this is root; otherwise it is the root of the inflated XML file.
             */

            @Override
            protected void afterHookedMethod(MethodHookParam param)
            {
                View root = (View) param.getResult();

                if (root != null)
                    clearAdViewInLayout(pkgName, root);
            }
        });
    }
}
