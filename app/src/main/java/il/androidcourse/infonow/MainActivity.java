package il.androidcourse.infonow;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
    private static final long INTERVAL = 1 * 60 * 1000; // 1 minutes
    private static final long SHORT_INTERVAL = 100; // 100 milliseconds
    public List<RSSItem> rssItems;
    private Handler handler;
    private Runnable rssUpdater;
    private NewsFragment newsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Cancel all notifications when the app is opened
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        handler = new Handler();
        new Thread(new Runnable() {
            private boolean firstRun = true;

            @Override
            public void run() {
                if (firstRun) {
                    rssItems = fetchRSSItems(true);
                    firstRun = false;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            while (newsFragment == null || rssItems == null) {      // Wait until newsFragment & rssItems are not null
                                try {
                                    Thread.sleep(SHORT_INTERVAL);                   // 100 milliseconds delay
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            newsFragment.setRSSItems(rssItems);
                        }
                    });
                } else {
                    Log.d(TAG, "run: fetch New RSS Items");
                    List<RSSItem> newRSSItems = fetchRSSItems(false);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (newsFragment != null && rssItems != null) {      // Wait until newsFragment & rssItems are not null
                                newsFragment.addRSSItems(rssItems);
                            }
                        }
                    });
                }

                handler.postDelayed(this, INTERVAL);
            }
        }).start();

        home();
    }

    private void home() {
        newsFragment = new NewsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newsFragment);
        transaction.commit();
    }

    private List<RSSItem> fetchRSSItems(boolean allItems) {
        List<RSSItem> items;
        try {
            // Fetch RSS feed
            URL url = new URL(RSSUtils.RSS_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            // Parse XML
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            Context context = MainActivity.this; // Use the appropriate context here
            if (allItems)
                items = RSSUtils.parseRSS(parser, context);
            else
                items = RSSUtils.parseNewRSS(parser, context, 999);

            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching RSS feed: " + e.getMessage(), e);
            return Collections.<RSSItem>emptyList();
        }
        return items;
    }

    @Override
    protected void onStop() {
        super.onStop();

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

        Log.d(TAG, "run: start RSSWorker");
    }
}