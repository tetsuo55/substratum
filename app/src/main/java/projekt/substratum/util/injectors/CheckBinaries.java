/*
 * Copyright (c) 2016-2017 Projekt Substratum
 * This file is part of Substratum.
 *
 * Substratum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Substratum is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Substratum.  If not, see <http://www.gnu.org/licenses/>.
 */

package projekt.substratum.util.injectors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import projekt.substratum.common.References;
import projekt.substratum.common.commands.FileOperations;

public enum CheckBinaries {
    ;

    public static void install(Context context, Boolean forced) {
        injectAOPT(context, forced);
        injectZipAlign(context, forced);
    }

    private static void injectAOPT(Context context, Boolean forced) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String aoptPath = context.getFilesDir().getAbsolutePath() + "/aopt";

        // Check if AOPT is installed on the device
        File aopt = new File(aoptPath);

        if (!aopt.isFile() || forced) {
            inject(context, prefs, aoptPath);
        } else if (aopt.exists()) {
            Log.d(References.SUBSTRATUM_LOG,
                    "The system partition already contains an existing compiler " +
                            "and Substratum is locked and loaded!");
        } else {
            Log.e(References.SUBSTRATUM_LOG,
                    "The system partition already contains an existing compiler, " +
                            "however it does not match Substratum integrity.");
            inject(context, prefs, aoptPath);
        }
    }

    private static void inject(Context mContext, SharedPreferences prefs, String aoptPath) {
        if (!Arrays.toString(Build.SUPPORTED_ABIS).contains("86")) {
            // Developers: AOPT-ARM (32bit) is using the legacy AAPT binary, while AAPT-ARM64
            //             (64bit) is using the brand new AOPT binary.
            String architecture =
                    Arrays.asList(Build.SUPPORTED_64_BIT_ABIS).size() > 0 ? "ARM64" : "ARM";
            String integrityCheck = prefs.getString("compiler", "aapt");
            try {
                if (integrityCheck.equals("aopt")) {
                    FileOperations.copyFromAsset(mContext, "aopt" + (architecture.equals("ARM64")
                            ? "64" :
                            ""), aoptPath);
                    Log.d(References.SUBSTRATUM_LOG,
                            "Android Overlay Packaging Tool (" + architecture + ") " +
                                    "has been added into the compiler directory.");
                } else {
                    FileOperations.copyFromAsset(mContext, "aapt", aoptPath);
                    Log.d(References.SUBSTRATUM_LOG,
                            "Android Asset Packaging Tool (" + architecture + ") " +
                                    "has been added into the compiler directory.");
                }
            } catch (Exception e) {
                // Suppress warning
            }
        } else {
            // Take account for x86 devices
            try {
                FileOperations.copyFromAsset(mContext, "aapt86", aoptPath);
                Log.d(References.SUBSTRATUM_LOG,
                        "Android Asset Packaging Tool (x86) " +
                                "has been added into the compiler directory.");
            } catch (Exception e) {
                // Suppress warning
            }
        }
        File f = new File(aoptPath);
        if (f.isFile()) {
            if (!f.setExecutable(true, true)) Log.e("CheckBinaries", "Could not set executable...");
        }
    }

    private static void injectZipAlign(Context mContext, Boolean forced) {
        String zipalignPath = mContext.getFilesDir().getAbsolutePath() + "/zipalign";
        File f = new File(zipalignPath);

        // Check if ZipAlign is already installed
        if (f.exists() && !forced)
            return;

        if (!Arrays.toString(Build.SUPPORTED_ABIS).contains("86")) {
            String architecture =
                    Arrays.asList(Build.SUPPORTED_64_BIT_ABIS).size() > 0 ? "ARM64" : "ARM";
            FileOperations.copyFromAsset(mContext, "zipalign" + (architecture.equals("ARM64") ?
                    "64" :
                    ""), zipalignPath);
            Log.d(References.SUBSTRATUM_LOG,
                    "ZipAlign (" + architecture + ") " +
                            "has been added into the compiler directory.");
        } else {
            // Take account for x86 devices
            try {
                FileOperations.copyFromAsset(mContext, "zipalign86", zipalignPath);
                Log.d(References.SUBSTRATUM_LOG,
                        "ZipAlign (x86) " +
                                "has been added into the compiler directory.");
            } catch (Exception e) {
                // Suppress warning
            }
        }

        if (f.isFile()) {
            if (!f.setExecutable(true, true)) Log.e("CheckBinaries", "Could not set executable...");
        }
    }
}
