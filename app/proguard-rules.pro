# DotLearn - 默认不开启混淆（isMinifyEnabled = false）
# 如后续开启混淆，请保留 Room 与 Compose 相关规则。
-keep class androidx.room.** { *; }
-dontwarn org.jetbrains.annotations.**
