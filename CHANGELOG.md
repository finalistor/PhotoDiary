# 更新日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/) 规范。

## [1.4.0] — 2026-06-01

### 新增
- 自定义主题色：在选择主题面板中选择"自定义"，通过色调/饱和度/亮度滑块自由选择任意颜色作为主题色
- 常用颜色快捷选择：自定义模式下提供 35 种预设颜色格子快速选取

### 变更
- 更新应用图标

## [1.3.1] — 2026-05-30

### 修复
- 分享按钮在生成图片失败后永久禁用的问题（`try-finally` 确保 `isSharing` 重置）
- `deleteEntry` 先删文件再删数据库记录，若 DB 操作失败导致文件已丢失不可恢复（改为先删 DB 再删文件）

### 优化
- EntryDetailUiState 添加 `@Immutable` 注解，提升 Compose 稳定性
- 编辑器标签列表 `remember` 键移除多余的 `uiState.tags`，避免每次标签切换重建所有 chip
- 合并 `updateEntryWithPhotos` 中删除旧照片的两次迭代为一次
- ShimmerPlaceholder 颜色列表提取为顶层常量，避免每帧重新分配
- 移除未使用的 `PhotoDao.insertPhoto` 方法
- 移除 CalendarUiState / TimelineUiState 中未使用的 `today` 字段
- 移除 CreateEditEntryViewModel 中仅赋值不读取的 `initialCreatedAt`
- 保存失败时 `isSaving = false` 提取到 `finally` 块，消除重复
- DiaryRepository 接口移除冗余 `: Unit` 返回类型

## [1.3] — 2026-05-29

### 修复
- 修复通过日历点击创建日记时，`createdAt` 存储为午夜零点而非实际创建时间，导致时间显示始终为 00:00 的问题
- 修复 DatePicker 选择日期后会覆盖真实创建时间戳的问题
- 修复编辑已有日记时，日期选择器默认显示当天日期而非日记原始日期的问题
- 修复仅修改文本内容保存时，`createdAt` 被 `selectedEntryDate` 覆盖的问题（现分离 `createdAt` 和 `entryDate` 字段）

### 新增
- 多套预设主题配色：赤陶（默认）、海洋蓝、森林绿、薰衣草、日落橙、黑白，含浅色/深色自适应
- 主题选择器：在时间线顶栏调色板图标打开底部面板，点击色块即时切换
- 主题切换动画：`animateColorAsState` 实现 500ms 平滑颜色过渡
- 图片加载闪烁占位符（ShimmerPlaceholder），用于缩略图和照片网格

### 变更
- 导航过渡动画改用 `spring()` 替代 `tween()`，手感更自然
- 日历月份切换动画改用弹性动画
- 搜索栏切换添加垂直滑入滑出过渡
- 列表项添加交错入场动画（标签筛选、标签管理、照片墙）
- 日历日期单元格添加按压缩放反馈（0.92x + 弹性回弹）
- FAB 显示/隐藏改用弹性动画
- 日记预览卡片移除日期时间显示（精简信息密度）

## [1.2] — 2026-05-27

### 新增
- FAB 改为日历选择器：点击加号展示日历，选择日期新建或编辑已有日记
- 日期冲突智能跳转：编辑日期时若目标日期已有日记，弹出提示（保存并查看 / 放弃并查看 / 跳转并修改）
- 新建日记时若当天已有日记，自动跳转编辑

### 修复
- 文本编辑保存后 `entry_date` 被非归一化时间戳覆盖，导致日期查询失败
- 新建日记保存后没有回到主界面的导航问题

## [1.1] — 2026-05-26

### 新增
- 一天最多一篇日记约束：数据库层 `entry_date` UNIQUE 索引 + 仓库层冲突检查
- 新建日记时自动以日期作为默认标题（格式：yyyy年M月d日）

### 变更
- 日历点击简化：有日记则直接进入详情，不再显示多条目选择弹窗
- 移除 `EntryPickerDialog` 组件
- `CalendarDay.buildCalendarDays()` 改用 `entryDate` 分组
- 桌面小组件改用 `entryDate` 精确查询

### 修复
- 日历标题栏周日放在第一列，修复标题与日期列的错位问题

## [1.0] — 2026-05-25

### 新增
- 时间线主页：嵌入式月历 + 近期日记列表 + 搜索 + 下拉刷新
- 全屏日历视图：支持月份切换、多条目选择器、缩略图预览
- 日记编辑：标题、内容、照片（拍照/相册）、标签、日期选择器
- 日记详情：照片网格、标签展示、分享为图片
- 照片墙：按年份分组，月份分组展示
- 照片查看器：全屏 HorizontalPager 浏览
- 标签管理：预设标签（旅行/美食/日常/工作）+ 自定义标签，支持颜色
- 标签筛选：点击标签查看关联日记
- 搜索：标题和内容 LIKE 搜索
- 桌面小组件：今日日记概览（4×2）
- 主题切换：浅色/深色/跟随系统，Terracotta 暖色调
- Sentry 崩溃上报（release 版本）
- README.md 项目首页文档
- MIT 开源许可证

[1.4.0]: https://github.com/finalistor/PhotoDiary/releases/tag/v1.4.0
[1.3]: https://github.com/finalistor/PhotoDiary/releases/tag/v1.3
[1.2]: https://github.com/finalistor/PhotoDiary/releases/tag/v1.2
[1.1]: https://github.com/finalistor/PhotoDiary/releases/tag/v1.1
[1.0]: https://github.com/finalistor/PhotoDiary/releases/tag/v1.0
