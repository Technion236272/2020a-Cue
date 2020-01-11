package com.technion.cue;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.technion.cue.BusinessFeatures.BOBusinessHomePage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.technion.cue.FirebaseCollections.APPOINTMENTS_COLLECTION;
import static com.technion.cue.FirebaseCollections.BUSINESSES_COLLECTION;
import static com.technion.cue.FirebaseCollections.TYPES_COLLECTION;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // reminder on appointment
        if (remoteMessage.getData().containsKey("business_name")) {

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

            FirebaseFirestore.getInstance()
                    .collection(BUSINESSES_COLLECTION)
                    .document(FirebaseAuth.getInstance().getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        DateFormat formattingDateFormat = new SimpleDateFormat("EE, dd/MM, HH:mm");
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(Long.valueOf(remoteMessage.getData().get("appointment_date")));
                        String old_date = formattingDateFormat.format(c.getTime());
                        c.setTimeInMillis(Long.valueOf(remoteMessage.getData().get("new_appointment_date")));
                        String new_date = formattingDateFormat.format(c.getTime());

                        if (snapshot.exists()) {
                            if (remoteMessage.getData().get("action_doer").equals("business"))
                                return;
                            switch (remoteMessage.getData().get("action_type")) {
                                case "scheduling":
                                    bigTextStyle.setBigContentTitle(
                                            remoteMessage.getData().get("client_name")
                                                    + " has scheduled an appointment!"
                                    );
                                    bigTextStyle.bigText(
                                            remoteMessage.getData().get("client_name")
                                                    + " has scheduled an appointment of type "
                                                    + remoteMessage.getData().get("appointment_type") + " on "
                                                    + old_date
                                    );
                                    break;
                                case "rescheduling":
                                    bigTextStyle.setBigContentTitle(
                                            remoteMessage.getData().get("client_name")
                                                    + " has rescheduled an appointment!"
                                    );
                                    bigTextStyle.bigText(
                                            remoteMessage.getData().get("client_name")
                                                    + " has rescheduled an appointment from "
                                                    + old_date + " to " + new_date
                                    );
                                    break;
                                case "cancellation":
                                    bigTextStyle.setBigContentTitle(
                                            remoteMessage.getData().get("client_name")
                                                    + " has canceled an appointment"
                                    );
                                    bigTextStyle.bigText(
                                            remoteMessage.getData().get("client_name")
                                                    + " has canceled an appointment that was due to happen on "
                                                    + old_date
                                    );
                                    break;
                            }

                            Notification notification =
                                    new NotificationCompat.Builder(getApplicationContext(), "0")
                                            .setSmallIcon(R.drawable.business_icon)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setStyle(bigTextStyle)
                                            .build();
                            mNotificationManager.notify(0, notification);
                        } else {
//                            if (remoteMessage.getData().get("action_doer").equals("client"))
//                                return;
                            switch (remoteMessage.getData().get("action_type")) {
                                case "scheduling":
                                    bigTextStyle.setBigContentTitle(
                                            remoteMessage.getData().get("business_name")
                                                    + " has scheduled an appointment for you!"
                                    );
                                    bigTextStyle.bigText(
                                            remoteMessage.getData().get("business_name")
                                                    + " has scheduled for you an appointment of type "
                                                    + remoteMessage.getData().get("appointment_type") + " on "
                                                    + old_date
                                    );
                                    break;
                                case "rescheduling":
                                    bigTextStyle.setBigContentTitle(
                                            remoteMessage.getData().get("business_name")
                                                    + " has rescheduled an appointment for you!"
                                    );
                                    bigTextStyle.bigText(
                                            remoteMessage.getData().get("business_name")
                                                    + " has rescheduled for you an appointment from"
                                                    + old_date + " to " + new_date
                                    );
                                    break;
                                case "cancellation":
                                    bigTextStyle.setBigContentTitle(
                                            remoteMessage.getData().get("business_name")
                                                    + " has canceled an appointment for you!"
                                    );
                                    bigTextStyle.bigText(
                                            remoteMessage.getData().get("business_name")
                                                    + " has canceled for you an appointment that was due to happen on"
                                                    + old_date
                                    );
                                    break;
                            }

                            Notification notification =
                                    new NotificationCompat.Builder(getApplicationContext(), "0")
                                            .setSmallIcon(R.drawable.business_icon)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setStyle(bigTextStyle)
                                            .build();
                            mNotificationManager.notify(0, notification);

                            Intent alarmIntent = new Intent(this, AlarmReceiver.class);

                            String ap_type = remoteMessage.getData()
                                    .get("action_type").equals("scheduling") ?
                                    remoteMessage.getData().get("appointment_type") :
                                    remoteMessage.getData().get("new_appointment_type");

                            String ap_date = remoteMessage.getData()
                                    .get("action_type").equals("scheduling") ?
                                    remoteMessage.getData().get("appointment_date") :
                                    remoteMessage.getData().get("new_appointment_date");

                            c.setTimeInMillis(Long.valueOf(ap_date));

                            alarmIntent.putExtra("business", remoteMessage.getData().get("business_name"));
                            alarmIntent.putExtra("notes", remoteMessage.getData().get("notes"));
                            alarmIntent.putExtra("type", ap_type);
                            alarmIntent.putExtra("date", formattingDateFormat.format(c.getTime()));

                            c.add(Calendar.DAY_OF_WEEK, -1);


                            PendingIntent pendingIntent =
                                    PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
                            AlarmManager alarmManager =
                                    (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);

                            if (remoteMessage.getData().get("action_type").equals("cancellation"))
                                alarmManager.cancel(pendingIntent);
                            else if (remoteMessage.getData().get("action_type").equals("rescheduling")) {
                                alarmManager.cancel(pendingIntent);
                                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                            } else {
                                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                            }
                        }
                    });


        } else {
            Intent intent = new Intent(this, BOBusinessHomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Notification notification =
                    new NotificationCompat.Builder(getApplicationContext(), "0")
                            .setSmallIcon(R.drawable.business_icon)
                            .setContentTitle("A client has posted a new review in your business page!")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .build();
            mNotificationManager.notify(0, notification);
        }
    }
}
