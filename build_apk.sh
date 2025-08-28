#!/bin/bash

echo "🎵 Simple Audio Player - Збірка APK"
echo "====================================="

# Перевіряємо чи є Android SDK
if [ ! -d "$HOME/Library/Android/sdk" ]; then
    echo "❌ Android SDK не знайдено"
    echo "💡 Встановіть Android Studio або Android SDK"
    echo "📱 Завантажте з: https://developer.android.com/studio"
    exit 1
fi

echo "✅ Android SDK знайдено"

# Встановлюємо змінну середовища
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools"

echo "🔧 Налаштовуємо змінні середовища..."

# Очищаємо проект
echo "🧹 Очищення проекту..."
./gradlew clean

# Збираємо APK
echo "📦 Збірка APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ APK успішно створено!"
    echo "📱 Файл знаходиться в: app/build/outputs/apk/debug/"
    
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

