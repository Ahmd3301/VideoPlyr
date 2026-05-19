# 🎬 VideoPlyr — Mission Brief for Antigravity 2.0
# Platform: Google Antigravity 2.0 Desktop (Agent-First)
# GitHub: connect via Settings → Integrations → GitHub before starting

---

## 🎯 MISSION SUMMARY

Build, test, and deliver a working Android debug APK named **VideoPlyr**.
You must autonomously: create the GitHub repository, write all source files,
trigger GitHub Actions, monitor the build, fix any errors, and provide the
final APK download link — without human intervention.

If a build fails: read the error log, fix the root cause, push the fix,
and re-trigger the workflow. Repeat until the build succeeds.

---

## 📦 APP IDENTITY

| Field | Value |
|-------|-------|
| App Name | VideoPlyr |
| Package | io.videoplyr.app |
| minSdk | 26 (Android 8.0) |
| targetSdk | 34 |
| compileSdk | 34 |
| Build type | debug |
| Language | Kotlin |
| Build system | Gradle Kotlin DSL |

---

## ⚠️ GRADLE — LOCKED VERSION TRIANGLE (NEVER DEVIATE)

Any mismatch between these three causes immediate build failure.
Use exactly these versions, no exceptions:

| Component | Version |
|-----------|---------|
| JDK | 17 (Temurin) |
| Gradle Wrapper | 8.4 |
| Android Gradle Plugin | 8.2.2 |
| Kotlin | 1.9.22 |

### gradle/wrapper/gradle-wrapper.properties — write EXACTLY:
```
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### gradlew — DO NOT commit to repo. Generate in GitHub Actions:
```yaml
- name: Generate Gradle Wrapper
  run: |
    gradle wrapper --gradle-version 8.4 --distribution-type bin
    chmod +x gradlew
```

---

## 🗂️ FILE STRUCTURE

```
VideoPlyr/
├── .github/workflows/build.yml
├── gradle/wrapper/gradle-wrapper.properties
├── settings.gradle.kts
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── values/s.xml
│       │   ├── drawable/
│       │   │   ├── ic_play.xml
│       │   │   ├── ic_pause.xml
│       │   │   ├── ic_rewind.xml
│       │   │   ├── ic_forward.xml
│       │   │   ├── ic_mute.xml
│       │   │   ├── ic_volume.xml
│       │   │   ├── ic_captions_on.xml
│       │   │   ├── ic_captions_off.xml
│       │   │   ├── ic_settings.xml
│       │   │   ├── ic_pip.xml
│       │   │   ├── ic_fullscreen_enter.xml
│       │   │   ├── ic_fullscreen_exit.xml
│       │   │   ├── ic_resize.xml
│       │   │   └── controls_gradient.xml
│       │   └── layout/
│       │       ├── activity_main.xml
│       │       ├── player_controls.xml
│       │       └── item_playlist.xml
│       └── java/io/videoplyr/app/
│           ├── MainActivity.kt
│           ├── PlayerController.kt
│           ├── ControlsManager.kt
│           ├── DeepLinkHandler.kt
│           ├── ExtractorEngine.kt
│           └── PlaylistAdapter.kt
```

---

## 📄 settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.2.2"
        kotlin("android") version "1.9.22"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "VideoPlyr"
include(":app")
```

---

## 📄 app/build.gradle.kts

```kotlin
plugins {
    id("com.android.application")
    kotlin("android")
}
android {
    namespace = "io.videoplyr.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "io.videoplyr.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        debug { isDebuggable = true }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { viewBinding = true }
}
dependencies {
    // ExoPlayer — Media3
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.3.1")

    // UI
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")

    // Blur
    implementation("com.github.Dimezis:BlurView:version-2.0.3")
}
```

---

## 📄 AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

    <application
        android:label="VideoPlyr"
        android:theme="@style/FullscreenTheme"
        android:hardwareAccelerated="true"
        android:supportsRtl="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="sensorLandscape"
            android:configChanges="orientation|screenSize|keyboardHidden|screenLayout|smallestScreenSize"
            android:windowSoftInputMode="adjustNothing"
            android:supportsPictureInPicture="true"
            android:launchMode="singleTop">

            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>

            <!-- Deep Links: videoplyrio://open and videoplyrio://playlist -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW"/>
                <category android:name="android.intent.category.DEFAULT"/>
                <category android:name="android.intent.category.BROWSABLE"/>
                <data android:scheme="videoplyrio"/>
            </intent-filter>

        </activity>
    </application>
</manifest>
```

---

## 📄 res/values/s.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="black">#000000</color>
    <color name="white">#FFFFFF</color>
    <color name="blur_bg">#1FFFFFFF</color>
    <color name="blur_bg_active">#40FFFFFF</color>
    <color name="progress_played">#FFFFFFFF</color>
    <color name="progress_buffered">#66FFFFFF</color>
    <color name="progress_unplayed">#33FFFFFF</color>

    <style name="FullscreenTheme" parent="Theme.AppCompat.NoActionBar">
        <item name="android:windowFullscreen">true</item>
        <item name="android:windowLayoutInDisplayCutoutMode">shortEdges</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowDrawsSystemBarBackgrounds">true</item>
        <item name="android:background">#000000</item>
    </style>
</resources>
```

---

## 📄 ICONS STRATEGY — Two-Phase with Fallback

### Phase 1: Extract from plyr.svg (GitHub Actions step)

In build.yml, before assembleDebug, run this Python script:

```python
import re, pathlib, urllib.request, xml.etree.ElementTree as ET

urllib.request.urlretrieve("https://cdn.plyr.io/3.8.4/plyr.svg", "/tmp/plyr.svg")

svg_text = pathlib.Path("/tmp/plyr.svg").read_text()
out = pathlib.Path("app/src/main/res/drawable")
out.mkdir(parents=True, exist_ok=True)

icons = {
    "plyr-play": "ic_play",
    "plyr-pause": "ic_pause",
    "plyr-rewind": "ic_rewind",
    "plyr-fast-forward": "ic_forward",
    "plyr-muted": "ic_mute",
    "plyr-volume": "ic_volume",
    "plyr-captions-on": "ic_captions_on",
    "plyr-captions-off": "ic_captions_off",
    "plyr-settings": "ic_settings",
    "plyr-pip": "ic_pip",
    "plyr-fullscreen-enter": "ic_fullscreen_enter",
    "plyr-fullscreen-exit": "ic_fullscreen_exit",
}

ET.register_namespace("", "http://www.w3.org/2000/svg")
ns = {"svg": "http://www.w3.org/2000/svg"}
tree = ET.fromstring(svg_text)
extracted = []

for symbol_id, file_name in icons.items():
    symbol = tree.find(f'.//svg:symbol[@id="{symbol_id}"]', ns)
    if symbol is None:
        print(f"MISSING: {symbol_id}")
        continue
    vb = symbol.get("viewBox", "0 0 18 18").split()
    w, h = vb[2], vb[3]
    paths = []
    for elem in symbol.iter():
        tag = elem.tag.split("}")[-1]
        if tag == "path":
            d = elem.get("d", "")
            if d:
                paths.append(f'    <path android:fillColor="#FFFFFF" android:pathData="{d}"/>')
    if not paths:
        print(f"NO_PATHS: {symbol_id}")
        continue
    xml_out = f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="18dp"
    android:height="18dp"
    android:viewportWidth="{w}"
    android:viewportHeight="{h}">
{chr(10).join(paths)}
</vector>'''
    (out / f"{file_name}.xml").write_text(xml_out)
    extracted.append(file_name)
    print(f"OK: {file_name}.xml")

print(f"EXTRACTED={','.join(extracted)}")
```

### Phase 2: Material Icons fallback for any missing icon

After running the Python script, for every icon NOT successfully extracted,
create the VectorDrawable using Material Icons path data:

| File | Material path data (viewportWidth/Height=24) |
|------|----------------------------------------------|
| ic_play.xml | M8,5.14V19.14L19,12.14L8,5.14Z |
| ic_pause.xml | M6,19H10V5H6V19M14,5V19H18V5H14Z |
| ic_rewind.xml | M11.5,12L22,6V18L11.5,12M2,18H6V6H2V18M6,6V18L16.5,12L6,6Z (replay_10 style) |
| ic_forward.xml | M13,6V18L2.5,12M22,6H18V18H22V6M18,18V6L7.5,12L18,18Z (forward_10 style) |
| ic_mute.xml | M16.5,12A4.5,4.5 0 0,0 14,7.97V10.18L16.45,12.63C16.48,12.43 16.5,12.21 16.5,12M19,12C19,12.94 18.8,13.82 18.46,14.64L19.97,16.15C20.62,14.91 21,13.5 21,12A9,9 0 0,0 12,3L12,3A9,9 0 0,0 9,3.77L10.52,5.29C10.98,5.11 11.48,5 12,5A7,7 0 0,1 19,12M4.27,3L3,4.27L7.73,9C6.25,10.13 5.23,11.76 5.04,13.65L3,13.65V15.65L5.04,15.65C5.56,18.1 7.37,20.06 9.73,20.7V22.7H11.73V20.7C12.95,20.38 14.05,19.74 14.94,18.87L18.73,22.65L20,21.38L4.27,3Z |
| ic_volume.xml | M3,9V15H7L12,20V4L7,9H3M16.5,12C16.5,10.23 15.5,8.71 14,7.97V16.02C15.5,15.29 16.5,13.76 16.5,12M14,3.23V5.29C16.89,6.15 19,8.83 19,12C19,15.17 16.89,17.84 14,18.7V20.77C18.01,19.86 21,16.28 21,12C21,7.72 18.01,4.14 14,3.23Z |
| ic_captions_on.xml | M20,4H4C2.9,4 2,4.9 2,6V18C2,19.1 2.9,20 4,20H20C21.1,20 22,19.1 22,18V6C22,4.9 21.1,4 20,4M20,18H4V6H20V18M6,10H8V12H6V10M6,14H14V16H6V14M16,14H18V16H16V14M10,10H18V12H10V10Z |
| ic_captions_off.xml | M20,4H4C2.9,4 2,4.9 2,6V18C2,19.1 2.9,20 4,20H20C21.1,20 22,19.1 22,18V6C22,4.9 21.1,4 20,4M20,18H4V6H20V18M6,10H8V12H6V10M6,14H14V16H6V14M16,14H18V16H16V14M10,10H18V12H10V10M2,2L22,22L20.59,23.41L1.17,4L2,2Z |
| ic_settings.xml | M19.14,12.94C19.18,12.64 19.2,12.33 19.2,12C19.2,11.68 19.18,11.36 19.13,11.06L21.16,9.48C21.34,9.34 21.39,9.07 21.28,8.87L19.36,5.55C19.24,5.33 18.99,5.26 18.77,5.33L16.38,6.29C15.88,5.91 15.35,5.59 14.76,5.35L14.4,2.81C14.36,2.57 14.16,2.4 13.92,2.4H10.08C9.84,2.4 9.65,2.57 9.61,2.81L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33C5.02,5.25 4.77,5.33 4.65,5.55L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48L4.89,11.06C4.84,11.36 4.8,11.69 4.8,12C4.8,12.31 4.82,12.64 4.87,12.94L2.84,14.52C2.66,14.66 2.61,14.93 2.72,15.13L4.64,18.45C4.76,18.67 5.01,18.74 5.23,18.67L7.62,17.71C8.12,18.09 8.65,18.41 9.24,18.65L9.6,21.19C9.65,21.43 9.84,21.6 10.08,21.6H13.92C14.16,21.6 14.36,21.43 14.39,21.19L14.75,18.65C15.34,18.41 15.88,18.09 16.37,17.71L18.76,18.67C18.98,18.75 19.23,18.67 19.35,18.45L21.27,15.13C21.39,14.91 21.34,14.66 21.15,14.52L19.14,12.94M12,15.6C10.02,15.6 8.4,13.98 8.4,12C8.4,10.02 10.02,8.4 12,8.4C13.98,8.4 15.6,10.02 15.6,12C15.6,13.98 13.98,15.6 12,15.6Z |
| ic_pip.xml | M19,11H11V17H19V11M21,3H3C1.9,3 1,3.9 1,5V19C1,20.1 1.9,21 3,21H21C22.1,21 23,20.1 23,19V5C23,3.9 22.1,3 21,3M21,19.01H3V4.99H21V19.01Z |
| ic_fullscreen_enter.xml | M7,14H5V19H10V17H7V14M5,10H7V7H10V5H5V10M17,17H14V19H19V14H17V17M14,5V7H17V10H19V5H14Z |
| ic_fullscreen_exit.xml | M5,16H8V19H10V14H5V16M8,8H5V10H10V5H8V8M14,19H16V16H19V14H14V19M16,8V5H14V10H19V8H16Z |
| ic_resize.xml | M1,1V6H3V3H6V1H1M18,1V3H21V6H23V1H18M3,18H1V23H6V21H3V18M21,21H18V23H23V18H21V21Z |

For every icon: viewportWidth="24" viewportHeight="24" width="18dp" height="18dp"
fillColor="#FFFFFF"

### controls_gradient.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:startColor="#00000000"
        android:endColor="#CC000000"
        android:angle="270"/>
</shape>
```

---

## 📄 activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/rootContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000000">

    <androidx.media3.ui.AspectRatioFrameLayout
        android:id="@+id/aspectRatioFrame"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <SurfaceView
            android:id="@+id/surfaceView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </androidx.media3.ui.AspectRatioFrameLayout>

    <androidx.media3.ui.SubtitleView
        android:id="@+id/subtitleView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

    <!-- Extraction overlay — shown only during ###ex extraction -->
    <LinearLayout
        android:id="@+id/extractionOverlay"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:gravity="center"
        android:background="#000000"
        android:visibility="gone">

        <ProgressBar
            android:id="@+id/extractionProgress"
            style="@style/Widget.AppCompat.ProgressBar.Horizontal"
            android:layout_width="240dp"
            android:layout_height="4dp"
            android:indeterminate="true"
            android:progressTint="#FFFFFFFF"
            android:indeterminateTint="#FFFFFFFF"/>

        <TextView
            android:id="@+id/extractionText"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="Extracting..."
            android:textColor="#FFFFFFFF"
            android:textSize="14sp"
            android:alpha="0.7"/>

    </LinearLayout>

    <include
        android:id="@+id/controlsOverlay"
        layout="@layout/player_controls"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</FrameLayout>
```

---

## 📄 player_controls.xml

Exact visual clone of test3315.html. All measurements match:
48dp controls bar, 34dp title, 20dp margins, 16dp gaps, 135dp playlist height.

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/controlsRoot"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:alpha="0">

    <!-- Bottom gradient -->
    <View
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_gravity="bottom"
        android:background="@drawable/controls_gradient"/>

    <!-- PLAYLIST — hidden by default, shown when title tapped -->
    <eightbitlab.com.blurview.BlurView
        android:id="@+id/playlistContainer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        android:layout_marginBottom="114dp"
        android:visibility="gone"
        android:alpha="0"
        android:translationY="20dp"
        android:scaleX="0.98"
        android:scaleY="0.98"
        app:blurRadius="4dp"
        app:blurOverlayColor="@color/blur_bg">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/playlistRecycler"
            android:layout_width="match_parent"
            android:layout_height="135dp"
            android:orientation="horizontal"
            android:clipToPadding="false"
            app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"/>

    </eightbitlab.com.blurview.BlurView>

    <!-- TITLE BAR — tappable, toggles playlist -->
    <eightbitlab.com.blurview.BlurView
        android:id="@+id/titleContainer"
        android:layout_width="wrap_content"
        android:layout_height="34dp"
        android:layout_gravity="bottom|start"
        android:layout_marginStart="20dp"
        android:layout_marginBottom="64dp"
        android:clickable="true"
        android:focusable="true"
        app:blurRadius="4dp"
        app:blurOverlayColor="@color/blur_bg">

        <TextView
            android:id="@+id/titleText"
            android:layout_width="wrap_content"
            android:layout_height="34dp"
            android:gravity="center_vertical"
            android:paddingStart="14dp"
            android:paddingEnd="14dp"
            android:text="Video player plyr.io 👍"
            android:textColor="#FFFFFF"
            android:textSize="15sp"
            android:singleLine="true"/>

    </eightbitlab.com.blurview.BlurView>

    <!-- CONTROLS BAR — 11 buttons, transparent background -->
    <LinearLayout
        android:id="@+id/controlsBar"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_gravity="bottom"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        android:layout_marginBottom="10dp"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <ImageButton android:id="@+id/btnRewind"
            android:layout_width="32dp" android:layout_height="32dp"
            android:src="@drawable/ic_rewind"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="Rewind"/>

        <ImageButton android:id="@+id/btnPlayPause"
            android:layout_width="32dp" android:layout_height="32dp"
            android:layout_marginStart="4dp"
            android:src="@drawable/ic_play"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="Play"/>

        <ImageButton android:id="@+id/btnForward"
            android:layout_width="32dp" android:layout_height="32dp"
            android:layout_marginStart="4dp"
            android:src="@drawable/ic_forward"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="Forward"/>

        <androidx.media3.ui.DefaultTimeBar
            android:id="@+id/timeBar"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="8dp"
            android:layout_marginEnd="8dp"
            app:played_color="@color/progress_played"
            app:buffered_color="@color/progress_buffered"
            app:unplayed_color="@color/progress_unplayed"
            app:scrubber_color="@color/white"
            app:scrubber_enabled_size="12dp"
            app:scrubber_disabled_size="8dp"
            app:scrubber_dragged_size="16dp"/>

        <TextView android:id="@+id/tvTime"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginEnd="4dp"
            android:text="0:00 / 0:00"
            android:textColor="#FFFFFF"
            android:textSize="12sp"/>

        <ImageButton android:id="@+id/btnMute"
            android:layout_width="32dp" android:layout_height="32dp"
            android:src="@drawable/ic_volume"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="Mute"/>

        <ImageButton android:id="@+id/btnCaptions"
            android:layout_width="32dp" android:layout_height="32dp"
            android:src="@drawable/ic_captions_off"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="Captions"/>

        <ImageButton android:id="@+id/btnSettings"
            android:layout_width="32dp" android:layout_height="32dp"
            android:src="@drawable/ic_settings"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="Settings"/>

        <ImageButton android:id="@+id/btnPip"
            android:layout_width="32dp" android:layout_height="32dp"
            android:src="@drawable/ic_pip"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="PiP"/>

        <ImageButton android:id="@+id/btnResize"
            android:layout_width="32dp" android:layout_height="32dp"
            android:src="@drawable/ic_resize"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="Resize"/>

        <ImageButton android:id="@+id/btnFullscreen"
            android:layout_width="32dp" android:layout_height="32dp"
            android:src="@drawable/ic_fullscreen_enter"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:tint="#FFFFFF" android:scaleType="fitCenter"
            android:contentDescription="Fullscreen"/>

    </LinearLayout>

</FrameLayout>
```

---

## 📄 item_playlist.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<eightbitlab.com.blurview.BlurView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/cardBlur"
    android:layout_width="240dp"
    android:layout_height="135dp"
    android:layout_marginEnd="16dp"
    android:clickable="true"
    android:focusable="true"
    app:blurRadius="4dp"
    app:blurOverlayColor="@color/blur_bg">

    <TextView
        android:id="@+id/itemTitle"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:textColor="#FFFFFF"
        android:textSize="15sp"
        android:paddingStart="12dp"
        android:paddingEnd="12dp"/>

</eightbitlab.com.blurview.BlurView>
```

---

## 📄 ExtractorEngine.kt

Implements the ###ex extraction logic — invisible WebView approach matching m3u8.js logic.

```kotlin
class ExtractorEngine(
    private val context: Context,
    private val onFound: (String) -> Unit,
    private val onFailed: () -> Unit
) {
    // EXTRACTION MARKER — URL suffix that triggers extraction mode
    companion object {
        const val EXTRACT_SUFFIX = "###ex"
        const val TIMEOUT_MS = 10_000L
        const val POLL_INTERVAL_MS = 300L

        fun needsExtraction(url: String) = url.contains(EXTRACT_SUFFIX)
        fun cleanUrl(url: String) = url.replace(EXTRACT_SUFFIX, "")
    }

    private var extractWebView: WebView? = null
    private val handler = Handler(Looper.getMainLooper())

    // Step 1: fetch the page, find player iframe URL, then extract m3u8
    // Matches getPlayerUrl() in m3u8.js:
    //   finds li[onclick*="player_iframe.location.href"] → extracts iframe URL
    // Step 2: open iframe URL in invisible WebView, poll DOM every 300ms
    //   looking for button.hd_btn[data-url*=".m3u8"] — matches extractM3u8() in m3u8.js

    fun extract(rawUrl: String) {
        val pageUrl = cleanUrl(rawUrl)
        extractWebView?.destroy()

        val webView = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowUniversalAccessFromFileURLs = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        }
        extractWebView = webView

        val bridge = object {
            @JavascriptInterface
            fun onM3u8Found(url: String) {
                handler.post {
                    cleanup()
                    onFound(url)
                }
            }
            @JavascriptInterface
            fun onM3u8NotFound() {
                handler.post {
                    cleanup()
                    onFailed()
                }
            }
        }
        webView.addJavascriptInterface(bridge, "Extractor")

        // Phase 1: load main page, find player iframe URL
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // Inject JS that:
                // 1. Finds li[onclick*="player_iframe.location.href"]
                // 2. Extracts the iframe URL from onclick attribute
                // 3. Navigates WebView to that URL
                // 4. Then polls for button.hd_btn[data-url*=".m3u8"] every 300ms
                // 5. On find: calls Extractor.onM3u8Found(url)
                // 6. On timeout (10s): calls Extractor.onM3u8NotFound()
                view.evaluateJavascript("""
                    (function() {
                        // Check if we're already on the player page
                        var btns = document.querySelectorAll('button.hd_btn');
                        if (btns.length > 0) {
                            // Already on player page — start polling
                            startPolling();
                            return;
                        }

                        // Find player iframe URL (Step 1 from m3u8.js)
                        var li = document.querySelector('li[onclick*="player_iframe.location.href"]');
                        if (li) {
                            var onclick = li.getAttribute('onclick');
                            var match = onclick.match(/'([^']+)'/);
                            if (match) {
                                window.location.href = match[1];
                                return;
                            }
                        }

                        // If no li found, try polling directly
                        startPolling();

                        function startPolling() {
                            var attempts = 0;
                            var maxAttempts = Math.floor(10000 / 300);
                            var interval = setInterval(function() {
                                attempts++;
                                var buttons = document.querySelectorAll('button.hd_btn');
                                for (var i = 0; i < buttons.length; i++) {
                                    var dataUrl = buttons[i].getAttribute('data-url');
                                    if (dataUrl && dataUrl.indexOf('.m3u8') !== -1) {
                                        clearInterval(interval);
                                        Extractor.onM3u8Found(dataUrl);
                                        return;
                                    }
                                }
                                // Also check video src and source tags
                                var video = document.querySelector('video[src*=".m3u8"]');
                                if (video) {
                                    clearInterval(interval);
                                    Extractor.onM3u8Found(video.src);
                                    return;
                                }
                                var source = document.querySelector('source[src*=".m3u8"]');
                                if (source) {
                                    clearInterval(interval);
                                    Extractor.onM3u8Found(source.src);
                                    return;
                                }
                                if (attempts >= maxAttempts) {
                                    clearInterval(interval);
                                    Extractor.onM3u8NotFound();
                                }
                            }, 300);
                        }
                    })();
                """, null)
            }
        }

        // Load with Referer header matching m3u8.js
        webView.loadUrl(pageUrl, mapOf("Referer" to "https://faselhd.center/"))
    }

    private fun cleanup() {
        extractWebView?.destroy()
        extractWebView = null
    }
}
```

---

## 📄 DeepLinkHandler.kt

```kotlin
object DeepLinkHandler {

    enum class Type { SINGLE, SINGLE_EXTRACT, PLAYLIST, NONE }

    data class Result(
        val type: Type,
        val url: String? = null,
        val title: String? = null,
        val playlist: List<PlaylistItem>? = null
    )

    fun parse(intent: Intent?): Result {
        val uri = intent?.data ?: return Result(Type.NONE)
        if (uri.scheme != "videoplyrio") return Result(Type.NONE)

        return when (uri.host) {
            "open" -> {
                val rawUrl = uri.getQueryParameter("url") ?: return Result(Type.NONE)
                val title = uri.getQueryParameter("title") ?: "Video"
                val type = if (ExtractorEngine.needsExtraction(rawUrl))
                    Type.SINGLE_EXTRACT else Type.SINGLE
                Result(type, url = rawUrl, title = title)
            }
            "playlist" -> {
                val data = uri.getQueryParameter("data") ?: return Result(Type.NONE)
                try {
                    val json = String(Base64.decode(data, Base64.URL_SAFE or Base64.NO_WRAP))
                    val array = JSONArray(json)
                    val items = (0 until array.length()).map {
                        val obj = array.getJSONObject(it)
                        PlaylistItem(obj.getString("title"), obj.getString("url"))
                    }
                    Result(Type.PLAYLIST, playlist = items)
                } catch (e: Exception) {
                    Result(Type.NONE)
                }
            }
            else -> Result(Type.NONE)
        }
    }
}

data class PlaylistItem(val title: String, val url: String)
```

---

## 📄 ControlsManager.kt

```kotlin
class ControlsManager(
    private val controlsRoot: View,
    private val playlistContainer: View,
    private val autoHideMs: Long = 3000L
) {
    private val handler = Handler(Looper.getMainLooper())
    var isVisible = false
    var isPlaylistOpen = false
    private val hideRunnable = Runnable { hide() }

    fun show() {
        isVisible = true
        controlsRoot.animate().alpha(1f).setDuration(300)
            .setInterpolator(DecelerateInterpolator()).start()
        scheduleHide()
    }

    fun hide() {
        isVisible = false
        if (isPlaylistOpen) hidePlaylist()
        controlsRoot.animate().alpha(0f).setDuration(300)
            .setInterpolator(AccelerateInterpolator()).start()
    }

    fun toggle() { if (isVisible) hide() else show() }

    fun togglePlaylist() {
        if (isPlaylistOpen) hidePlaylist() else showPlaylist()
    }

    private fun showPlaylist() {
        isPlaylistOpen = true
        playlistContainer.visibility = View.VISIBLE
        playlistContainer.animate()
            .alpha(1f).translationY(0f).scaleX(1f).scaleY(1f)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()
        cancelHide()
    }

    private fun hidePlaylist() {
        isPlaylistOpen = false
        val dy = 20f * playlistContainer.resources.displayMetrics.density
        playlistContainer.animate()
            .alpha(0f).translationY(dy).scaleX(0.98f).scaleY(0.98f)
            .setDuration(300)
            .withEndAction { playlistContainer.visibility = View.GONE }
            .start()
    }

    fun scheduleHide() {
        handler.removeCallbacks(hideRunnable)
        handler.postDelayed(hideRunnable, autoHideMs)
    }

    fun cancelHide() = handler.removeCallbacks(hideRunnable)
}
```

---

## 📄 PlayerController.kt

```kotlin
class PlayerController(
    private val context: Context,
    private val surfaceView: SurfaceView,
    private val subtitleView: SubtitleView,
    private val timeBar: DefaultTimeBar,
    private val tvTime: TextView,
    private val onPlayingChanged: (Boolean) -> Unit,
    private val onTracksChanged: (hasSubs: Boolean, qualities: List<Int>) -> Unit
) {
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setSeekBackIncrementMs(10_000)
        .setSeekForwardIncrementMs(10_000)
        .build()

    private val handler = Handler(Looper.getMainLooper())

    init {
        player.setVideoSurfaceView(surfaceView)
        subtitleView.setPlayer(player)

        timeBar.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(t: TimeBar, pos: Long) {}
            override fun onScrubMove(t: TimeBar, pos: Long) {}
            override fun onScrubStop(t: TimeBar, pos: Long, canceled: Boolean) {
                if (!canceled) player.seekTo(pos)
            }
        })

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = onPlayingChanged(isPlaying)
            override fun onTracksChanged(tracks: Tracks) {
                val qualities = tracks.groups
                    .filter { it.type == C.TRACK_TYPE_VIDEO }
                    .flatMap { g -> (0 until g.length).map { g.getTrackFormat(it).height } }
                    .distinct().sorted()
                val hasSubs = tracks.groups.any { it.type == C.TRACK_TYPE_TEXT }
                onTracksChanged(hasSubs, qualities)
            }
        })

        val tick = object : Runnable {
            override fun run() {
                val pos = player.currentPosition
                val dur = player.duration.takeIf { it > 0 } ?: 0
                timeBar.setPosition(pos)
                timeBar.setDuration(dur)
                timeBar.setBufferedPosition(player.bufferedPosition)
                tvTime.text = "${fmt(pos)} / ${fmt(dur)}"
                handler.postDelayed(this, 500)
            }
        }
        handler.post(tick)
    }

    fun loadUrl(url: String) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
    }

    fun loadDefault() = loadUrl(
        "https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-1080p.mp4"
    )

    fun loadPlaylist(items: List<PlaylistItem>) {
        player.setMediaItems(items.map { MediaItem.fromUri(it.url) })
        player.prepare()
        player.playWhenReady = true
    }

    fun toggleMute(): Boolean {
        val muting = player.volume > 0f
        player.volume = if (muting) 0f else 1f
        return muting
    }

    fun toggleCaptions(on: Boolean) {
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .apply {
                if (on) setPreferredTextLanguages("en", "ar")
                else setIgnoredTextSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            }.build()
    }

    fun setSpeed(speed: Float) = player.setPlaybackSpeed(speed)

    fun setQuality(height: Int) {
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setMaxVideoSize(Int.MAX_VALUE, height)
            .setMinVideoSize(0, height)
            .build()
    }

    fun release() { handler.removeCallbacksAndMessages(null); player.release() }

    private fun fmt(ms: Long) = "%d:%02d".format(ms / 1000 / 60, ms / 1000 % 60)
}
```

---

## 📄 PlaylistAdapter.kt

```kotlin
class PlaylistAdapter(
    private val items: List<PlaylistItem>,
    private val rootViewGroup: ViewGroup,
    private val onClick: (PlaylistItem, Int) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.VH>() {

    private var activeIndex = 0

    inner class VH(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        b.cardBlur.setupWith(rootViewGroup).setBlurRadius(4f).setBlurAutoUpdate(true)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.itemTitle.text = items[position].title
        holder.binding.cardBlur.setOverlayColor(
            if (position == activeIndex) 0x40FFFFFF.toInt() else 0x1FFFFFFF.toInt()
        )
        holder.itemView.setOnClickListener {
            val prev = activeIndex; activeIndex = position
            notifyItemChanged(prev); notifyItemChanged(position)
            onClick(items[position], position)
        }
    }

    override fun getItemCount() = items.size
    fun setActive(i: Int) { val p = activeIndex; activeIndex = i; notifyItemChanged(p); notifyItemChanged(i) }
}
```

---

## 📄 MainActivity.kt

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: PlayerController
    private lateinit var controls: ControlsManager
    private lateinit var extractor: ExtractorEngine

    private var isMuted = false
    private var captionsOn = false
    private var qualities = listOf<Int>()

    private val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
    )
    private var resizeIdx = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyFullscreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPlayer()
        setupControls()
        setupExtractor()
        handleIntent(intent)
    }

    private fun applyFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setupExtractor() {
        extractor = ExtractorEngine(
            context = this,
            onFound = { m3u8Url ->
                hideExtractionOverlay()
                player.loadUrl(m3u8Url)
            },
            onFailed = {
                hideExtractionOverlay()
                // Show brief toast and load default
                player.loadDefault()
            }
        )
    }

    private fun showExtractionOverlay() {
        binding.extractionOverlay.visibility = View.VISIBLE
        binding.controlsOverlay.controlsRoot.alpha = 0f
    }

    private fun hideExtractionOverlay() {
        binding.extractionOverlay.visibility = View.GONE
    }

    private fun setupPlayer() {
        val c = binding.controlsOverlay
        player = PlayerController(
            context = this,
            surfaceView = binding.surfaceView,
            subtitleView = binding.subtitleView,
            timeBar = c.timeBar,
            tvTime = c.tvTime,
            onPlayingChanged = { playing ->
                c.btnPlayPause.setImageResource(
                    if (playing) R.drawable.ic_pause else R.drawable.ic_play
                )
            },
            onTracksChanged = { hasSubs, q ->
                qualities = q
                c.btnCaptions.isEnabled = hasSubs
            }
        )
    }

    private fun setupControls() {
        val c = binding.controlsOverlay
        controls = ControlsManager(c.controlsRoot, c.playlistContainer)

        c.btnPlayPause.setOnClickListener {
            if (player.player.isPlaying) player.player.pause() else player.player.play()
            controls.scheduleHide()
        }
        c.btnRewind.setOnClickListener { player.player.seekBack(); controls.scheduleHide() }
        c.btnForward.setOnClickListener { player.player.seekForward(); controls.scheduleHide() }
        c.btnMute.setOnClickListener {
            isMuted = player.toggleMute()
            c.btnMute.setImageResource(if (isMuted) R.drawable.ic_mute else R.drawable.ic_volume)
            controls.scheduleHide()
        }
        c.btnCaptions.setOnClickListener {
            captionsOn = !captionsOn
            player.toggleCaptions(captionsOn)
            c.btnCaptions.setImageResource(
                if (captionsOn) R.drawable.ic_captions_on else R.drawable.ic_captions_off
            )
            controls.scheduleHide()
        }
        c.btnSettings.setOnClickListener { showSettings(); controls.cancelHide() }
        c.btnPip.setOnClickListener { enterPip() }
        c.btnResize.setOnClickListener {
            resizeIdx = (resizeIdx + 1) % resizeModes.size
            binding.aspectRatioFrame.resizeMode = resizeModes[resizeIdx]
            controls.scheduleHide()
        }
        c.btnFullscreen.setOnClickListener { finish() }
        c.titleContainer.setOnClickListener { controls.togglePlaylist() }
        binding.rootContainer.setOnClickListener { controls.toggle() }

        c.playlistRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun handleIntent(intent: Intent?) {
        val result = DeepLinkHandler.parse(intent)
        val c = binding.controlsOverlay

        when (result.type) {
            DeepLinkHandler.Type.SINGLE -> {
                player.loadUrl(result.url!!)
                c.titleText.text = "${result.title} 👍"
                setPlaylist(listOf(PlaylistItem(result.title ?: "Video", result.url)))
            }
            DeepLinkHandler.Type.SINGLE_EXTRACT -> {
                showExtractionOverlay()
                c.titleText.text = "${result.title} 👍"
                extractor.extract(result.url!!)
            }
            DeepLinkHandler.Type.PLAYLIST -> {
                player.loadPlaylist(result.playlist!!)
                c.titleText.text = "${result.playlist.first().title} 👍"
                setPlaylist(result.playlist)
            }
            DeepLinkHandler.Type.NONE -> {
                player.loadDefault()
                c.titleText.text = "Video player plyr.io 👍"
                setPlaylist(listOf(
                    PlaylistItem("Video #01", "https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-720p.mp4"),
                    PlaylistItem("Video #02", "https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-1080p.mp4")
                ))
            }
        }
    }

    private fun setPlaylist(items: List<PlaylistItem>) {
        val c = binding.controlsOverlay
        val adapter = PlaylistAdapter(items, binding.root as ViewGroup) { item, idx ->
            player.loadUrl(item.url)
            c.titleText.text = "${item.title} 👍"
            controls.togglePlaylist()
        }
        c.playlistRecycler.adapter = adapter
    }

    private fun showSettings() {
        val dialog = BottomSheetDialog(this, R.style.FullscreenTheme)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0x1FFFFFFF)
            setPadding(32, 32, 32, 32)
        }
        // Speed options
        listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
            val tv = TextView(this).apply {
                text = "${speed}x"
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(16, 24, 16, 24)
                setOnClickListener { player.setSpeed(speed); dialog.dismiss() }
            }
            layout.addView(tv)
        }
        // Quality options
        qualities.forEach { q ->
            val tv = TextView(this).apply {
                text = "${q}p"
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(16, 24, 16, 24)
                setOnClickListener { player.setQuality(q); dialog.dismiss() }
            }
            layout.addView(tv)
        }
        dialog.setContentView(layout)
        dialog.show()
    }

    private fun enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
            )
        }
    }

    override fun onNewIntent(intent: Intent?) { super.onNewIntent(intent); handleIntent(intent) }
    override fun onResume() { super.onResume(); applyFullscreen() }
    override fun onPause() { super.onPause(); player.player.pause() }
    override fun onDestroy() { super.onDestroy(); player.release() }
    override fun onBackPressed() { finish() }
}
```

---

## 📄 .github/workflows/build.yml

```yaml
name: Build VideoPlyr APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Generate Gradle Wrapper
        run: |
          gradle wrapper --gradle-version 8.4 --distribution-type bin
          chmod +x gradlew

      - name: Extract Plyr SVG Icons with Material Fallback
        run: |
          python3 - <<'PYEOF'
          import urllib.request, pathlib, xml.etree.ElementTree as ET

          try:
              urllib.request.urlretrieve("https://cdn.plyr.io/3.8.4/plyr.svg", "/tmp/plyr.svg")
              svg_text = pathlib.Path("/tmp/plyr.svg").read_text()
              plyr_available = True
          except:
              plyr_available = False
              svg_text = ""

          out = pathlib.Path("app/src/main/res/drawable")
          out.mkdir(parents=True, exist_ok=True)

          icons = {
              "plyr-play": "ic_play",
              "plyr-pause": "ic_pause",
              "plyr-rewind": "ic_rewind",
              "plyr-fast-forward": "ic_forward",
              "plyr-muted": "ic_mute",
              "plyr-volume": "ic_volume",
              "plyr-captions-on": "ic_captions_on",
              "plyr-captions-off": "ic_captions_off",
              "plyr-settings": "ic_settings",
              "plyr-pip": "ic_pip",
              "plyr-fullscreen-enter": "ic_fullscreen_enter",
              "plyr-fullscreen-exit": "ic_fullscreen_exit",
          }

          material = {
              "ic_play": "M8,5.14V19.14L19,12.14L8,5.14Z",
              "ic_pause": "M6,19H10V5H6V19M14,5V19H18V5H14Z",
              "ic_rewind": "M11.5,12L22,6V18L11.5,12M2,18H6V6H2V18M6,6V18L16.5,12L6,6Z",
              "ic_forward": "M13,6V18L2.5,12M22,6H18V18H22V6M18,18V6L7.5,12L18,18Z",
              "ic_mute": "M3,9V15H7L12,20V4L7,9H3M16.5,12C16.5,10.23 15.5,8.71 14,7.97V16.02C15.5,15.29 16.5,13.76 16.5,12M14,3.23V5.29C16.89,6.15 19,8.83 19,12C19,15.17 16.89,17.84 14,18.7V20.77C18.01,19.86 21,16.28 21,12C21,7.72 18.01,4.14 14,3.23M16.5,12C16.5,10.23 15.5,8.71 14,7.97V16.02C15.5,15.29 16.5,13.76 16.5,12Z",
              "ic_volume": "M3,9V15H7L12,20V4L7,9H3M16.5,12C16.5,10.23 15.5,8.71 14,7.97V16.02C15.5,15.29 16.5,13.76 16.5,12M14,3.23V5.29C16.89,6.15 19,8.83 19,12C19,15.17 16.89,17.84 14,18.7V20.77C18.01,19.86 21,16.28 21,12C21,7.72 18.01,4.14 14,3.23Z",
              "ic_captions_on": "M20,4H4C2.9,4 2,4.9 2,6V18C2,19.1 2.9,20 4,20H20C21.1,20 22,19.1 22,18V6C22,4.9 21.1,4 20,4M20,18H4V6H20V18M6,10H8V12H6V10M6,14H14V16H6V14M16,14H18V16H16V14M10,10H18V12H10V10Z",
              "ic_captions_off": "M20,4H4C2.9,4 2,4.9 2,6V18C2,19.1 2.9,20 4,20H20C21.1,20 22,19.1 22,18V6C22,4.9 21.1,4 20,4M20,18H4V6H20V18M6,10H8V12H6V10M6,14H14V16H6V14M16,14H18V16H16V14M10,10H18V12H10V10M2,2L22,22L20.59,23.41L1.17,4L2,2Z",
              "ic_settings": "M12,15.5A3.5,3.5 0 0,1 8.5,12A3.5,3.5 0 0,1 12,8.5A3.5,3.5 0 0,1 15.5,12A3.5,3.5 0 0,1 12,15.5M19.43,12.97C19.47,12.65 19.5,12.33 19.5,12C19.5,11.67 19.47,11.34 19.43,11L21.54,9.37C21.73,9.22 21.78,8.95 21.66,8.73L19.66,5.27C19.54,5.05 19.27,4.96 19.05,5.05L16.56,6.05C16.04,5.66 15.5,5.32 14.87,5.07L14.5,2.42C14.46,2.18 14.25,2 14,2H10C9.75,2 9.54,2.18 9.5,2.42L9.13,5.07C8.5,5.32 7.96,5.66 7.44,6.05L4.95,5.05C4.73,4.96 4.46,5.05 4.34,5.27L2.34,8.73C2.21,8.95 2.27,9.22 2.46,9.37L4.57,11C4.53,11.34 4.5,11.67 4.5,12C4.5,12.33 4.53,12.65 4.57,12.97L2.46,14.63C2.27,14.78 2.21,15.05 2.34,15.27L4.34,18.73C4.46,18.95 4.73,19.03 4.95,18.95L7.44,17.94C7.96,18.34 8.5,18.68 9.13,18.93L9.5,21.58C9.54,21.82 9.75,22 10,22H14C14.25,22 14.46,21.82 14.5,21.58L14.87,18.93C15.5,18.68 16.04,18.34 16.56,17.94L19.05,18.95C19.27,19.03 19.54,18.95 19.66,18.73L21.66,15.27C21.78,15.05 21.73,14.78 21.54,14.63L19.43,12.97Z",
              "ic_pip": "M19,11H11V17H19V11M21,3H3C1.9,3 1,3.9 1,5V19C1,20.1 1.9,21 3,21H21C22.1,21 23,20.1 23,19V5C23,3.9 22.1,3 21,3M21,19.01H3V4.99H21V19.01Z",
              "ic_fullscreen_enter": "M7,14H5V19H10V17H7V14M5,10H7V7H10V5H5V10M17,17H14V19H19V14H17V17M14,5V7H17V10H19V5H14Z",
              "ic_fullscreen_exit": "M5,16H8V19H10V14H5V16M8,8H5V10H10V5H8V8M14,19H16V16H19V14H14V19M16,8V5H14V10H19V8H16Z",
              "ic_resize": "M1,1V6H3V3H6V1H1M18,1V3H21V6H23V1H18M3,18H1V23H6V21H3V18M21,21H18V23H23V18H21V21Z",
          }

          extracted = set()

          if plyr_available:
              try:
                  ns = {"svg": "http://www.w3.org/2000/svg"}
                  tree = ET.fromstring(svg_text)
                  for sym_id, fname in icons.items():
                      sym = tree.find(f'.//svg:symbol[@id="{sym_id}"]', ns)
                      if sym is None: continue
                      vb = sym.get("viewBox", "0 0 18 18").split()
                      w, h = vb[2], vb[3]
                      paths = [f'    <path android:fillColor="#FFFFFF" android:pathData="{e.get("d")}"/>'
                               for e in sym.iter() if e.tag.split("}")[-1] == "path" and e.get("d")]
                      if not paths: continue
                      xml = f'''<?xml version="1.0" encoding="utf-8"?>
          <vector xmlns:android="http://schemas.android.com/apk/res/android"
              android:width="18dp" android:height="18dp"
              android:viewportWidth="{w}" android:viewportHeight="{h}">
          {chr(10).join(paths)}
          </vector>'''
                      (out / f"{fname}.xml").write_text(xml)
                      extracted.add(fname)
                      print(f"PLYR_OK: {fname}")
              except Exception as e:
                  print(f"PLYR_PARSE_ERROR: {e}")

          # Fallback: Material Icons for any missing
          for fname, path_data in material.items():
              if fname not in extracted:
                  xml = f'''<?xml version="1.0" encoding="utf-8"?>
          <vector xmlns:android="http://schemas.android.com/apk/res/android"
              android:width="18dp" android:height="18dp"
              android:viewportWidth="24" android:viewportHeight="24">
              <path android:fillColor="#FFFFFF" android:pathData="{path_data}"/>
          </vector>'''
                  (out / f"{fname}.xml").write_text(xml)
                  print(f"MATERIAL_FALLBACK: {fname}")

          print("ICONS_DONE")
          PYEOF

      - uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle.kts', 'gradle/wrapper/gradle-wrapper.properties') }}

      - name: Build Debug APK
        run: ./gradlew assembleDebug --no-daemon --stacktrace

      - uses: actions/upload-artifact@v4
        with:
          name: VideoPlyr-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 30

      - uses: softprops/action-gh-release@v2
        with:
          tag_name: build-${{ github.run_number }}
          name: "VideoPlyr Build #${{ github.run_number }}"
          body: |
            ## 📥 Download APK
            Tap `app-debug.apk` below — install directly on your device.
            Commit: ${{ github.sha }}
          files: app/build/outputs/apk/debug/app-debug.apk
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

      - name: Print download URL
        run: |
          echo "✅ APK READY:"
          echo "https://github.com/${{ github.repository }}/releases/tag/build-${{ github.run_number }}"
```

---

## ✅ ACCEPTANCE CRITERIA — Verify All Before Finishing

### Build
- [ ] APK compiles with zero errors on first GitHub Actions run
- [ ] APK size under 12MB
- [ ] GitHub Release created with direct APK download link

### UI — Exact match with test3315.html
- [ ] Full-screen landscape, notch covered, no system bars
- [ ] Black background (#000000)
- [ ] Bottom gradient (transparent → #CC000000)
- [ ] All 11 control buttons present and functional
- [ ] Title bar: 34dp height, rgba(255,255,255,0.12) blur background
- [ ] Title shows current video name + 👍 emoji
- [ ] Tap title → playlist slides up with animation
- [ ] Playlist cards: 240×135dp, blur background, 16dp gap
- [ ] Active card: rgba(255,255,255,0.25) — brighter than inactive
- [ ] Controls auto-hide after 3 seconds
- [ ] Tap screen → controls appear (NOT play/pause)
- [ ] Controls fade in 300ms decelerate / fade out 300ms accelerate
- [ ] Playlist animates: translateY(20dp→0) + scale(0.98→1) 500ms

### Extraction (###ex)
- [ ] URL with ###ex shows extraction overlay immediately
- [ ] Horizontal progress bar (indeterminate) visible during extraction
- [ ] "Extracting..." text visible below progress bar
- [ ] On success: overlay disappears, video plays
- [ ] On failure (10s timeout): overlay disappears, default video loads
- [ ] Referer header set to https://faselhd.center/ during extraction

### Deep Links
- [ ] `videoplyrio://open?url=URL&title=TITLE` — plays directly
- [ ] `videoplyrio://open?url=URL###ex&title=TITLE` — triggers extraction
- [ ] `videoplyrio://playlist?data=BASE64` — loads playlist, plays first item

### Player
- [ ] Demo video plays automatically on launch (no deep link)
- [ ] HLS / m3u8 URLs play natively via ExoPlayer
- [ ] Resize cycles: fill → contain → square
- [ ] Settings sheet shows speed + quality options
- [ ] PiP works (Android O+)
- [ ] Captions toggle works when subtitles available

---

## 🧪 TEST COMMANDS

```bash
# Single video
adb shell am start -W -a android.intent.action.VIEW \
  -d "videoplyrio://open?url=https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-720p.mp4&title=Test+Video" \
  io.videoplyr.app

# Extraction mode
adb shell am start -W -a android.intent.action.VIEW \
  -d "videoplyrio://open?url=https://faselhd.center/episode/test###ex&title=FaselHD+Test" \
  io.videoplyr.app

# Playlist — generate Base64 first:
echo '[{"title":"Vid1","url":"https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-720p.mp4"},{"title":"Vid2","url":"https://cdn.plyr.io/static/demo/View_From_A_Blue_Moon_Trailer-1080p.mp4"}]' | base64 -w 0

adb shell am start -W -a android.intent.action.VIEW \
  -d "videoplyrio://playlist?data=BASE64_HERE" \
  io.videoplyr.app
```

---

## 📝 AGENT BEHAVIOR INSTRUCTIONS

1. Create a new GitHub repository named `VideoPlyr` using the connected GitHub integration
2. Write all files listed above in the exact structure specified
3. Commit and push to `main` branch
4. Monitor GitHub Actions workflow — watch for build errors
5. If build fails: read the full error log, identify root cause, fix, push, re-monitor
6. Repeat error-fix cycle until build succeeds with exit code 0
7. Confirm APK is attached to GitHub Release
8. Output the final APK download URL to the user
9. Do not ask for human input during the process — work autonomously end-to-end
