package il.androidcourse.infonow;
import static il.androidcourse.infonow.RSSUtils.PREFS_NAME;
import static il.androidcourse.infonow.RSSUtils.RSS_URL;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RSSWorker extends Worker {

    private static final String TAG = "RSSWorker";
    private static final String CHANNEL_ID = "RSSChannel";

    private static final int MAX_NOTIFICATIONS = 10;

    public RSSWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "RSSWorker started");

        try {
            // Fetch RSS feed
            URL url = new URL(RSS_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            // Parse XML
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            Context context = getApplicationContext(); // Use the appropriate context here
            List<RSSItem> newItems = RSSUtils.parseNewRSS(parser, context, MAX_NOTIFICATIONS);

            if (!newItems.isEmpty()) {
                showNotifications(newItems);
            }

            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching RSS feed: " + e.getMessage(), e);
            return Result.failure();
        }

        Log.d(TAG, "RSSWorker completed");
        return Result.success();
    }

    private void showNotifications(List<RSSItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (RSSItem item : items)  {
            Bitmap bitmap = getBitmapFromUrl(item.getImage());
            if (bitmap != null) {
                Notification notification = createNotification(item.getTitle(), bitmap);
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.notify((int) System.currentTimeMillis(), notification);
                }
            }
        }
    }

    private Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            Log.d(TAG, "downloading image from: " + imageUrl);
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            Log.e(TAG, "Error downloading image: " + e.getMessage(), e);
            return null;
        }
    }

    private Notification createNotification(String title, Bitmap image) {

        Context context = getApplicationContext();
        Intent intent = new Intent(context, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "RSS Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(image)
                .setContentIntent(pendingIntent) // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true) // Automatically remove the notification when it is tapped
                .build();
    }
}

