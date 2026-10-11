# DotLearn

> 一款**完全离线**的 Android 学习辅助 App，把**西蒙学习法**、**费曼学习法**、**康奈尔笔记**整合成一套可日常执行的学习流。
> 中英双语可切换，数据只存在本机。

---

## 一、核心设计

三种学习法不是三个孤立的小工具，而是**围绕同一个「知识点」的三种加工方式**：

```
                    知识点 / 概念
                          │
      ┌───────────────────┼───────────────────┐
      ▼                   ▼                   ▼
  康奈尔笔记            费曼讲解            西蒙专注
 结构记录·线索自测     讲一遍·找卡点      拆知识块·番茄钟
      └───────────────────┼───────────────────┘
                          ▼
                  复习排程 · 打卡 · 统计
```

某个概念既可以写康奈尔笔记，也可以做费曼讲解，也能安排进西蒙式的专注计划；
数据通过「知识点」串联，复习与统计统一处理。

---

## 二、功能清单

### 三大学习法
| 方法 | 落地功能 |
| --- | --- |
| **康奈尔笔记** | 线索栏 / 笔记栏 / 总结栏 三段式编辑；**线索自测模式**（遮住笔记栏，按线索回忆后逐个揭示） |
| **费曼学习法** | 四步引导（选概念 → 讲给小白 → 找卡点 → 简化加类比）；**本地语音录制与回放**；卡点单独归档 |
| **西蒙学习法** | 把目标拆解为可逐个攻克的**知识块清单**；番茄专注计时器（圆环进度）；掌握度与连续打卡 |

### 复习与统计
- **间隔重复**：固定间隔 1 / 3 / 7 / 15 / 30 天，答"记住了"推进阶段，答"不熟"回到第一阶段
- **本地定时提醒**：用 WorkManager 链式续期，每天在设定时间推送复习提醒，**不需要联网**
- **学习统计**：累计/本周专注时长、连续打卡天数、近 7 天柱状图、掌握度分布

### 数据
- 学科 / 知识点 / 笔记 / 讲解 / 知识块 / 专注记录 / 复习排程，全部存本地 SQLite
- **一键导出 / 导入 JSON 备份**，数据属于你

### 体验
- 中英双语，**可在应用内直接切换**（设置 → 语言），也支持跟随系统
- Material 3，浅色 / 深色 / 跟随系统

---

## 三、离线与隐私

`AndroidManifest.xml` 中**刻意不声明 `android.permission.INTERNET`**。

这不是"代码里没写网络请求"这种自觉性约定，而是系统级的硬约束：即使代码中出现网络调用，系统也会直接拒绝。
配合应用私有目录存储，可以确认——**数据不可能离开这台设备**。

本 App 自己声明的权限只有两个：

| 权限 | 用途 |
| --- | --- |
| `RECORD_AUDIO` | 仅在费曼讲解中录制本地语音（可选，不授权也能用其他功能） |
| `POST_NOTIFICATIONS` | 推送本地复习提醒（Android 13+） |

构建后清单中还会出现 4 个由 WorkManager 自动合并进来的权限，需要说明清楚：

| 合并进来的权限 | 说明 |
| --- | --- |
| `ACCESS_NETWORK_STATE` | 仅能**读取**连接状态，不具备收发任何数据的能力；且在没有 `INTERNET` 的前提下无法建立连接 |
| `WAKE_LOCK` | 定时任务唤醒 CPU，属于本地调度 |
| `RECEIVE_BOOT_COMPLETED` | 重启后恢复定时提醒 |
| `FOREGROUND_SERVICE` | WorkManager 内部使用 |

已用 `aapt2 dump badging` 校验构建产物：**权限列表中不存在 `android.permission.INTERNET`**。
在 Android 上，没有该权限的进程无法建立任何网络套接字——这是系统层面的保证，而非代码约定。

---

## 四、技术栈

| 层 | 选型 |
| --- | --- |
| 语言 | Kotlin 1.9.24 |
| UI | Jetpack Compose + Material 3（Compose BOM 2024.06.00） |
| 架构 | MVVM + 单向数据流（UI → ViewModel/StateFlow → Repository → Room DAO） |
| 本地库 | Room 2.6.1（SQLite）+ DataStore |
| 定时 | WorkManager 2.9.0 + Notification |
| 导航 | Navigation Compose 2.7.7 |
| 录音 | MediaRecorder → 应用私有目录 |
| 备份 | org.json，纯本地序列化 |
| 构建 | AGP 8.5.2 / Gradle 8.7 / JDK 17 |

- `minSdk 26`（Android 8.0）、`targetSdk 34`、`compileSdk 34`
- 无第三方 UI 库、无网络库、无 DI 框架

---

## 五、目录结构

```
DotLearn/
├─ app/src/main/java/com/dotlearn/app/
│  ├─ MainActivity.kt              # 单 Activity 入口（AppCompatActivity，支持语言切换）
│  ├─ DotLearnApp.kt               # Application，初始化 ServiceLocator
│  ├─ ServiceLocator.kt            # 轻量服务定位器（替代 DI 框架）
│  ├─ data/                        # Room 实体、DAO、数据库、仓库、设置
│  ├─ domain/                      # 间隔重复算法、时间工具
│  ├─ backup/                      # JSON 导出 / 导入
│  ├─ audio/                       # 录音与播放
│  ├─ work/                        # 每日复习提醒 + 通知
│  └─ ui/                          # 主题、导航、通用组件、各功能页与 ViewModel
├─ app/src/main/res/
│  ├─ values/strings.xml           # 英文（默认）
│  └─ values-zh/strings.xml        # 简体中文
└─ .github/workflows/build.yml     # 云端自动构建 APK
```

---

## 六、如何得到 APK

### 路线 A：云端自动构建（本机无需装任何东西）

1. 在 GitHub 新建一个仓库（私有或公开都行）
2. 把本工程推上去：
   ```bash
   cd DotLearn
   git init
   git add .
   git commit -m "DotLearn: initial commit"
   git branch -M main
   git remote add origin https://github.com/<你的用户名>/<仓库名>.git
   git push -u origin main
   ```
3. 推送后 GitHub Actions 会自动开始构建
4. 打开仓库的 **Actions** 页 → 最新的 `Build APK` → 页面底部 **Artifacts** 下载 `DotLearn-debug-apk`
5. 解压得到 `app-debug.apk`，传到手机安装即可（需在系统设置里允许"安装未知来源应用"）

> 也可以在 Actions 页面点 **Run workflow** 手动触发。

### 路线 B：本地用 Android Studio

1. 安装 [Android Studio](https://developer.android.com/studio)（自带 JDK 17 与 Android SDK）
2. `File → Open`，选择 `DotLearn` 目录
3. 等待 Gradle Sync 完成
4. 点 Run ▶ 装到手机，或 `Build → Build Bundle(s)/APK(s) → Build APK(s)`

> 注意：本机若只有 JDK 8 是**无法**构建的，AGP 8.x 需要 JDK 17。

---

## 七、上手建议

1. 先在「我的 → 学科管理」建 1~2 个学科（比如"线性代数""英语"）
2. 在学科里添加知识点
3. 进「专注」把大目标拆成知识块，用番茄钟逐个攻克
4. 对难懂的概念，去「讲解」按费曼四步讲一遍并录下来
5. 用「笔记」写康奈尔笔记，复习时用「线索自测」遮住笔记栏回忆
6. 每天完成「今日」页的待复习，「我的 → 学习统计」看积累

---

## 八、已知边界

- 数据不做云同步（这是设计取舍，为了绝对隐私）。换机请用「导出备份」
- 未接入系统日历，提醒只走本地通知
- 录音文件保存在应用私有目录，卸载应用会被清除，重要内容请先导出备份

---

MIT License
