# MaskRadar - 마스크 레이더   

**~~현재 구글 플레이스토어 정책 강화로 인하여 어플리케이션 검색이 되지 않습니다. 한국 정보화 진흥원에 앱 등록을 문의한 후, 플레이스토어에 재 개시 될 수 있도록 하겠습니다. -> 재출시 되었습니다!!~~**   
### [건강보험심사평가원에서의 오픈 API 제공 중단](https://www.data.go.kr/bbs/ntc/selectNotice.do?originId=NOTICE_0000000001728)에 따라 본 어플리케이션은 더이상 운용되지 않습니다.   

**Google Play - <https://play.google.com/store/apps/details?id=com.jh.mask_radar>**       
**ONE Store - <https://onestore.co.kr/userpoc/apps/view?pid=0000748572>**         
     
                
> 공적마스크 검색 어플리케이션입니다.   
> Only For Android - 안드로이드 전용입니다.   
   
&nbsp;   
## 🤔 본 어플리케이션을 만들게된 계기 (What made me create this application?)
2020년도 초, 코로나19의 확산에 따라 마스크 수급불안이 발생하였고 이에 정부는 2020년 3월 5일 **'마스크 수급 안정화 대책'** 을 발표하였습니다. (출생연도 끝자리에 따라 마스크를 구입할 수 있는 요일을 지정)   
다만, 각 약국마다 배분할 수 있는 마스크의 수에 제한이 있었다보니 약국별로 재고가 얼마나 남아있는지 알 수 있도록 관련 API도 제공하기 시작하였습니다. (3월 11일) - [참고 링크](https://terms.naver.com/entry.nhn?docId=5926931&cid=43667&categoryId=43667)   
저 또한 사람들이 편리하게 마스크를 살 수 있도록 돕기 위해 어플리케이션 개발에 착수하였습니다.   
   
&nbsp;   

## 💻 개발 기간 (Development Period)
최초버전인 Ver 1.0을 제작하는데에 소요된 시간 - 약 1주일   
이후 UI 변경 및 불편사항들을 반영하여 업데이트 진행.   
개발은 혼자 진행하였으며 아이콘 및 디자인 부분에서는 박유진 학생(yujinp@gmail.com)의 도움을 받아 진행하였습니다.   
**개발에 사용된 언어: Java, Kotlin(일부)**   
&nbsp;   
*사실 심사받는 과정이 길어져 출시에는 몇주 더 소요되었었다.(코로나로 인한 재택근무 등의 영향)*   
**출시된 이후에도 '코로나 관련 어플리케이션 정책'변경으로 인해 한차례 구글 플레이에서 삭제되었었다가 ['한국지능정보사회진흥원(구 한국정보화진흥원) PasS-TA'](https://mask.paas-ta.org/) 문의를 통해 해결하였었다.**   
   
&nbsp;   

## 📚 사용한 라이브러리 (Used libraries)
1. [네이버 지도](https://www.ncloud.com/product/applicationService/maps)
2. [Google Material Design](https://material.io/design)
3. [Volley](https://developer.android.com/training/volley?hl=ko)
4. play-services-location
5. [Room](https://developer.android.com/training/data-storage/room?hl=ko)
6. [swipe refresh layout](https://developer.android.com/training/swipe/add-swipe-interface?hl=ko) 
7. 기타 legacy support나 kotlin 지원 라이브러리 등
   
&nbsp;   

## 🚀 사용했거나 사용하려 했던 패턴/스킬 (Used Or Tried Patterns And Skills)
1. MVVM (Model - View - ViewModel)   
제대로 사용했다고 볼 수는 없다. 새로운 액티비티 등을 만들었을 때 자동으로 작성되어있는 것이 궁금하여 검색하고 사용을 시도해보았으나.. 굉장히 미숙하게 사용했다. 
2. Factory Pattern (객체 생성의 시점을 바로 결정하지 않고 서브클래스로 미룸)   
이 패턴 또한 올바르게 사용했다고 생각이 들지는 않는다. 
   
&nbsp;   

## 💦 만들면서 힘들었던 점 (Difficulties)

   
&nbsp;   

## 💬 기능(사용법) 

   
&nbsp;   

## 🛠 개선해야할 점/추가했으면 하는 기능 (Needs to be improved / Want to add)

   
&nbsp;   

## 📝 Information
   

     초기버전에서 네비게이션의 위치를 변경하였으며 즐겨찾기 기능을 추가하였습니다. 
     
     
#### 개인정보 처리방침: <https://developerahn.blogspot.com/2020/03/blog-post.html>   
#### 데이터는 '건강보험심사평가원-공적마스크 판매정보'를 통해 가져왔습니다.(공공데이터포털) <https://www.data.go.kr/dataset/15043025/openapi.do>
&nbsp;
### 사용된 오픈소스 파일 정보
* 지도는 '네이버 지도'를 사용하였습니다. <https://www.ncloud.com/product/applicationService/maps>   
* 폰트는 '우아한 형제들'사의 배달의 민족 글꼴을 사용하였습니다. <https://www.woowahan.com/#/fonts>   
  + 사용된 폰트: 한나는 열한살, 한나 Air, 주아체
* 별도로 '네이버'사의 글꼴을 사용하였습니다. <https://hangeul.naver.com/font>
  + 사용된 폰트: 나눔 스퀘어   
* * *
1. 어플리케이션에 사용된 ic_map, ic_move_to_inbox, ic_notification_black, ic_settings, ic_settings_outline, ic_start, ic_start_border, ic_update, ic_warning xml 파일들은 Google Material Design 아이콘을 사용하였습니다.   &nbsp;   [Material.io](https://material.io/resources/icons/?style=baseline, "Material Design") - Available under [Apache license version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html, "Apache license link")   
2. ic_nh, ic_marker_nh, ic_unselected_nh 아이콘에 사용된 농협 마크는 NH농협의 CI 심볼마크를 사용하였습니다. [NH농협 CI 소개](https://www.nonghyup.com/introduce/ci/symbol.do, "NH농협 CI 소개링크") 
3. ic_post_offece, ic_marker_post_office, ic_unselected_post_office 아이콘에 사용된 우체국 마크는 우정사업본부의 우정 제비 CI를 사용하였습니다. [우정 제비 CI](http://www.koreapost.go.kr/, "우정사업본부 CI")
4. ic_marker_pharm, ic_unselected_pharm, ic_pill, ic_pill_color 아이콘, 어플리케이션 로고 및 그래픽 이미지의 저작권은 박유진 학생(yujinp@gmail.com) 및 안진홍(ictechgy@gmail.com)에 있습니다. 
* * *   
#### Ver.1.0.0 이미지   
<img width="370" alt="mask_radar_1" src="https://user-images.githubusercontent.com/39452092/84392512-74bcdd00-ac35-11ea-986c-663da05153cf.png">   
<img width="370" alt="mask_radar_2" src="https://user-images.githubusercontent.com/39452092/84392537-7ab2be00-ac35-11ea-930e-a87162e120ed.png">   
<img width="370" alt="mask_radar_3" src="https://user-images.githubusercontent.com/39452092/84392546-7dadae80-ac35-11ea-97d8-4f6169ef919a.png">   
<img width="370" alt="mask_radar_4" src="https://user-images.githubusercontent.com/39452092/84392560-80a89f00-ac35-11ea-9170-5903baeda11a.png">   
<img width="370" alt="mask_radar_5" src="https://user-images.githubusercontent.com/39452092/84392581-856d5300-ac35-11ea-99ac-16230acd6749.png">   
<img width="370" alt="mask_radar_6" src="https://user-images.githubusercontent.com/39452092/84392577-84d4bc80-ac35-11ea-9dce-a8a9df4e2525.png">   
<img width="370" alt="mask_radar_7" src="https://user-images.githubusercontent.com/39452092/84392573-843c2600-ac35-11ea-81da-b266147fe8bc.png">

   
      
#### 아래 이미지는 Ver. 1.0.0 이전 이미지   
<img width="200" alt="mask_radar_screenshot1" src="https://user-images.githubusercontent.com/39452092/79042879-4a02d800-7c36-11ea-9f58-b67c30b226f4.png">
<img width="200" alt="mask_radar_screenshot2" src="https://user-images.githubusercontent.com/39452092/79042880-4e2ef580-7c36-11ea-8a85-2bd36b73a748.png">
<img width="200" alt="mask_radar_screenshot3" src="https://user-images.githubusercontent.com/39452092/79042881-4f602280-7c36-11ea-8786-fa543814142d.png">
