package com.jh.mask_radar.model;

/**
 * 하나의 판매지점에 대한 정보를 가지고 있는 객체 -> 서버에서 받은 데이터를 파싱해서 저장해둘 객체.
 * @see <a href="https://www.data.go.kr/dataset/15043025/openapi.do">건강보험심사평가원_공적 마스크 판매정보</a>
 * @see <a href="https://app.swaggerhub.com/apis-docs/Promptech/public-mask-info/20200307-oas3#/v1/get_storesByGeo_json">Reference document</a>
 */
public class Store {

    /**
     * 판매처 식별코드
     */
    private String code;

    /**
     * 데이터 생성일자
     */
    private String created_at;

    /**
     * 재고상태.
     * 100개 이상(녹색): 'plenty' / 30개 이상 100개미만(노랑색): 'some' / 2개 이상 30개 미만(빨강색): 'few' / 1개 이하(회색): 'empty' / 판매중지: 'break'
     */
    private String remain_stat;

    /**
     * 입고시간
     */
    private String stock_at;

    /**
     * 주소
     */
    private String addr;

    /**
     * 위도
     */
    private float lat;

    /**
     * 경도
     */
    private float lng;

    /**
     * 판매처 이름
     */
    private String name;

    /**
     * 판매처 유형 - 약국: '01', 우체국: '02', 농협: '03'
     */
    private String type;

    private int index;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getRemain_stat() {
        return remain_stat;
    }

    public void setRemain_stat(String remain_stat) {
        this.remain_stat = remain_stat;
    }

    public String getStock_at() {
        return stock_at;
    }

    public void setStock_at(String stock_at) {
        this.stock_at = stock_at;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIndex(int index){ this.index = index; }
    public int getIndex(){ return index; }
}
