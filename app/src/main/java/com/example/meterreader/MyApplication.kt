package com.example.meterreader

import android.app.Application
import com.yandex.mobile.ads.MobileAds

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация SDK вручную
        MobileAds.initialize(this) {
            // SDK готов, можно загружать рекламу
        }
    }
}
