#!/bin/bash
# Build and install QinBot APK

set -e

cd "$(dirname "$0")/QinFeedback"

echo "🔨 Building APK..."
./gradlew assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo "✅ APK built: $APK_PATH"
    
    # Check if ADB device connected
    if adb devices | grep -q "device$"; then
        echo "📱 Installing to device..."
        adb install -r "$APK_PATH"
        echo "✅ Installed! Launch QinBot on the device."
    else
        echo "⚠️  No device connected. Connect Qin via USB and run:"
        echo "   adb install -r $APK_PATH"
    fi
else
    echo "❌ Build failed"
    exit 1
fi
