# FFmpeg-Kit 16KB with libx264 Support

Custom build of ffmpeg-kit for Android with 16KB page size compatibility and libx264 encoder.

## Features

- ✅ 16KB Page Size Support (Android 15+)
- ✅ libx264 Encoder (High-quality H.264)
- ✅ ARM Architecture Support (arm64-v8a, armeabi-v7a)
- ✅ HLS Video Conversion
- ✅ Local Processing
- ✅ Optimized Size (22MB)

## Quick Download

**Ready-to-use AAR files:**

- [ffmpeg-kit-16kb-with-libx264.aar](releases/ffmpeg-kit-16kb-with-libx264.aar) (22MB)
  - ARM architectures only (covers 99%+ of Android devices)
  - libx264 encoder enabled
  - 16KB page size compatible

## Quick Start

### Option 1: Use Pre-built AAR (Recommended)

```bash
# Download the AAR file
wget https://github.com/cfa532/ffmpeg-kit-16KB/raw/main/releases/ffmpeg-kit-16kb-with-libx264.aar

# Add to your project
cp ffmpeg-kit-16kb-with-libx264.aar your-project/app/libs/
```

### Option 2: Build from Source

```bash
# Set environment
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
export ANDROID_NDK_ROOT="$HOME/Library/Android/sdk/ndk/android-ndk-r25c"
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"

# Build
./android.sh --enable-gpl --enable-x264

# Use AAR
cp android/ffmpeg-kit-android-lib/build/outputs/aar/ffmpeg-kit-release.aar your-project/app/libs/
```

## Integration

```kotlin
// build.gradle.kts
dependencies {
    implementation(files("libs/ffmpeg-kit-16kb-with-libx264.aar"))
    implementation("com.arthenica:smart-exception-java:0.2.1")
}

// Usage
val command = "-i input.mp4 -c:v libx264 -preset fast -f hls output.m3u8"
FFmpegKit.execute(command)
```

## Documentation

See [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) for detailed build instructions.

## License

GPL v2+ (libx264), LGPL v3+ (ffmpeg-kit)

---

**Version**: 6.0-16KB-libx264  
**Tested**: Huawei Android, 16KB page size
