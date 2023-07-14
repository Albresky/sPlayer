package cn.albresky.splayer.Bean;

import java.io.Serializable;

public class SongController implements Serializable {

    public boolean isPlaying;

    public int position;


    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
