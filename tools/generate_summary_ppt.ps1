$ErrorActionPreference = "Stop"

param(
    [string]$OutputPath = "D:\beiwojianshenapp\被窝健身_0-1VibeCoding总结.pptx"
)

$ppLayoutTitle = 1
$ppLayoutText = 2

function Add-TitleSlide($pres, [string]$title, [string]$subtitle) {
    $slide = $pres.Slides.Add($pres.Slides.Count + 1, $ppLayoutTitle)
    $slide.Shapes.Title.TextFrame.TextRange.Text = $title
    $slide.Shapes.Item(2).TextFrame.TextRange.Text = $subtitle
}

function Add-TextSlide($pres, [string]$title, [string[]]$lines) {
    $slide = $pres.Slides.Add($pres.Slides.Count + 1, $ppLayoutText)
    $slide.Shapes.Title.TextFrame.TextRange.Text = $title
    $body = $slide.Shapes.Item(2).TextFrame.TextRange
    $body.Text = ($lines -join "`r")
    $body.ParagraphFormat.Bullet.Visible = -1
}

$outDir = Split-Path -Parent $OutputPath
if ($outDir -and -not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

$ppt = $null
$pres = $null

try {
    $ppt = New-Object -ComObject PowerPoint.Application
    $pres = $ppt.Presentations.Add()

    Add-TitleSlide $pres "被窝健身（Android）0→1 Vibe Coding 总结" "项目：被窝健身`r设备：小米 15 真机`r日期：2026-03-23"

    Add-TextSlide $pres "目录" @(
        "明白需求",
        "准备工作",
        "修改 bug",
        "不足与优化"
    )

    Add-TextSlide $pres "1. 明白需求：目标与边界" @(
        "核心目标：记录训练动作、重量/组数、有氧、体重",
        "趋势展示：体重折线图 + 动作折线图（当日最大重量）",
        "交互主线：首页日历 → 选日期 → 当日记录（训练/有氧/体重）",
        "动作管理：动作库 + 五大分类（胸/背/肩/胳膊/腿）",
        "V1 边界：离线本地存储优先，不做账号/云同步/社交"
    )

    Add-TextSlide $pres "1. 明白需求：关键页面与数据" @(
        "首页：月历网格；日期下方显示当天训练涉及部位（胸/背/肩/臂/腿）",
        "当日记录页：训练/有氧/体重三栏，支持新增/编辑/删除",
        "图表页：范围切换（7/30/90/全部）+ 点击节点显示数值",
        "数据模型：动作(Exercise) / 训练(StrengthEntry) / 有氧(Cardio) / 体重(Weight)"
    )

    Add-TextSlide $pres "2. 准备工作：开发环境与真机" @(
        "JDK 17 + Android Studio（D:\jdk17、D:\andraw studio）",
        "Android SDK 单独目录（避免放在 Studio 安装目录）",
        "小米 15：开启开发者模式、USB 调试/USB 安装",
        "连接验证：ADB 识别设备并可 installDebug 安装运行"
    )

    Add-TextSlide $pres "2. 准备工作：项目结构与技术选型" @(
        "语言与 UI：Kotlin + XML(Views) + Material 组件",
        "本地存储：Room（离线可用、重启不丢数据）",
        "列表：RecyclerView；分栏：ViewPager2 + TabLayout",
        "图表：自绘轻量折线图（便于快速迭代）"
    )

    Add-TextSlide $pres "3. 修改 bug：典型问题与修复" @(
        "AndroidX 依赖报错：补齐 gradle.properties 开关",
        "Manifest 资源缺失：清理未创建的引用，保证可编译运行",
        "动作库 FAB 位置异常：统一锚定到右下角",
        "折线图可用性：点击节点提示日期与重量",
        "日历体验：恢复“今天”高亮；仅显示当月日期"
    )

    Add-TextSlide $pres "3. 修改 bug：需求迭代与迁移" @(
        "动作分类：为动作表增加 category 字段并做数据库迁移（按名称自动猜分类）",
        "训练选择：新增训练时按分类筛选动作；列表按分类分组展示",
        "首页日历：按天汇总训练部位，显示在日期下方",
        "应用名称：提供“修改名称”入口（App 内标题/首页标题可变）"
    )

    Add-TextSlide $pres "4. 不足与优化：现状不足" @(
        "UI 仍偏工程化原型：视觉规范、间距、字体一致性可加强",
        "缺少导出/备份：目前仅本地存储，换机不便",
        "缺少自动化测试：回归主要靠真机手测",
        "图表能力有限：缺少缩放、浮层提示、更多指标",
        "架构可升级：逐步引入 MVVM/Flow，降低耦合"
    )

    Add-TextSlide $pres "4. 不足与优化：下一步路线" @(
        "体验：选中日期高亮、分类标签更美观、输入校验与错误提示更友好",
        "数据：CSV/JSON 导出、备份/恢复、（可选）云同步",
        "图表：引入成熟图表库或增强自绘（浮层、缩放、指示线）",
        "性能：大数据量分页/缓存、数据库索引与统计优化",
        "工程：统一代码结构、增加单元/仪表测试、CI 构建"
    )

    $pres.SaveAs($OutputPath)
} finally {
    if ($pres -ne $null) { $pres.Close() | Out-Null }
    if ($ppt -ne $null) { $ppt.Quit() | Out-Null }
}

Write-Host "PPT 已生成：$OutputPath"

