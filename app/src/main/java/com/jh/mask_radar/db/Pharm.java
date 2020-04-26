package com.jh.mask_radar.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Pharm {
    @PrimaryKey
    public int code;

    @ColumnInfo(name = "create_at")
    public String createdAt;

    @ColumnInfo(name = "remain_stat")
    public String remainStat;

    @ColumnInfo(name = "stock_at")
    public String stockAt;

    @ColumnInfo(name = "address")
    public String addr;

    @ColumnInfo(name = "latitude")
    public float lat;

    @ColumnInfo(name = "longitude")
    public float lng;

    @ColumnInfo
    public String name;

    @ColumnInfo
    public String type;
}
