package com.example.timlineview;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.squareup.picasso.Picasso;
import com.video.timeline.ImageLoader;
import com.video.timeline.ScrollableTimelineGlView;
import com.video.timeline.TimelineGlSurfaceView;
import com.video.timeline.VideoTimeLine;

import java.io.File;

public class NewActivity extends AppCompatActivity implements View.OnClickListener {
    private SimpleExoPlayer player;

    private VideoTimeLine videoTimeLine;
    private VideoTimeLine fixedVideoTimeline;

    private ImageLoader picassoLoader = (thumbPath, view) -> Picasso.get().load(Uri.fromFile(new File(thumbPath)))
            .placeholder(new ColorDrawable(Color.LTGRAY)).into(view);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String fileUri = getIntent().getStringExtra("file_uri");
        setContentView(R.layout.preview3);
        findViewById(R.id.show_fixed).setOnClickListener(this);
        findViewById(R.id.show_list).setOnClickListener(this);

        PlayerView playerView = findViewById(R.id.playerView);

        MediaSource mediaSource = new ProgressiveMediaSource.Factory
                (new DefaultDataSourceFactory(this, "geo"), new DefaultExtractorsFactory())
                .createMediaSource(URLUtil.isNetworkUrl(fileUri) ? Uri.parse(fileUri) : Uri.fromFile(new File(fileUri)));

        player = new SimpleExoPlayer.Builder(this).build();
        player.prepare(mediaSource);
        playerView.setPlayer(player);

        ScrollableTimelineGlView scrollableTimelineGlView = findViewById(R.id.thumb_list);
        videoTimeLine = VideoTimeLine.with(fileUri)
                .setImageLoader(picassoLoader)
                .setFrameDuration(2)
                .setFrameSizeDp(50)
                .setSeekParams(SeekParameters.CLOSEST_SYNC)
                .into(scrollableTimelineGlView);

        RecyclerView rv = scrollableTimelineGlView.getRecyclerView();

        rv.setClipToPadding(false);
        rv.setPadding(Android.dpToPx(this, 160), 0, Android.dpToPx(this, 160), 0);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    long seekPos = (long) (rv.computeHorizontalScrollOffset() * 1F / rv.computeHorizontalScrollRange()
                            * player.getDuration());
                    player.seekTo(seekPos);
                }
            }
        });


        TimelineGlSurfaceView glSurfaceView = findViewById(R.id.fixed_thumb_list);
        fixedVideoTimeline = VideoTimeLine.with(fileUri)
                .into(glSurfaceView);

        playerView.getVideoSurfaceView().setOnClickListener(v -> player.setPlayWhenReady(!player.getPlayWhenReady()));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
        if (videoTimeLine != null) {
            videoTimeLine.destroy();
        }

        if (fixedVideoTimeline != null) {
            fixedVideoTimeline.destroy();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.show_fixed) {
            fixedVideoTimeline.start();
        } else if (v.getId() == R.id.show_list) {
            videoTimeLine.start();
        }
    }
}
