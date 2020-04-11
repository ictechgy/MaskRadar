package com.jh.mask_radar.ui.map;

/**
 * 마커 하나에 대한 정보를 가지고 있는 클래스.
 */
public class MarkerInfo {


    /**
     * 마커가 stores나 markers 내에서 어느 포지션에 속한 마커인지를 나타내는 멤버변수
     */
    private int index;

    /**
     * 마커가 01인 약국인지, 02인 우체국인지, 03인 하나로마트인지를 나타내는 타입변수
     */
    private String type;

    /**
     * 지도를 어느정도 이동한 경우 이미 기존에 그려져 있던 마커라면 다시 그리지 않게 하도록 하기위한 구분값.
     * 지도를 움직였을 때 이미 기존에 존재하던 마커라면 true값을 가진다. -> 아직 미구현
     */
    private boolean isNew;

    private MarkerInfo(){}

    public MarkerInfo(int index, String type, boolean isNew) {
        this.index = index;
        this.type = type;
        this.isNew = isNew;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
