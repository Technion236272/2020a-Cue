package com.technion.cue;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmService extends JobIntentService {

    public static final int JOB_ID = 1;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, AlarmService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), "0")
                        .setSmallIcon(R.drawable.business_icon)
                        .setContentTitle("Meeting Tomorrow")
                        .setContentText("you have a meeting tomorrow")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mNotificationManager.notify(0, builder.build());
    }

}
