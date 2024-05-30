package il.androidcourse.infonow;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        for (RSSItem item : items) {
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
        holder.timeView.setText(item.getHourAndMinutes());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                dialog = new Dialog(context);
                dialog.setContentView(R.layout.item_rss_dialog_box);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); // 340 x 680
                dialog.setCancelable(true);

                ((TextView) dialog.findViewById(R.id.dialogTitleView)).setText(item.getTitle());
                ((TextView) dialog.findViewById(R.id.dialogDescriptionView)).setText(item.getDescription());
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

                // Load image using Glide
                ImageView dialogImageView = dialog.findViewById(R.id.dialogImg);
                Glide.with(context)
                        .load(item.getImage())
                        .placeholder(R.drawable.loading) // Add a placeholder image if you have one
                        .error(R.drawable.error) // Add an error image if you have one
                        .into(dialogImageView);

                dialog.show();
            }

        });

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(item.getImage())
                .placeholder(R.drawable.loading) // Add a placeholder image if you have one
                .error(R.drawable.error) // Add an error image if you have one
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return rssItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView descriptionView;
        TextView timeView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleView = itemView.findViewById(R.id.titleView);
            descriptionView = itemView.findViewById(R.id.descriptionView);
            timeView = itemView.findViewById(R.id.timeView);
        }
    }
}
