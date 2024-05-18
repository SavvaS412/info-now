package il.androidcourse.infonow;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class RSSService extends Service {
    private static final String CHANNEL_ID = "RSSServiceChannel";
    private static final String RSS_URL = "https://www.israelhayom.co.il/rss.xml";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, createNotification("Fetching latest news..."));
        new Thread(new RSSFetcher()).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "RSS Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("InfoNow Service")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher) //R.drawable.ic_notification
                .setContentIntent(pendingIntent)
                .build();
    }

    private void showNotification(String title, String content) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher) //R.drawable.ic_notification
                .build();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), notification);
        }
    }

    private class RSSFetcher implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    URL url = new URL(RSS_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = connection.getInputStream();

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = factory.newPullParser();
                    parser.setInput(inputStream, null);

                    List<String> titles = new ArrayList<>();
                    boolean insideItem = false;

                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (parser.getName().equalsIgnoreCase("item")) {
                                insideItem = true;
                            } else if (parser.getName().equalsIgnoreCase("title") && insideItem) {
                                titles.add(parser.nextText());
                            }
                        } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                            insideItem = false;
                        }
                        eventType = parser.next();
                    }

                    for (String title : titles) {
                        showNotification("Info Now", title);
                    }

                    inputStream.close();
                    Thread.sleep(60000); // Wait for 1 minute before fetching again
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}