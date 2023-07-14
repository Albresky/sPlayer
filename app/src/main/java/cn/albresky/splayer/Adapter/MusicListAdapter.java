package cn.albresky.splayer.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
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

import cn.albresky.splayer.Bean.Song;
import cn.albresky.splayer.R;
import cn.albresky.splayer.Utils.DatetimeUtils;
import cn.albresky.splayer.Utils.MusicScanner;


public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    private List<Song> mSongs;

    private List<Bitmap> mSongCovers;

    private Context mContext;

    private OnItemClickListener mOnItemClickListener;

    public MusicListAdapter(List<Song> songs, @NonNull Context context) {
        mSongs = songs;
        mContext = context;
        mOnItemClickListener = (OnItemClickListener) context;

        // preload song covers
        mSongCovers = new ArrayList<>();
        getCovers();
    }

    private void getCovers() {
        for (Song song : mSongs) {
            byte[] data;
            data = MusicScanner.isAlbumContainCover(song.getPath());
            if (data == null) {
                song.setHasCover(false);
            } else {
                song.setHasCover(true);
            }
            mSongCovers.add(MusicScanner.getAlbumPicture(mContext, data, 1));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_music_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = mSongs.get(position);
        String time = DatetimeUtils.formatTime(song.getDuration());

        holder.songName.setText(song.getSong());
        holder.singer.setText(song.getSinger() + " - " + song.getAlbum());
        holder.duration.setText(time);
        holder.position.setText(position + 1 + "");
        holder.songType.setText(song.getType());
        holder.songCover.setImageBitmap(mSongCovers.get(position));

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "You clicked view ", Toast.LENGTH_SHORT).show();
            mOnItemClickListener.onItemClick(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    public void updateData(List<Song> songs) {
        mSongs = songs;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Background work
            getCovers();
            handler.post(() -> {
                // UI Thread work
                notifyDataSetChanged();
            });
        });
    }


    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView songCover;
        TextView songName;
        TextView singer;
        TextView duration;
        TextView position;
        TextView songType;
        View songView;

        public ViewHolder(View itemView) {
            super(itemView);
            songView = itemView;
            songCover = itemView.findViewById(R.id.tv_song_cover);
            songName = itemView.findViewById(R.id.tv_song_name);
            singer = itemView.findViewById(R.id.tv_singer);
            duration = itemView.findViewById(R.id.tv_duration_time);
            position = itemView.findViewById(R.id.tv_position);
            songType = itemView.findViewById(R.id.tv_song_type);
        }
    }
}
