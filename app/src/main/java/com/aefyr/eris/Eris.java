package com.aefyr.eris;

import android.annotation.SuppressLint;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;

public class Eris implements IXposedHookLoadPackage {

    @SuppressWarnings("unchecked")
    @SuppressLint("PrivateApi")
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        findAndHookConstructor("android.security.net.config.NetworkSecurityConfig$Builder", lpparam.classLoader, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //Getting UserCertificateSource singleton instance
                Class cUserCeritificateSource = Class.forName("android.security.net.config.UserCertificateSource");
                Object iUserCeritificateSource = cUserCeritificateSource.getDeclaredMethod("getInstance").invoke(null);

                //Creating new CertificatesEntryRef with UserCertificateSource instance as source
                Class cCertificatesEntryRef = Class.forName("android.security.net.config.CertificatesEntryRef");
                Object iCertificatesEntryRef = cCertificatesEntryRef.getConstructor(Class.forName("android.security.net.config.CertificateSource"), boolean.class).newInstance(iUserCeritificateSource, true);


                //Injecting that CertificatesEntryRef entry into NetworkSecurityConfig.Builder, so we'll always have UserCertificateSource as root certificates source
                Class cNetworkSecurityConfigBuilder = Class.forName("android.security.net.config.NetworkSecurityConfig$Builder");
                cNetworkSecurityConfigBuilder.getDeclaredMethod("addCertificatesEntryRef", cCertificatesEntryRef).invoke(param.thisObject, iCertificatesEntryRef);

                Log.d("Eris", "Injected into " + lpparam.packageName);
            }
        });
    }
}
