# ApplovinUtils
An useful, quick implementation of IronSource Mediation SDK


<!-- GETTING STARTED -->

### Prerequisites

Add this to your project-level build.gradle
  ```sh
  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
Add this to your app-level build.gradle
```sh
dependencies {
	implementation 'com.github.dktlib:ApplovinUtilsLibrary:Tag'
}
 ```
### Usage

#### Init
Add this to onCreate of your first activity
 ```sh
      ApplovinUtil.initApplovin(this,  true)
 ```
 #### Mediation Adapter
 
 If you're going to use IronSource Mediation with other networks, you have to implement the corresponding network adapter
 Here's all network adapter you need:
 https://developers.is.com/ironsource-mobile/android/mediation-networks-android/#step-1
#### Load and show interstitial
 ```sh
	var interHolder = InterHolder("")

    fun loadInter(context: Context){
        ApplovinUtil.loadAnGetInterstitials(context,interHolder,object : InterstititialCallbackNew{
            override fun onInterstitialReady(interstitialAd: MaxInterstitialAd) {

            }

            override fun onInterstitialClosed() {

            }

            override fun onInterstitialLoadFail(error: String) {

            }

            override fun onInterstitialShowSucceed() {

            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }

    fun showInter(context: AppCompatActivity,interHolder: InterHolder,adsOnClick: AdsOnClick){
        AppOpenManager.getInstance().isAppResumeEnabled = true
        ApplovinUtil.showInterstitialsWithDialogCheckTimeNew(context, 800,interHolder ,object : InterstititialCallbackNew {
            override fun onInterstitialReady(interstitialAd : MaxInterstitialAd) {
                Toast.makeText(context,"Ready",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialClosed() {
                loadInter(context)
                adsOnClick.onAdsCloseOrFailed()
            }

            override fun onInterstitialLoadFail(error: String) {
                loadInter(context)
                adsOnClick.onAdsCloseOrFailed()
            }

            override fun onInterstitialShowSucceed() {

            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }
 ```
#### load and show banner
 ```sh
        ApplovinUtil.showBanner(this, bannerContainer, "", object : BannerCallback {
            override fun onBannerLoadFail(error: String) {
            }

            override fun onBannerShowSucceed() {
            }

            override fun onAdRevenuePaid(ad: MaxAd?) {
            }
        })
 ```
 #### Load and show Native

 ```sh
    fun showAdsNative(activity: Activity, nativeHolder: NativeHolder,viewGroup: ViewGroup){
        ApplovinUtil.loadAndShowNativeAdsWithLayout(activity,nativeHolder,R.layout.native_custom_ad_view,viewGroup,GoogleENative.UNIFIED_MEDIUM,object : NativeCallBackNew{
            override fun onNativeAdLoaded(nativeAd: MaxAd?, nativeAdView: MaxNativeAdView?) {
                Toast.makeText(activity,"Loaded",Toast.LENGTH_SHORT).show()
            }

            override fun onAdFail(error: String) {
                Toast.makeText(activity,"LoadFailed",Toast.LENGTH_SHORT).show()
            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }
  ```

#### Load and show Reward
```sh
            ApplovinUtil.loadReward(this, "c10d259dcb47378d", 15000, object : RewardCallback {
                override fun onRewardReady() {
                    ApplovinUtil.showRewardWithDialogCheckTime(
                        this@MainActivity,
                        1500,
                        object : RewardCallback {
                            override fun onRewardReady() {

                            }

                            override fun onRewardClosed() {
      
                            }

                            override fun onRewardLoadFail(error: String) {
                            }

                            override fun onRewardShowSucceed() {
                            }

                            override fun onUserRewarded() {

                            }

                            override fun onRewardedVideoStarted() {
                            }

                            override fun onRewardedVideoCompleted() {

                            }

                            override fun onAdRevenuePaid(ad: MaxAd?) {
                            }
                        })
                }

                override fun onRewardClosed() {
                }

                override fun onRewardLoadFail(error: String) {
                }

                override fun onRewardShowSucceed() {
                }

                override fun onUserRewarded() {
                }

                override fun onRewardedVideoStarted() {
                }

                override fun onRewardedVideoCompleted() {
                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                }
            })
```
