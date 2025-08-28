#!/bin/bash

echo "🎵 Simple Audio Player - Збірка APK"
echo "====================================="

# Встановлюємо змінні середовища
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

echo "🔧 Android SDK: $ANDROID_HOME"

# Перевіряємо чи є Android SDK
if [ ! -d "$ANDROID_HOME" ]; then
    echo "❌ Android SDK не знайдено в $ANDROID_HOME"
    exit 1
fi

echo "✅ Android SDK знайдено"

# Очищаємо проект
echo "🧹 Очищення проекту..."
./gradlew clean

# Збираємо APK
echo "📦 Збірка APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ APK успішно створено!"
    
    # Показуємо розмір APK
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
        echo "📏 Розмір APK: $APK_SIZE"
        
        # Копіюємо на робочий стіл
        cp "$APK_PATH" ~/Desktop/simple_audio_player.apk
        echo "📁 APK скопійовано на робочий стіл: simple_audio_player.apk"
    fi
else
    echo "❌ Помилка збірки APK"
    exit 1
fi
