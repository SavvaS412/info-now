package il.androidcourse.infonow;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RSSItem {
    private String title;
    private String description;
    private Date pubDate;
    private String link;
    private String image;

    public RSSItem(String title, String description, Date pubDate, String link, String image) {
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.link = link;
        this.image = image;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public String getHourAndMinutes() {
        Calendar gmt3Calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"));
        gmt3Calendar.setTime(pubDate);
        gmt3Calendar.add(Calendar.HOUR_OF_DAY, -3);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        return dateFormat.format(gmt3Calendar.getTime());
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
