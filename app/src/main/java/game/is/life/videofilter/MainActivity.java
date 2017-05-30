package game.is.life.videofilter;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import game.is.life.videofilter.adapter.VideoListAdapter;
import game.is.life.videofilter.adapter.VideoListItem;

public class MainActivity extends AppCompatActivity {

    private FileIO fileIO;
    private static final String TAG = "MainActivity";
    private ArrayList<VideoListItem> videos;
    private RecyclerView videoList;
    private TextView placeHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileIO = FileIO.getInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        placeHolder = (TextView) findViewById(R.id.place_holder);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                startActivity(new Intent(getApplicationContext(),CameraFilterActivity.class));
            }
        });

        videos = new ArrayList<>();
        if (loadFiles() == 0){
            placeHolder.setVisibility(View.VISIBLE);
        }else {
            placeHolder.setVisibility(View.GONE);
        }

        videoList = (RecyclerView) findViewById(R.id.video_list);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        videoList.setHasFixedSize(true);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        videoList.setLayoutManager(llm);
        videoList.setAdapter(new VideoListAdapter(videos,getApplicationContext()));
    }

    private int loadFiles(){
        File appFolder = fileIO.getAppFolder();
        File[] videoFiles = appFolder.listFiles();
        videos.clear();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        // adding all video files to the list
        for (File videoFile: videoFiles){
            String filenameArray[] = videoFile.toString().split("\\.");
            String extension = filenameArray[filenameArray.length-1];
            if (!extension.equals("mp4")){
                continue;
            }else {
                retriever.setDataSource(videoFile.getAbsolutePath());
                String filePathArray[] = videoFile.toString().split("/");

                String durationMs = retriever.
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int seconds = (Integer.valueOf(durationMs) / 1000) % 60;
                int minutes = (int) ((Integer.valueOf(durationMs) / (1000*60)) % 60);

                String duration;
                if (minutes != 0) {
                    duration = String.format("%d min %d sec", minutes, seconds);
                }else{
                    duration = String.format("%d sec", seconds);
                }

                VideoListItem videoListItem = new VideoListItem();
                videoListItem.setTitle(filePathArray[filePathArray.length - 1]);
                videoListItem.setFormat(extension);
                videoListItem.setDuration(duration);
                videoListItem.setThumb(retriever.getFrameAtTime (0));
                videoListItem.setFullPath(videoFile.toString());
                videos.add(videoListItem);
            }
        }

        return videos.size();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity","onResume");
        if (loadFiles() == 0){
            placeHolder.setVisibility(View.VISIBLE);
        }else {
            placeHolder.setVisibility(View.GONE);
        } //updates list when back from the camera activity
        videoList.getAdapter().notifyDataSetChanged();
    }
}
