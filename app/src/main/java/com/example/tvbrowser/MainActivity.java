package com.example.tvbrowser;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionOverride;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.trackselection.TrackGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private WebView webView;
    private EditText urlInput;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ListView audioTrackListView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        urlInput = findViewById(R.id.urlInput);
        playerView = findViewById(R.id.playerView);
        audioTrackListView = findViewById(R.id.audioTrackListView);
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android TV; rv:89.0) Gecko/89.0 Firefox/89.0");
        
        webView.loadUrl("https://kinojump.com/88-luchshe-zvonite-solu.html");
        
        urlInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                String url = urlInput.getText().toString();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                if (url.endsWith(".mp4") || url.contains("stream") || url.endsWith(".m3u8") || url.endsWith(".mpd")) {
                    playVideo(url);
                } else {
                    webView.loadUrl(url);
                }
                return true;
            }
            return false;
        });
    }

    private void playVideo(String videoUrl) {
        webView.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
        
        TrackSelector trackSelector = new DefaultTrackSelector(this);
        player = new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
        playerView.setPlayer(player);
        
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer-test");
        MediaSource mediaSource;
        
        if (videoUrl.endsWith(".m3u8")) {
            mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)));
        } else if (videoUrl.endsWith(".mpd")) {
            mediaSource = new DashMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)));
        } else {
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(videoUrl)));
        }
        
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        showAudioTracks();
    }

    private void showAudioTracks() {
        List<String> audioTracks = new ArrayList<>();
        TrackGroup trackGroup = player.getCurrentTrackGroups().get(0);
        for (int i = 0; i < trackGroup.length; i++) {
            audioTracks.add("Audio Track " + (i + 1));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, audioTracks);
        audioTrackListView.setAdapter(adapter);
        audioTrackListView.setVisibility(View.VISIBLE);
        
        audioTrackListView.setOnItemClickListener((parent, view, position, id) -> {
            TrackSelectionOverride override = new TrackSelectionOverride(trackGroup, position);
            ((DefaultTrackSelector) player.getTrackSelector()).setParameters(
                    ((DefaultTrackSelector) player.getTrackSelector()).buildUponParameters().setTrackSelectionOverrides(override));
            Toast.makeText(this, "Switched to Audio Track " + (position + 1), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (playerView.getVisibility() == View.VISIBLE) {
                playerView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                player.release();
                return true;
            }
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
