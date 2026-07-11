# 会话总结 — DuJi 项目

> 会话时间：2026-06-XX  
> 项目：DuJi（Android / Kotlin + Jetpack Compose + Miuix）  
> 远端参考仓库：https://github.com/QNquenan/DuJi（Flutter 版）

---

## 一、会话概况

本次会话围绕 DuJi Android 项目进行了多轮迭代，核心目标是将"那些日子"（ThoseDays）从仅有表单 UI 的草稿页，完善为完整的可用功能模块，并同步调整了多个 UI 细节和项目规则。

---

## 二、当前项目状态（会话结束时）

### 技术栈

| 层面 | 技术选型 |
|------|---------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Miuix KMP |
| 持久化 | DataStore Preferences |
| 农历 | `cn.6tail:lunar:1.7.4` |
| 构建 | Gradle Kotlin DSL |

### 页面结构

- **MainActivity** — 单 Activity，HorizontalPager 三页导航
  - 我的物品（MyItemsScreen）— 完整功能
  - **那些日子（ThoseDaysScreen）— 已完整** ✅
  - 设置（SettingsScreen）— 占位

### 目录结构

```
app/src/main/java/com/quenan/duji/
├── MainActivity.kt
├── data/
│   ├── item/           # 物品数据层
│   │   ├── ItemModels.kt
│   │   ├── ItemRepository.kt
│   └── day/            # 日子数据层（本次新增）
│       ├── DayModels.kt
│       └── DayRepository.kt
├── ui/
│   ├── component/      # 公共组件
│   ├── screen/
│   │   ├── MyItemsScreen.kt
│   │   ├── MyItemsViewModel.kt
│   │   ├── ThoseDaysScreen.kt      # 主页面（本次大幅改造）
│   │   ├── ThoseDaysViewModel.kt   # 本次新增
│   │   ├── ThoseDaysUi.kt          # 本次新增
│   │   └── SettingsScreen.kt
│   ├── theme/
│   └── util/
│       └── LunarCalendar.kt        # 农历工具（本次新增）
```

---

## 三、本次会话完成的改动（按时间顺序）

### 1. 卡片内边距修正

**文件**：`ThoseDaysScreen.kt`

- 移除 Card 的 `insideMargin = PaddingValues(16.dp)`，改为 `PaddingValues(0.dp)`
- 内部输入框改为自身用 `padding(horizontal = 16.dp, vertical = 12.dp)` 撑开
- 目的：让 WindowDropdownPreference 的点击遮罩层能覆盖到卡片边缘

### 2. 远端项目解析

**URL**：`https://github.com/QNquenan/DuJi`

- 确认远端主线是 Flutter 项目，与本地 Android Compose 不是同一技术栈
- 远端 Flutter 版功能完整度更高：装备、倒数日、农历、设置、小组件联动

### 3. 倒数日表单逻辑迁移

**文件**：`ThoseDaysScreen.kt`

- 类型切换时，生日自动固定为"每年"
- 新增 `syncDateLabel()` 统一日期文案
- 新增 `applyTypeChange()` / `applyRepeatCycleChange()`
- 新增每周选择对话框（`showWeekDaysDialog`）
- 新增每月选择对话框（`showMonthDaysDialog`）
- 日期选择入口根据重复周期分支到不同对话框
- 确认按钮增加基础校验（名称、日期、每周/每月选择）

### 4. 每月日期网格化

**文件**：`ThoseDaysScreen.kt`

- 每月选择从 FlowRow 改为 7 列 LazyVerticalGrid，保证同宽整齐

### 5. 农历选择逻辑迁移

**新增文件**：
- `LunarCalendar.kt` — 农历/公历互转工具
- 新增 Gradle 依赖 `cn.6tail:lunar:1.7.4`

**文件**：`ThoseDaysScreen.kt`

- 日期选择对话框新增 Miuix TabRow 公历/农历双模式
- 农历模式下支持年/月/日选择
- 确认时做真实农历→公历转换
- 文案支持农历显示

### 6. TabRowWithContour 替换

**文件**：`ThoseDaysScreen.kt`

- 日期 tab 从 `TabRow` 换成 `TabRowWithContour`
- 使用 `tabs = listOf("公历", "农历")` 语法

### 7. WindowDropdownPreference 内边距

**文件**：`ThoseDaysScreen.kt`

- 类型和重复周期两个 WindowDropdownPreference 增加 `modifier = Modifier.padding(horizontal = 16.dp)`

### 8. 添加物品图标底色

**文件**：`MyItemsScreen.kt`

- 选择图标的未选中态从透明改为 `secondaryContainer`

### 9. 添加日子自定义表情修复

**文件**：`ThoseDaysScreen.kt`

- 新增 `showCustomEmojiDialog` 和 `customEmojiText` 状态
- 新增自定义表情输入对话框
- "自定义"按钮改为打开输入对话框，而非直接关闭

### 10. 表情选择删除文字注释

**文件**：`ThoseDaysScreen.kt`

- 选择表情的格子从"表情 + 文字注释"改为仅显示表情

### 11. 项目规则新增

**文件**：`REASONIX.md`

- 新增规则 4：先提问澄清需求（一次只问一个问题，持续追问至 95% 确信）

### 12. 日子完整功能实现（核心改动）

**新增文件**：

| 文件 | 说明 |
|------|------|
| `data/day/DayModels.kt` | 日子数据模型、枚举、倒数状态计算逻辑 |
| `data/day/DayRepository.kt` | DataStore 持久化，支持 observe/add/update/delete |
| `ui/screen/ThoseDaysViewModel.kt` | ViewModel，支持列表排序、视图模式切换 |
| `ui/screen/ThoseDaysUi.kt` | DayListItem / DayGridItem 组件 |

**文件**：`ThoseDaysScreen.kt`

- 从纯说明页改造成真实列表页
- 空状态页面
- 列表 / 网格切换
- FAB 新增
- 详情 bottom sheet
- 编辑回填
- 删除确认对话框
- 接入 ViewModel 数据流

**数据模型字段**：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 唯一标识 |
| emoji | String | 表情 |
| emojiName | String | 表情名称 |
| name | String | 日子名称 |
| type | DayType | 倒/正数日 / 纪念日 / 生日 |
| repeatCycle | RepeatCycle | 不重复/每周/每月/每年 |
| targetDate | String | 目标日期（公历 ISO） |
| note | String | 备注 |
| weekDays | List<Int> | 每周重复选中的星期 |
| monthDays | List<Int> | 每月重复选中的日期 |
| isLunar | Boolean | 是否使用农历 |
| isPinned | Boolean | 是否置顶 |
| createdAt | Long | 创建时间 |

**倒数逻辑支持**：
- 普通倒数/正数
- 纪念日周年计算
- 生日动态计算
- 每周重复
- 每月重复
- 农历每年重复

---

## 四、Git 提交记录

| 提交号 | 信息 | 说明 |
|--------|------|------|
| `17f4b9b` | `feat: 补齐倒数日表单逻辑并更新规则` | 表单逻辑迁移 + REASONIX.md 规则 + Gradle 升级 |
| `4c7a07d` | `feat: 补齐农历日期选择与每月网格布局` | 农历工具、依赖、Tab 切换 |
| `445d04a` | `style: 调整日期切换tab与下拉项边距` | TabRowWithContour + WindowDropdownPreference 内边距 |
| `24584f5` | `fix: 修复表情自定义与图标底色` | 自定义表情对话框 + 图标底色 |
| `fe29dd0` | `style: 精简日子表情选择展示` | 删除表情文字注释 |
| `12ad531` | `docs: 增加需求澄清提问规则` | 规则 4 |
| `e60157b` | `refactor: 统一表情选择样式与按钮布局` | 表情格子样式统一 |
| `3458e5f` | `refactor: 统一图标选择框架与按钮布局` | 图标选择网格化 |
| `6a42359` | `feat: 完善日子完整功能` | 核心改动：完整日子功能 |

---

## 五、当前仍存在的问题 / 可继续的方向

1. 表单校验失败时没有错误提示文案（远端有 toast/ snackbar）
2. 日子列表/详情页的视觉细节可进一步贴近"我的物品"
3. 设置页仍为占位状态
4. 日子暂不支持搜索/排序设置入口
5. 详情页字段顺序可微调
6. 删除/保存成功提示未补齐

---

## 六、构建验证

每次改动后均通过：
```
./gradlew.bat :app:compileDebugKotlin
```
最后一次验证结果：`BUILD SUCCESSFUL`

---

## 七、关键文件索引

| 文件路径 | 说明 | 行数 |
|----------|------|------|
| `app/src/main/.../screen/ThoseDaysScreen.kt` | 日子主页面 | ~940 |
| `app/src/main/.../screen/ThoseDaysUi.kt` | 日子列表/网格组件 | ~190 |
| `app/src/main/.../screen/ThoseDaysViewModel.kt` | 日子 ViewModel | ~80 |
| `app/src/main/.../data/day/DayModels.kt` | 日子模型与倒数逻辑 | ~200 |
| `app/src/main/.../data/day/DayRepository.kt` | 日子持久化 | ~110 |
| `app/src/main/.../ui/util/LunarCalendar.kt` | 农历工具 | ~60 |
| `app/src/main/.../screen/MyItemsScreen.kt` | 物品主页面 | ~1270 |
| `app/src/main/.../screen/MyItemsViewModel.kt` | 物品 ViewModel | ~130 |
| `app/src/main/.../data/item/ItemModels.kt` | 物品模型 | ~50 |
| `app/src/main/.../data/item/ItemRepository.kt` | 物品持久化 | ~100 |
| `REASONIX.md` | 项目工作流规则 | 22 |
| `gradle/libs.versions.toml` | 依赖版本目录 | 43 |
| `app/build.gradle.kts` | 模块构建配置 | 66 |