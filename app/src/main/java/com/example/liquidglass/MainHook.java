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

    // If One UI 9 renamed this class, find the new name with jadx and edit here.
    private static final String[] BG_CLASSES = {
        "com.android.systemui.statusbar.notification.row.NotificationBackgroundView",
    };

    @Override
    public void handleLoadPackage(LoadPackageParam lp) {
        if (!"com.android.systemui".equals(lp.packageName)) return;

        Class<?> bg = null;
        for (String name : BG_CLASSES) {
            bg = XposedHelpers.findClassIfExists(name, lp.classLoader);
            if (bg != null) break;
        }
        if (bg == null) {
            XposedBridge.log(TAG + "notification background class not found");
            return;
        }

        // Give every new notification card a glass background.
        XposedBridge.hookAllConstructors(bg, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam p) {
                try {
                    View v = (View) p.thisObject;
                    XposedHelpers.callMethod(p.thisObject, "setCustomBackground",
                            (Drawable) new GlassDrawable(v.getContext()));
                } catch (Throwable t) { XposedBridge.log(TAG + t); }
            }
        });

        // Swap any background SystemUI tries to set (Drawable or resource id).
        XposedBridge.hookAllMethods(bg, "setCustomBackground", new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam p) {
                if (p.args.length != 1) return;
                View v = (View) p.thisObject;
                if (p.args[0] instanceof Drawable && !(p.args[0] instanceof GlassDrawable)) {
                    p.args[0] = new GlassDrawable(v.getContext());
                } else if (p.args[0] instanceof Integer) {
                    p.setResult(null);
                    XposedHelpers.callMethod(p.thisObject, "setCustomBackground",
                            (Drawable) new GlassDrawable(v.getContext()));
                }
            }
        });

        // Stop SystemUI from tinting the glass with solid colors.
        XposedBridge.hookAllMethods(bg, "setTint", XC_MethodReplacement.returnConstant(null));

        XposedBridge.log(TAG + "hooked " + bg.getName());
    }
}
