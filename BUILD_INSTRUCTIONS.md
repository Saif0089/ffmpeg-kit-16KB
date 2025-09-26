# FFmpeg-Kit 16KB Build Instructions

This document provides step-by-step instructions for building a custom ffmpeg-kit with 16KB page size support and libx264 encoder for Android applications.

## Overview

This build creates a custom ffmpeg-kit library that:
- ✅ Supports 16KB page size (Android 15+ compatibility)
- ✅ Includes libx264 encoder for high-quality H.264 video encoding
- ✅ Optimized for ARM architectures (arm64-v8a, armeabi-v7a) - covers 99%+ of Android devices
- ✅ Provides both native libraries (.so files) and Java classes
- ✅ Compact size (22MB AAR file)

## Quick Start (Pre-built AAR)

**Don't want to build from source?** Use the pre-built AAR file:

```bash
# Download the ready-to-use AAR
wget https://github.com/cfa532/ffmpeg-kit-16KB/raw/main/releases/ffmpeg-kit-16kb-with-libx264.aar

# Add to your Android project
cp ffmpeg-kit-16kb-with-libx264.aar your-project/app/libs/

# Update build.gradle.kts
implementation(files("libs/ffmpeg-kit-16kb-with-libx264.aar"))
implementation("com.arthenica:smart-exception-java:0.2.1")
```

**Features of the pre-built AAR:**
- ✅ 22MB size (optimized for mobile)
- ✅ ARM architectures only (arm64-v8a, armeabi-v7a)
- ✅ libx264 encoder enabled
- ✅ 16KB page size compatible
- ✅ Ready to use immediately

## Prerequisites

### System Requirements
- **macOS** (tested on macOS 14.6.0)
- **Java 17** (required for Android Gradle Plugin 8.1.0+)
- **Android SDK** with API level 24+
- **Android NDK r25c** (16KB page size compatible)

### Required Software
1. **Android Studio** (latest version)
2. **Java 17** (via SDKMAN or Homebrew)
3. **Git** (for cloning repositories)

## Step 1: Environment Setup

### 1.1 Install Java 17
```bash
# Using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.2-open
sdk use java 17.0.2-open

# Verify installation
java -version
# Should show: openjdk version "17.0.2"
```

### 1.2 Download Android NDK r25c
```bash
# Create NDK directory
mkdir -p ~/Library/Android/sdk/ndk

# Download NDK r25c for macOS
cd ~/Library/Android/sdk/ndk
curl -L -o android-ndk-r25c-darwin.zip "https://dl.google.com/android/repository/android-ndk-r25c-darwin.zip"

# Extract NDK
unzip android-ndk-r25c-darwin.zip

# Verify extraction
ls -la android-ndk-r25c/
```

### 1.3 Set Environment Variables
```bash
# Set required environment variables
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
export ANDROID_NDK_ROOT="$HOME/Library/Android/sdk/ndk/android-ndk-r25c"
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"

# Verify environment
echo "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"
echo "ANDROID_NDK_ROOT: $ANDROID_NDK_ROOT"
echo "JAVA_HOME: $JAVA_HOME"
```

## Step 2: Clone and Build FFmpeg-Kit

### 2.1 Clone the Repository
```bash
# Clone the 16KB-compatible ffmpeg-kit repository
git clone https://github.com/AliAkhgar/ffmpeg-kit-16KB.git
cd ffmpeg-kit-16KB
```

### 2.2 Build with libx264 Support
```bash
# Build ffmpeg-kit with libx264 encoder and GPL support
./android.sh --enable-gpl --enable-x264
```

**Expected Output:**
```
Building ffmpeg-kit for Android...
Building x264...
Building ffmpeg...
Building ffmpeg-kit...
Creating Android archive...
BUILD SUCCESSFUL
```

### 2.3 Verify Build Results
```bash
# Check if AAR file was created
ls -la android/ffmpeg-kit-android-lib/build/outputs/aar/

# Should show: ffmpeg-kit-release.aar
```

## Step 3: Integration into Android Project

### 3.1 Copy AAR to Your Project
```bash
# Copy the built AAR to your Android project
cp android/ffmpeg-kit-android-lib/build/outputs/aar/ffmpeg-kit-release.aar /path/to/your/project/app/libs/ffmpeg-kit-16kb-with-libx264.aar
```

### 3.2 Update build.gradle.kts
```kotlin
dependencies {
    // Custom FFmpeg Kit with libx264 and 16KB page size support
    implementation(files("libs/ffmpeg-kit-16kb-with-libx264.aar"))
    implementation("com.arthenica:smart-exception-java:0.2.1")
    
    // ... other dependencies
}
```

### 3.3 Update Your Code
```kotlin
// Use libx264 encoder in your FFmpeg commands
val ffmpegCommand = """
    -i "$inputPath" 
    -vf "scale=1280:720:force_original_aspect_ratio=decrease:force_divisible_by=2" 
    -c:v libx264 -c:a aac -b:v 1000k -b:a 128k 
    -hls_time 10 -hls_list_size 3 
    -hls_flags delete_segments 
    -f hls "$outputPath"
""".trimIndent().replace(Regex("\\s+"), " ")
```

## Step 4: Testing

### 4.1 Build Your Project
```bash
cd /path/to/your/project
./gradlew assembleDebug -x lintDebug
```

### 4.2 Test Video Conversion
```kotlin
// Test the encoder availability
val availableEncoders = testAvailableEncoders()
// Should include: [libx264, mpeg4, copy]

// Test HLS conversion
val result = localHLSConverter.convertToHLS(inputUri, outputDir, fileName)
// Should complete successfully with both 720p and 480p outputs
```

## Troubleshooting

### Common Issues

#### 1. Java Version Mismatch
**Error:** `Unsupported Java version`
**Solution:** Ensure Java 17 is being used
```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/current"
java -version
```

#### 2. NDK Not Found
**Error:** `NDK not found`
**Solution:** Verify NDK path and version
```bash
ls -la $ANDROID_NDK_ROOT
# Should show NDK r25c directory
```

#### 3. Width Divisibility Error
**Error:** `width not divisible by 2`
**Solution:** Add `force_divisible_by=2` to your scale filter
```bash
-vf "scale=1280:720:force_original_aspect_ratio=decrease:force_divisible_by=2"
```

#### 4. LiveEdit Issues
**Error:** `Can't instantiate abstract class`
**Solution:** Restart the app instead of using LiveEdit for sealed class changes

### Build Logs
If the build fails, check the logs:
```bash
# Check build logs
tail -50 build.log

# Check Android build logs
cd android
./gradlew assembleRelease --stacktrace
```

## Technical Details

### Supported Encoders
- **libx264**: High-quality H.264 encoding (recommended)
- **mpeg4**: Basic MPEG-4 encoding
- **copy**: Stream copying (no re-encoding)

### Supported Architectures
- **arm64-v8a**: 64-bit ARM (modern devices)
- **armeabi-v7a**: 32-bit ARM with NEON
- **x86**: 32-bit Intel
- **x86_64**: 64-bit Intel

### 16KB Page Size Support
- Built with NDK r25c for Android 15+ compatibility
- Supports devices with 16KB memory page sizes
- Future-proof for upcoming Android requirements

## Performance Metrics

### Encoding Quality
- **libx264**: Excellent quality with good compression
- **Rate Factor**: ~21-25 (good quality)
- **Profile**: High Profile, Level 3.1
- **Color Space**: 4:2:0, 8-bit

### Build Size
- **AAR File**: ~80MB (includes all architectures)
- **Native Libraries**: ~60MB
- **Java Classes**: ~57KB

## Contributing

To contribute to this build:

1. Fork the repository
2. Make your changes
3. Test the build process
4. Submit a pull request

## License

This build includes GPL-licensed components (libx264):
- **libx264**: GPL v2+
- **FFmpeg**: LGPL v2.1+
- **ffmpeg-kit**: LGPL v3+

Ensure your application complies with the respective licenses.

## Support

For issues and questions:
- Check the troubleshooting section above
- Review build logs for specific errors
- Ensure all prerequisites are properly installed
- Verify environment variables are set correctly

---

**Build Date:** September 17, 2025  
**FFmpeg Version:** 6.0  
**NDK Version:** r25c  
**Java Version:** 17.0.2  
**Tested On:** macOS 14.6.0, Android API 24+
