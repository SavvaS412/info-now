package il.androidcourse.infonow;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RSSWorker extends Worker {

    private static final String TAG = "RSSWorker";
    private static final String CHANNEL_ID = "RSSChannel";
    private static final String RSS_URL = "https://www.israelhayom.co.il/rss.xml";
    private static final String PREFS_NAME = "internal";
    private SharedPreferences internalPreferences;

    public RSSWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        internalPreferences = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
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

            List<RSSItem> newItems = parseRSS(parser);

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

    private List<RSSItem> parseRSS(XmlPullParser parser) throws Exception {
        List<RSSItem> items = new ArrayList<>();
        boolean insideItem = false;
        RSSItem currentItem = null;
        SharedPreferences.Editor editor = internalPreferences.edit();
        String lastPublishedLink = internalPreferences.getString("lastPublishedLink", "default_value");

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equalsIgnoreCase("item")) {
                    insideItem = true;
                    currentItem = new RSSItem("", "", null, "", "");
                } else if (parser.getName().equalsIgnoreCase("title") && insideItem) {
                    currentItem.setTitle(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("description") && insideItem) {
                    currentItem.setDescription(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("pubDate") && insideItem) {
                    String dateString = parser.nextText();
                    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                    Date pubDate = format.parse(dateString);
                    currentItem.setPubDate(pubDate);
                } else if (parser.getName().equalsIgnoreCase("link") && insideItem) {
                    currentItem.setLink(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("image") && insideItem) {
                    currentItem.setImage(parser.nextText());
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                insideItem = false;

                if (currentItem != null && currentItem.getLink() == lastPublishedLink)
                {
                    editor.putString("lastPublishedLink", currentItem.getLink());
                    editor.apply();
                    break;
                }

                items.add(currentItem);
            }
            eventType = parser.next();
        }

        return items;
    }

    private void showNotifications(List<RSSItem> items) {
        for (RSSItem item : items) {
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
                .build();
    }
}

