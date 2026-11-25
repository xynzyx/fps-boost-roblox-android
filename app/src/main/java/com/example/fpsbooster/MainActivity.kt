package com.example.fpsbooster

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val robloxPkg = "com.roblox.client"
    private lateinit var info: TextView
    private var boostOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        info = TextView(this).apply { textSize = 15f }
        root.addView(info)

        fun btn(text: String, action: () -> Unit) {
            root.addView(Button(this).apply {
                this.text = text
                setOnClickListener { action() }
            })
        }

        btn("1) Arka plan uygulamalarını temizle") { killBackground() }
        btn("2) Performans modunu aç/kapat") { toggleBoost() }
        btn("3) Çözünürlük 720p ADB komutunu kopyala") {
            copy("adb shell wm size 1280x720 && adb shell wm density 200")
        }
        btn("4) Çözünürlük 540p (maksimum FPS) komutunu kopyala") {
            copy("adb shell wm size 960x540 && adb shell wm density 160")
        }
        btn("5) Çözünürlüğü eski haline getir komutunu kopyala") {
            copy("adb shell wm size reset && adb shell wm density reset")
        }
        btn("6) Geliştirici ayarlarını aç") {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
        btn("7) Roblox'u başlat") { launchRoblox() }

        root.addView(TextView(this).apply {
            textSize = 13f
            setPadding(0, 30, 0, 0)
            text = "NOT: Çözünürlük değiştirmek için ADB gerekir.\n" +
                "Roblox içinde: Ayarlar > Graphics Mode > Manual, Graphics Quality = 1."
        })

        setContentView(ScrollView(this).apply { addView(root) })
        refreshInfo()
    }

    private fun refreshInfo() {
        val dm = resources.displayMetrics
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        info.text = "Çözünürlük: ${dm.widthPixels}x${dm.heightPixels} (${dm.densityDpi} dpi)\n" +
            "Boş RAM: ${mi.availMem / 1048576} MB / ${mi.totalMem / 1048576} MB\n" +
            "Performans modu: ${if (boostOn) "AÇIK" else "kapalı"}\n"
    }

    private fun killBackground() {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        var count = 0
        for (app in packageManager.getInstalledApplications(0)) {
            val isSystem = app.flags and ApplicationInfo.FLAG_SYSTEM != 0
            if (!isSystem && app.packageName != packageName && app.packageName != robloxPkg) {
                am.killBackgroundProcesses(app.packageName)
                count++
            }
        }
        Toast.makeText(this, "$count uygulama için temizleme istendi", Toast.LENGTH_SHORT).show()
        refreshInfo()
    }

    private fun toggleBoost() {
        boostOn = !boostOn
        if (boostOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            window.setSustainedPerformanceMode(boostOn)
        }
        refreshInfo()
    }

    private fun copy(text: String) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("adb", text))
        Toast.makeText(this, "Kopyalandı", Toast.LENGTH_LONG).show()
    }

    private fun launchRoblox() {
        val i = packageManager.getLaunchIntentForPackage(robloxPkg)
        if (i != null) startActivity(i)
        else Toast.makeText(this, "Roblox yüklü değil", Toast.LENGTH_SHORT).show()
    }
}
