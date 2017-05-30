package game.is.life.videofilter.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import game.is.life.videofilter.R;

/**
 * Created by yzhao on 5/29/17.
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    private ArrayList<VideoListItem> mDataset;
    private Context context;

    interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        private CardView videoItem;
        private ImageView thumb;
        private TextView title;
        private TextView duration;
        private TextView format;
        private ItemClickListener clickListener;

        private VideoViewHolder(CardView v) {
            super(v);
            videoItem = v;
            videoItem.setOnClickListener(this); // add click listener
            thumb = (ImageView)itemView.findViewById(R.id.video_thumb);
            title = (TextView)itemView.findViewById(R.id.video_title);
            duration = (TextView)itemView.findViewById(R.id.video_duration);
            format = (TextView)itemView.findViewById(R.id.video_format);
        }

        private void setOnClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition(), false);
        }
    }

    public VideoListAdapter(ArrayList<VideoListItem> mDataset, Context context) {
        this.mDataset = mDataset;
        this.context = context;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView videoCardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_list_item, parent, false);
        return new VideoViewHolder(videoCardView);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Log.d("RecyclerAdapter", "holder at position " + position);
        VideoListItem video = mDataset.get(position);
        holder.thumb.setImageBitmap(video.getThumb());
        holder.title.setText(video.getTitle());
        holder.duration.setText(video.getDuration());
        holder.format.setText(video.getFormat());
        holder.setOnClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                VideoListItem item = mDataset.get(position);
                String fullPath = item.getFullPath();
                Uri videoUri = Uri.parse(fullPath);
                Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
                intent.setDataAndType(videoUri, "video/mp4");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
