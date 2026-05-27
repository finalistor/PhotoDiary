# 更新日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/) 规范。

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

[1.2]: https://github.com/finalistor/PhotoDiary/releases/tag/v1.2
[1.1]: https://github.com/finalistor/PhotoDiary/releases/tag/v1.1
[1.0]: https://github.com/finalistor/PhotoDiary/releases/tag/v1.0
