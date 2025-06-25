package com.example.music;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private List<Song> songs;
    private Context context;
    private OnItemClickListener listener;

    // Mảng màu phù hợp cho ứng dụng
    private int[] colors = {
            Color.parseColor("#FF8A65"), // Cam nhạt
            Color.parseColor("#4DB6AC"), // Xanh ngọc dịu
            Color.parseColor("#BA68C8"), // Tím nhẹ
            Color.parseColor("#81C784"), // Xanh lá cây pastel
            Color.parseColor("#64B5F6"), // Xanh dương nhạt
            Color.parseColor("#FFD54F")  // Vàng nhạt
    };

    public MusicAdapter(List<Song> songs, Context context, OnItemClickListener listener) {
        this.songs = songs;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());

        // Gán màu ngẫu nhiên theo vị trí item
        int color = colors[position % colors.length];
        holder.cardView.setCardBackgroundColor(color);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        CardView cardView;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            artist = itemView.findViewById(R.id.tvArtist);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
