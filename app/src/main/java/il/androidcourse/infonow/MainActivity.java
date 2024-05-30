package il.androidcourse.infonow;

import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 123;
    private static final String TAG = "MainActivity";
    private static final long INTERVAL = 1 * 60 * 1000; // 1 minutes
    private static final long SHORT_INTERVAL = 100; // 100 milliseconds
    private List<RSSItem> rssItems = new CopyOnWriteArrayList<>();
    private Handler mainHandler;
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private NewsFragment newsFragment;
    private Intent nextIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startProgressBar();

        new Handler().postDelayed(() -> {
            if (nextIntent != null){
                startActivity(nextIntent);
                finish();
            }

            RelativeLayout splash = findViewById(R.id.splash);
            splash.setVisibility(View.GONE);
        }, 3000); // Splash screen duration

        //FirebaseAuth auth = FirebaseAuth.getInstance();
        //if (auth.getCurrentUser() != null) {
        if (null != null)
            nextIntent = new Intent(MainActivity.this, AuthActivity.class);

        // Cancel all notifications when the app is opened
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }

        mainHandler = new Handler(Looper.getMainLooper());

        handlerThread = new HandlerThread("RSSHandlerThread");
        handlerThread.start();

        backgroundHandler = new Handler(handlerThread.getLooper());

        backgroundHandler.post(new FetchRSSItemsTask(true));
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

    private class FetchRSSItemsTask implements Runnable {
        private boolean firstRun;

        FetchRSSItemsTask(boolean firstRun) {
            this.firstRun = firstRun;
        }

        @Override
        public void run() {
            if (firstRun) {
                List<RSSItem> items = fetchRSSItems(true);
                rssItems.addAll(items);
                mainHandler.post(() -> {
                    while (!(newsFragment != null && rssItems != null)) {
                        try {
                            Thread.sleep(50); // Sleep for 50 milliseconds
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    newsFragment.setRSSItems(rssItems);
                });
                firstRun = false;
            } else {
                Log.d(TAG, "run: fetch New RSS Items");
                List<RSSItem> newItems = fetchRSSItems(false);
                mainHandler.post(() -> {
                    if (newsFragment != null && newItems != null) {
                        newsFragment.addRSSItems(newItems);
                    }
                });
            }
            backgroundHandler.postDelayed(this, INTERVAL);
        }
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quitSafely();
    }

    private void startProgressBar(){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animator.setDuration(3000); // Animation duration: 3 second
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
}