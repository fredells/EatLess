package fredells.eatless;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Fred on 2017-12-19.
 */
public class NotificationReceiver extends BroadcastReceiver {

    /*private int requestCode = 100;

    public void setRequestCode(int code) {
        this.requestCode = code;
    }*/

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "timeset").
                setSmallIcon(R.drawable.ic_launcher_background).
                setContentIntent(pendingIntent).
                setContentText("Remember to log your meal").
                setContentTitle("EatLess").

                setSound(alarmSound).
        setAutoCancel(true);
        notificationManager.notify(100, builder.build());

    }
}