package com.albresky.splayer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.albresky.splayer.Bean.Song;
import com.albresky.splayer.R;
import com.albresky.splayer.Utils.DateTimeUtils;
import com.albresky.splayer.Utils.MusicScanner;

import java.util.List;


public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    private List<Song> mSongs;

    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView songCover;
        TextView songName;
        TextView singer;
        TextView duration;
        TextView position;
        View songView;

        public ViewHolder(View itemView) {
            super(itemView);
            songView = itemView;
            songCover = itemView.findViewById(R.id.tv_song_cover);
            songName = itemView.findViewById(R.id.tv_song_name);
            singer = itemView.findViewById(R.id.tv_singer);
            duration = itemView.findViewById(R.id.tv_duration_time);
            position = itemView.findViewById(R.id.tv_position);
        }
    }

    public MusicListAdapter(List<Song> songs, @Nullable Context context) {
        mSongs = songs;
        mContext = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_music_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.songView.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "You clicked view ", Toast.LENGTH_SHORT).show();
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = mSongs.get(position);
        String time = DateTimeUtils.formatTime(song.getDuration());

        holder.songName.setText(song.getSong());
        holder.singer.setText(song.getSinger() + " - " + song.getAlbum());
        holder.duration.setText(time);
        holder.position.setText(position + 1 + "");
        holder.songCover.setImageBitmap(MusicScanner.getAlbumPicture(mContext, song.getPath(), 1));
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

}
