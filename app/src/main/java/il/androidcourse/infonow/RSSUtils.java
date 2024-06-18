package il.androidcourse.infonow;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RSSUtils {
    public static final String RSS_URL_ISRAEL_HAYOM = "https://www.israelhayom.co.il/rss.xml";
    public static final String RSS_URL_MAKO = "https://rcs.mako.co.il/rss/31750a2610f26110VgnVCM1000005201000aRCRD.xml";
    public static final String RSS_URL_HAARETZ = "https://www.haaretz.co.il/srv/rss---feedly ";
    public static final String PREFS_NAME = "internal";

    public static List<RSSItem> fetchRSSSource(String urlString, SimpleDateFormat format, SharedPreferences.Editor editor) throws Exception
    {
        // Fetch RSS feed
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();

        // Parse XML
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        List<RSSItem> items = null;

        if (urlString.equals(RSS_URL_ISRAEL_HAYOM))
            items = getIsraelHayomRssItems(parser, format, editor);
        else if (urlString.equals(RSS_URL_MAKO))
            items = getMakoRssItems(parser, format, editor);
        else if (urlString.equals(RSS_URL_HAARETZ))
            items = getHaaretzRssItems(parser, format, editor);

        inputStream.close();

        return items;
    }

    public static List<RSSItem> fetchRSSNewSource(String urlString, SimpleDateFormat format, SharedPreferences.Editor editor, int maxNotifications, Date lastPubDate) throws Exception
    {
        // Fetch RSS feed
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();

        // Parse XML
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        List<RSSItem> items = getIsraelHayomNewRssItems(parser, maxNotifications, format, lastPubDate, editor);

        inputStream.close();

        return items;
    }

    public static List<RSSItem> parseRSS(Context context) throws Exception {
        List<RSSItem> itemsAll = new ArrayList<>();

        SharedPreferences internalPreferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = internalPreferences.edit();
        String lastPublishedDateStr = internalPreferences.getString("lastPublishedDate", "default_value");
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        Date lastPublishedDate = lastPublishedDateStr.equals("default_value") ? null : format.parse(lastPublishedDateStr);

        if (internalPreferences.getBoolean("israelHayom", true))
            itemsAll.addAll(fetchRSSSource(RSS_URL_ISRAEL_HAYOM, format, editor));
        if (internalPreferences.getBoolean("mako", true))
            itemsAll.addAll(fetchRSSSource(RSS_URL_MAKO, format, editor));
        if (internalPreferences.getBoolean("haaretz", true))
            itemsAll.addAll(fetchRSSSource(RSS_URL_HAARETZ, format, editor));

        Comparator<RSSItem> comparator = Comparator.comparing(RSSItem::getPubDate);
        Collections.sort(itemsAll, comparator);
        Collections.reverse(itemsAll);
        return itemsAll;
    }

    private static @NonNull List<RSSItem> getIsraelHayomRssItems(XmlPullParser parser, SimpleDateFormat format, SharedPreferences.Editor editor) throws XmlPullParserException, IOException, ParseException {
        List<RSSItem> items = new ArrayList<>();
        boolean insideItem = false;
        RSSItem currentItem = null;
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
                    Date pubDate = format.parse(dateString);
                    currentItem.setPubDate(new Date(pubDate.getTime() - (3 * 60 * 60 * 1000)));
                } else if (parser.getName().equalsIgnoreCase("link") && insideItem) {
                    currentItem.setLink(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("image") && insideItem) {
                    currentItem.setImage(parser.nextText());
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                insideItem = false;

                if (currentItem != null && currentItem.getImage() != null && currentItem.getImage() != "")
                    items.add(currentItem);
                if (items.size() == 1) {
                    editor.putString("lastPublishedDate", format.format(currentItem.getPubDate()));
                    editor.apply();
                }
            }
            eventType = parser.next();
        }

        return items;
    }

    private static @NonNull List<RSSItem> getMakoRssItems(XmlPullParser parser, SimpleDateFormat format, SharedPreferences.Editor editor) throws XmlPullParserException, IOException, ParseException {
        List<RSSItem> items = new ArrayList<>();
        boolean insideItem = false;
        RSSItem currentItem = null;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equalsIgnoreCase("item")) {
                    insideItem = true;
                    currentItem = new RSSItem("", "", null, "", "");
                } else if (parser.getName().equalsIgnoreCase("title") && insideItem) {
                    currentItem.setTitle(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("shortDescription") && insideItem) {
                    currentItem.setDescription(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("pubDate") && insideItem) {
                    String dateString = parser.nextText();
                    Date pubDate = format.parse(dateString);
                    currentItem.setPubDate(pubDate);
                } else if (parser.getName().equalsIgnoreCase("link") && insideItem) {
                    currentItem.setLink(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("image435X329") && insideItem) {
                    currentItem.setImage(parser.nextText());
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                insideItem = false;

                if (currentItem != null && currentItem.getImage() != null && currentItem.getImage() != "")
                    items.add(currentItem);
                if (items.size() == 1) {
                    editor.putString("lastPublishedDate", format.format(currentItem.getPubDate()));
                    editor.apply();
                }
            }
            eventType = parser.next();
        }

        return items;
    }

    private static @NonNull List<RSSItem> getHaaretzRssItems(XmlPullParser parser, SimpleDateFormat format, SharedPreferences.Editor editor) throws XmlPullParserException, IOException, ParseException {
        List<RSSItem> items = new ArrayList<>();
        boolean insideItem = false;
        RSSItem currentItem = null;
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
                    Date pubDate = format.parse(dateString);
                    currentItem.setPubDate(pubDate);
                } else if (parser.getName().equalsIgnoreCase("link") && insideItem) {
                    currentItem.setLink(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("enclosure") && insideItem) {
                    String imageUrl = parser.getAttributeValue(null, "url");
                    String baseUrl = imageUrl.split("\\?")[0];

                    // Replace the width parameter with width=480
                    String modifiedWidthParam = "width=720";

                    // Construct the modified image URL
                    String modifiedImageUrl = baseUrl + "?" + modifiedWidthParam;
                    currentItem.setImage(modifiedImageUrl);
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                insideItem = false;

                if (currentItem != null && currentItem.getImage() != null && currentItem.getImage() != "")
                    items.add(currentItem);
                if (items.size() == 1) {
                    editor.putString("lastPublishedDate", format.format(currentItem.getPubDate()));
                    editor.apply();
                }
            }
            eventType = parser.next();
        }

        return items;
    }

    public static List<RSSItem> parseNewRSS(Context context, int max_notifications) throws Exception {
        List<RSSItem> itemsAll = new ArrayList<>();

        SharedPreferences internalPreferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = internalPreferences.edit();
        String lastPublishedDateStr = internalPreferences.getString("lastPublishedDate", "default_value");
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        Date lastPublishedDate = lastPublishedDateStr.equals("default_value") ? null : format.parse(lastPublishedDateStr);


        if (internalPreferences.getBoolean("israelHayom", true))
            itemsAll.addAll(fetchRSSNewSource(RSS_URL_ISRAEL_HAYOM, format, editor, max_notifications, lastPublishedDate));;

        Comparator<RSSItem> comparator = Comparator.comparing(RSSItem::getPubDate);
        Collections.sort(itemsAll, comparator);
        Collections.reverse(itemsAll);
        return itemsAll;
    }

    private static @NonNull List<RSSItem> getIsraelHayomNewRssItems(XmlPullParser parser, int max_notifications, SimpleDateFormat format, Date lastPublishedDate, SharedPreferences.Editor editor) throws XmlPullParserException, IOException, ParseException {
        List<RSSItem> items = new ArrayList<>();
        boolean insideItem = false;
        RSSItem currentItem = null;
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
                    Date pubDate = format.parse(dateString);
                    currentItem.setPubDate(pubDate);
                } else if (parser.getName().equalsIgnoreCase("link") && insideItem) {
                    currentItem.setLink(parser.nextText());
                } else if (parser.getName().equalsIgnoreCase("image") && insideItem) {
                    currentItem.setImage(parser.nextText());
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                insideItem = false;

                if (currentItem != null && lastPublishedDate != null && currentItem.getPubDate().compareTo(lastPublishedDate) <= 0)
                    break;

                if (currentItem != null && currentItem.getImage() != null && currentItem.getImage() != "")
                    items.add(currentItem);
                if (items.size() == 1) {
                    editor.putString("lastPublishedDate", format.format(currentItem.getPubDate()));
                    editor.apply();
                }

                if (items.size() >= max_notifications)      // limit the number of notifications at a time
                    break;
            }
            eventType = parser.next();
        }

        return items;
    }
}
