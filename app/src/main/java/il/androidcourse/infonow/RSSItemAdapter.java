package il.androidcourse.infonow;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RSSItemAdapter extends RecyclerView.Adapter<RSSItemAdapter.ViewHolder> {

    private List<RSSItem> rssItems = new ArrayList<>();
    private int itemSize;
    private Dialog dialog;

    public RSSItemAdapter(int itemSize) {
        this.itemSize = itemSize;
    }

    public void setRSSItems(List<RSSItem> items) {
        this.rssItems = items;
        notifyDataSetChanged();
    }

    public void addRSSItems(List<RSSItem> items) {
        for (RSSItem item: items) {
            this.rssItems.add(0, item);
        }
        notifyItemRangeInserted(0, items.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rss, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RSSItem item = rssItems.get(position);
        holder.titleView.setText(item.getTitle());
        holder.descriptionView.setText(item.getDescription());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                dialog = new Dialog(context);
                dialog.setContentView(R.layout.item_rss_dialog_box);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); // 340 x 680
                dialog.setCancelable(true);

                ((TextView)dialog.findViewById(R.id.dialogTitleView)).setText(item.getTitle());
                ((TextView)dialog.findViewById(R.id.dialogDescriptionView)).setText(item.getDescription());
                dialog.findViewById(R.id.dialogButtonContinue).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
                        httpIntent.setData(Uri.parse(item.getLink()));

                        context.startActivity(httpIntent);
                    }

                });
                dialog.findViewById(R.id.dialogButtonClose).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }

                });
                new ImageLoadTask(item.getImage(), ((ImageView)dialog.findViewById(R.id.dialogImg))).execute();
                dialog.show();
            }

        });
        new ImageLoadTask(item.getImage(), holder.imageView).execute();
    }

    @Override
    public int getItemCount() {
        return rssItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView descriptionView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleView = itemView.findViewById(R.id.titleView);
            descriptionView = itemView.findViewById(R.id.descriptionView);
        }
    }

    // AsyncTask to load images from the internet
    private static class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
