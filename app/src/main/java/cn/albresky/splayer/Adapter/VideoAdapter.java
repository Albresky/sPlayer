package cn.albresky.splayer.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.albresky.splayer.Bean.Video;
import cn.albresky.splayer.R;
import cn.albresky.splayer.Utils.Converter;
import cn.albresky.splayer.Utils.DatetimeUtils;
import cn.albresky.splayer.Utils.VideoScanner;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private static List<Video> mVideos;
    private static List<Bitmap> mVideoThumbnails;
    private final String TAG = "VideoAdapter";
    private Context mContext;

    private OnItemClickListener onItemClickListener;

    public VideoAdapter(List<Video> videos, @NonNull Context context) {
        mVideos = videos;
        mContext = context;
        onItemClickListener = (OnItemClickListener) context;

        // prepare video thumbnails
        mVideoThumbnails = new ArrayList<>();
        getThumbnails();
    }

    private void getThumbnails() {
        try {
            for (Video video : mVideos) {
                mVideoThumbnails.add(VideoScanner.getVideoThumbnail(video.getPath(), video.getWidth() / 10, video.getHeight() / 10));
            }
        } catch (Exception e) {
            Log.d(TAG, "getThumbnails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_video, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("TAG", "onBindViewHolder: " + position);
        Video video = mVideos.get(position);
        int d = video.getDuration();
        Bitmap thumbnail = mVideoThumbnails.get(position);
        String duration = DatetimeUtils.formatTime(d);

        if (thumbnail == null) {
            holder.videoWarning.setVisibility(View.VISIBLE);
        }
        holder.videoIndex.setText(String.valueOf(position + 1));
        holder.videoName.setText(video.getName());
        holder.videoPath.setText(video.getPath());
        holder.videoSize.setText(Converter.sizeConvert(video.getSize()));
        holder.videoResolution.setText(Converter.resolutionConvert(video.getWidth(), video.getHeight()));
        holder.videoDuration.setText(duration);
        holder.videoType.setText(video.getType());
        holder.videoThumbnail.setImageBitmap(thumbnail);
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(mContext, "点击了第" + position + "项", Toast.LENGTH_SHORT).show();
            onItemClickListener.onItemClick(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    public void updateData(List<Video> videos) {
        Log.d(TAG, "updateData: ");
        mVideos.clear();
        mVideos = videos;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Background work
            getThumbnails();
            handler.post(() -> {
                // UI Thread work
                notifyDataSetChanged();
            });
        });
        executor.shutdown();
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView videoThumbnail;

        TextView videoIndex;

        TextView videoName;

        TextView videoPath;

        TextView videoSize;

        TextView videoType;

        TextView videoResolution;

        TextView videoDuration;

        TextView videoWarning;


        public ViewHolder(View itemView) {
            super(itemView);

            videoIndex = itemView.findViewById(R.id.tv_video_index);
            videoThumbnail = itemView.findViewById(R.id.tv_video_thumbnail);
            videoName = itemView.findViewById(R.id.tv_video_name);
            videoPath = itemView.findViewById(R.id.tv_video_path);
            videoSize = itemView.findViewById(R.id.tv_video_size);
            videoResolution = itemView.findViewById(R.id.tv_video_resolution);
            videoDuration = itemView.findViewById(R.id.tv_video_duration);
            videoType = itemView.findViewById(R.id.tv_video_type);
            videoWarning = itemView.findViewById(R.id.tv_video_warning);
        }
    }
}
