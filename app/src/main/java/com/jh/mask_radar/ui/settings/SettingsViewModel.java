package com.jh.mask_radar.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SettingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("현재 베타버전 어플리케이션입니다.\n지도는 네이버 클라우드 플랫폼 지도API를 이용하였습니다." +
                "\n폰트는 배달의 민족(한나는 11살체, 한나air체, 주아체)와 네이버의 나눔스퀘어를 이용하였습니다.(비상업적 사용)" +
                "\n라이센스 관련정보는 준비하는대로 추가하겠습니다." +
                "\n\n문의메일 : ictechgy@gmail.com");
    }

    public LiveData<String> getText() {
        return mText;
    }
}