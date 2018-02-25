package me.mehdi.mapsforgehello;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * You may not use this file unless you agree to be bound to the terms of this license.
 * Note that this is closed-source. So, unless you have received a written permission from the owner
 * of the project, you may not use this file.
 * Copyright (C) 2017 - Kavosh Corporation
 * All rights reserved
 * Created by Mehdi Haghgoo on 2/25/18.
 */

public abstract class MyUtil {
    public static boolean hasLocationPermission(Context ctx) {
        int granted = PackageManager.PERMISSION_GRANTED;
        return (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == granted &&
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == granted);
    }
}
