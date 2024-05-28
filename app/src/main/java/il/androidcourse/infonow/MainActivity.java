package il.androidcourse.infonow;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 123;
    private static final String TAG = "MainActivity";
    private List<RSSItem> rssItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        home();

        // Define constraints for the work
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Require network connectivity
                .build();

        // Create a periodic work request
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(RSSWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        // Enqueue the periodic work request
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RSSWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );

        // Cancel all notifications when the app is opened
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    private void home() {
        NewsFragment newsFragment = new NewsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newsFragment);
        transaction.commit();
        
        newsFragment.setRSSItems(rssItems);
    }

    private List<RSSItem> fetchRSSItems() {
        List<RSSItem> items;
        try {
            // Fetch RSS feed
            URL url = new URL("https://www.israelhayom.co.il/rss.xml");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            // Parse XML
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            items = RSSUtils.parseRSS(parser);

            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching RSS feed: " + e.getMessage(), e);
            return Collections.<RSSItem>emptyList();
        }
        return items;
    }


}