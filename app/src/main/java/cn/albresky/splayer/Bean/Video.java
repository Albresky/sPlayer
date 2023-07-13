package cn.albresky.splayer.Bean;

import org.litepal.crud.LitePalSupport;

public class Video extends LitePalSupport {

    public String name;

    public String path;

    public int duration;

    public long size;

    public String type;

    public boolean isCheck;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
