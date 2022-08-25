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
  
### Usage

#### Init
Add this to onCreate of your first activity
 ```sh
      
 ```
 #### Mediation Adapter
 
 If you're going to use IronSource Mediation with other networks, you have to implement the corresponding network adapter
 Here's all network adapter you need:
 https://developers.is.com/ironsource-mobile/android/mediation-networks-android/#step-1
#### Load interstitial
 ```sh
      
 ```
#### Show interstitial
Only available after intersitital loaded successfully
 ```sh		
         IronSourceUtil.showInterstitials(placementId)
 ```
#### Load and show interstitials
 ```sh
        
 ```
 #### Load a banner
 
 ```sh
  ```
