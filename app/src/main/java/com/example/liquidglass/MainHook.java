package com.example.liquidglass;

import android.graphics.drawable.Drawable;
import android.view.View;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "[LiquidGlass] ";

    private static void setGlass(View v) {
        try {
            XposedHelpers.callMethod(v, "setCustomBackground",
                (Drawable) new GlassDrawable(v.getContext()));
        } catch (Throwable t) { XposedBridge.log(TAG + t); }
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lp) {
        if (!"com.android.systemui".equals(lp.packageName)) return;
        try {
            Class<?> bg = XposedHelpers.findClassIfExists(
                "com.android.systemui.statusbar.notification.row.NotificationBackgroundView",
                lp.classLoader);
            if (bg == null) { XposedBridge.log(TAG + "class not found"); return; }

            XposedBridge.hookAllMethods(bg, "setCustomBackground", new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam p) {
                    try {
                        if (p.args.length == 1 && p.args[0] instanceof Drawable
                                && !(p.args[0] instanceof GlassDrawable)) {
                            p.args[0] = new GlassDrawable(((View) p.thisObject).getContext());
                        }
                    } catch (Throwable t) { XposedBridge.log(TAG + t); }
                }
            });

            XposedBridge.hookAllMethods(bg, "setTint", XC_MethodReplacement.returnConstant(null));

            XposedBridge.hookAllConstructors(bg, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam p) {
                    final View v = (View) p.thisObject;
                    v.post(new Runnable() { @Override public void run() { setGlass(v); } });
                }
            });
            XposedBridge.log(TAG + "hooked OK");
        } catch (Throwable t) { XposedBridge.log(TAG + "init failed: " + t); }
    }
}
