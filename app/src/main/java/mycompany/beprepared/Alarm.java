package mycompany.beprepared;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Matt on 4/12/2015.
 * This class defines the behavior of the alarm used by Timer, GPS, and Wifi mode.
 */
public class Alarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, NotificationScreenActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, i, 0);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setAutoCancel(true)
                        .setSound(uri)
                        .setVibrate(new long[]{500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500})
                        .setContentTitle("Be Prepared")
                        .setContentText("Heading out?");
        builder.setContentIntent(pIntent);
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}
