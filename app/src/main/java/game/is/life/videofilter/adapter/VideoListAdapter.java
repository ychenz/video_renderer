package game.is.life.videofilter.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import game.is.life.videofilter.FileIO;
import game.is.life.videofilter.PlayerActivity;
import game.is.life.videofilter.R;

/**
 * Created by yzhao on 5/29/17.
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> implements ItemTouchHelperAdapter{

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
        private ImageButton shareBotton;

        private VideoViewHolder(CardView v) {
            super(v);
            videoItem = v;
            videoItem.setOnClickListener(this); // add click listener
            thumb = (ImageView)itemView.findViewById(R.id.video_thumb);
            title = (TextView)itemView.findViewById(R.id.video_title);
            duration = (TextView)itemView.findViewById(R.id.video_duration);
            format = (TextView)itemView.findViewById(R.id.video_format);
            shareBotton = (ImageButton)itemView.findViewById(R.id.share_btn);
            shareBotton.setOnClickListener(this);
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
    public void onBindViewHolder(VideoViewHolder holder,int position) {
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
                // todo Use file uri instead of String here
                File sdCard = Environment.getExternalStorageDirectory();
                File file = new File(sdCard, File.separator + FileIO.getAppFolderName()
                        + File.separator + item.getTitle());
                Uri videoUri;

                if (Build.VERSION.SDK_INT > 23) {
                    videoUri = FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName() + ".provider", file);
                }else
                    videoUri = Uri.fromFile(file);
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("videoUri",videoUri.toString());
                context.startActivity(intent);
            }
        });

        final int adapterPosistion = holder.getAdapterPosition();
        holder.shareBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoListItem item = mDataset.get(adapterPosistion);
                File sdCard = Environment.getExternalStorageDirectory();
                File file = new File(sdCard, File.separator + FileIO.getAppFolderName()
                        + File.separator + item.getTitle());
                Uri videoUri;
                if (Build.VERSION.SDK_INT > 23) {
                    videoUri = FileProvider.getUriForFile(context,
                            context.getApplicationContext().getPackageName() + ".provider", file);
                }else
                    videoUri = Uri.fromFile(file);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
                shareIntent.setType("video/mp4");
                context.startActivity(Intent.createChooser(shareIntent, "Share video to.."));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mDataset, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mDataset, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        VideoListItem item = mDataset.get(position);
        File sdCard = Environment.getExternalStorageDirectory();
        File file = new File(sdCard, File.separator + FileIO.getAppFolderName()
                + File.separator + item.getTitle());

        if (file.delete()){
            mDataset.remove(position);
            notifyItemRemoved(position);
        }
    }
}
