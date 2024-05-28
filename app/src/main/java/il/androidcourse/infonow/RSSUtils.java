package il.androidcourse.infonow;

import android.content.SharedPreferences;

import org.xmlpull.v1.XmlPullParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RSSUtils {
    public static List<RSSItem> parseRSS(XmlPullParser parser) throws Exception {
        List<RSSItem> items = new ArrayList<>();
        boolean insideItem = false;
        RSSItem currentItem = null;
//        SharedPreferences.Editor editor = internalPreferences.edit();
//        String lastPublishedLink = internalPreferences.getString("lastPublishedLink", "default_value");

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

                items.add(currentItem);
//                if (items.size() == 1) {
//                    editor.putString("lastPublishedLink", currentItem.getLink());
//                    editor.apply();
//                }
            }
            eventType = parser.next();
        }

        return items;
    }
}
