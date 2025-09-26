# Changelog

## [6.0-16KB-libx264] - 2025-09-17

### Added
- libx264 encoder support for high-quality H.264 encoding
- Comprehensive build documentation
- Troubleshooting guide for common issues
- Performance optimization for HLS video conversion
- Pre-built AAR files for direct download
- ARM-only architecture build (optimized for mobile devices)

### Changed
- Enhanced build process with GPL support
- Improved error handling for width divisibility issues
- Optimized FFmpeg command parameters for better quality
- Reduced AAR size by excluding x86 architectures

### Fixed
- Width divisibility errors in video scaling
- LiveEdit compatibility issues with sealed classes
- Build environment setup for macOS

### Technical Details
- **FFmpeg Version**: 6.0
- **NDK Version**: r25c (16KB page size compatible)
- **Java Version**: 17.0.2
- **Build Size**: 22MB AAR (ARM architectures only)
- **Architectures**: arm64-v8a, armeabi-v7a (covers 99%+ of Android devices)
- **Quality**: Rate factor ~21-25 (excellent quality)

### Downloads
- [ffmpeg-kit-16kb-with-libx264.aar](releases/ffmpeg-kit-16kb-with-libx264.aar) - Ready-to-use AAR file

### Tested On
- Huawei Android device with 16KB page size
- Android API 24+
- macOS 14.6.0 build environment
- HLS video conversion (720p and 480p)

### Breaking Changes
- Requires Java 17 for building
- Requires NDK r25c for 16KB page size support
- GPL license applies due to libx264 inclusion
