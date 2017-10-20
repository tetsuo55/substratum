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

package projekt.substratum.common.analytics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import projekt.substratum.common.References;

@SuppressWarnings("AccessStaticViaInstance")
public class FirebaseAnalytics {

    public static final String NAMES_PREFS = "names";
    public static final String PACKAGES_PREFS = "prefs";
    private static FirebaseDatabase mDatabase;

    @SuppressLint("MissingFirebaseInstanceTokenRefresh")
    private static DatabaseReference getDatabaseReference() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d(References.SUBSTRATUM_LOG, "Firebase Registration Token: " + token);
        }
        return mDatabase.getReference();
    }

    @SuppressWarnings("unchecked")
    public static void withdrawBlacklistedPackages(Context context) {
        DatabaseReference database = getDatabaseReference();
        database.child("patchers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences.Editor editor = context
                        .getSharedPreferences(PACKAGES_PREFS, Context.MODE_PRIVATE).edit();
                editor.clear();
                Collection<String> listOfPackages = new ArrayList<>();
                Object dataValue = dataSnapshot.getValue();
                if (dataValue != null) {
                    String data = dataValue.toString();
                    String[] dataArr = data.substring(1, data.length() - 1).split(",");
                    for (String aDataArr : dataArr) {
                        String entry = aDataArr.split("=")[1];
                        listOfPackages.add(entry);
                    }

                    Set set = new HashSet();
                    set.addAll(listOfPackages);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.US);
                    editor.putStringSet(dateFormat.format(new Date()), set);
                    editor.apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void withdrawAndromedaFingerprint(Context context, int version) {
        SharedPreferences prefs = context
                .getSharedPreferences("substratum_state", Context.MODE_PRIVATE);
        String prefKey = "andromeda_exp_fp_" + version;
        if (!prefs.contains("andromeda_exp_fp_" + version)) {
            SharedPreferences.Editor editor = prefs.edit();
            for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
                if (entry.getKey().startsWith("andromeda_fp_")) {
                    editor.remove(entry.getKey());
                }
            }
            DatabaseReference database = getDatabaseReference();
            database.child("andromeda-fp")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Object dataValue = dataSnapshot.child(String.valueOf(version))
                                    .getValue();
                            if (dataValue != null) {
                                String hash = dataValue.toString();
                                editor.putString(prefKey, hash).apply();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }

    public static void withdrawSungstratumFingerprint(Context context, int version) {
        SharedPreferences prefs = context
                .getSharedPreferences("substratum_state", Context.MODE_PRIVATE);
        String prefKey = "sungstratum_exp_fp_" + version;
        if (!prefs.contains("sungstratum_exp_fp_" + version)) {
            SharedPreferences.Editor editor = prefs.edit();
            for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
                if (entry.getKey().startsWith("sungstratum_exp_fp_")) {
                    editor.remove(entry.getKey());
                }
            }
            DatabaseReference database = getDatabaseReference();
            database.child("sungstratum-fp")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Object dataValue = dataSnapshot.child(String.valueOf(version))
                                    .getValue();
                            if (dataValue != null) {
                                String hash = dataValue.toString();
                                editor.putString(prefKey, hash).apply();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }
}