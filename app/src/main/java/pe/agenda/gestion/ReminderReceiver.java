package pe.agenda.gestion;
import android.app.*;import android.content.*;import android.os.Build;
public class ReminderReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){String ch="agenda_reminders";NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(ch,"Recordatorios de agenda",NotificationManager.IMPORTANCE_HIGH));Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,ch):new Notification.Builder(c);b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(i.getStringExtra("title")).setContentText("Próximo: "+i.getStringExtra("when")).setAutoCancel(true);try{nm.notify((int)(i.getLongExtra("id",1)%Integer.MAX_VALUE),b.build());}catch(SecurityException ignored){}}
}
