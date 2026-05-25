---
name: run-photodiary
description: Build, install, launch, and drive the PhotoDiary Android app via ADB. Use to run, screenshot, create entries, or test UI flows on an emulator or device.
---

# PhotoDiary — Android Photo Diary App

Kotlin + Jetpack Compose + Room + Material3. Single-Activity (Compose
Navigation), manual DI, MVVM. Min SDK 29, target 34.

**All paths relative to `PhotoDiary/` (the project root).**

The driver at `.claude/skills/run-photodiary/driver.sh` wraps ADB with
app-specific helpers. Source it and call its functions.

## Prerequisites

```bash
# Android SDK at C:/Users/87747/AppData/Local/Android/Sdk
# Java 21 (OpenJDK), ADB in PATH
adb version
```

## Build

```bash
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

## Run (agent path)

Source the driver and drive the app:

```bash
source .claude/skills/run-photodiary/driver.sh

# Start emulator if no device is connected
adb devices
# If empty:
emulator -avd Pixel_7 -no-window -no-audio -no-boot-anim &
# Wait ~30s, then verify:
adb devices  # should show emulator-5554

# Build, install, launch
build
install
launch

# Interact
ss "timeline"                          # screenshot → screenshots/timeline.png
fab                                    # tap FAB → create entry screen
cancel                                 # go back (取消 button)
tap $SEARCH_ICON                       # open search (503, 221)
key KEYCODE_BACK                       # close search
tap $TITLE_FIELD && text "My Title"    # type into title field

# Create a full entry
create_entry "My Entry" "Some content here"

# Introspect UI
ui_text                                # all visible text strings
ui_desc                                # all content-descriptions
on_screen "新建日记"                    # check if string is on screen

# Stop
stop
```

### Screen coordinates (1080×2400, 420dpi)

**Timeline screen:**

| Element | Variable | Coordinates | Bounds |
|---------|----------|------------|--------|
| FAB (新建日记) | `$FAB` | `964 2221` | [891,2148][1038,2295] |
| Search icon | `$SEARCH_ICON` | `503 221` | [440,158][566,284] |
| Theme toggle | `$THEME_ICON` | `629 221` | [566,158][692,284] |
| Photo wall | `$PHOTOWALL_ICON` | `755 221` | [692,158][818,284] |
| Tag management | `$TAGMGR_ICON` | `881 221` | [818,158][944,284] |
| About | `$ABOUT_ICON` | `1007 221` | [944,158][1070,284] |

**Create/Edit entry screen:**

| Element | Variable | Coordinates | Bounds |
|---------|----------|------------|--------|
| Cancel (取消) | `$CANCEL_BTN` | `87 221` | [11,158][163,284] |
| Save (保存) | `$SAVE_BTN` | `969 221` | [869,158][1069,284] |
| Title field | `$TITLE_FIELD` | `540 420` | [53,336][1027,504] |
| Content field | `$CONTENT_FIELD` | `540 1180` | [53,943][1027,1416] |
| Date picker | `$DATE_PICKER` | `540 598` | [53,535][1027,661] |

## Device / emulator management

```bash
# List AVDs
ls C:/Users/87747/AppData/Local/Android/Sdk/emulator/

# Start headless
emulator -avd Pixel_7 -no-window -no-audio -no-boot-anim &

# Kill emulator
adb -s emulator-5554 emu kill

# Connect to MuMu emulator (if running on host)
adb connect 127.0.0.1:5555

# List connected devices
adb devices
```

## Direct invocation (unit tests)

```bash
./gradlew test
./gradlew test --tests "com.photodiary.data.local.dao.DiaryEntryDaoTest"
```

## Gotchas

- **Single Activity, always.** `current_screen()` / `dumpsys window` always
  shows `com.photodiary.MainActivity` regardless of which Compose screen is
  visible. Use `ui_text()` or `on_screen "<keyword>"` to determine the
  current composable screen instead.
- **Emulator cold boot takes ~30s.** The emulator may fail to load a
  snapshot ("different renderer configured") and start from scratch. Wait
  until `adb devices` shows the device.
- **Compose UI lacks resource-ids.** All Compose views show as
  `android.view.View` with generic class names. Use text content and
  content-descriptions to identify elements (e.g., `content-desc="新建日记"`
  for the FAB).
- **Text input needs field focus.** Tap the target EditText first,
  wait 1s, then use `adb shell input text`. Without a prior tap, text goes
  nowhere.
- **ADB path handling in Git Bash.** Use forward slashes for file
  operations (e.g., `adb exec-out screencap -p > /e/work/.../file.png`).
  The `adb shell "cat /sdcard/..."` command needs quoting to prevent Git
  Bash from intercepting the path.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `adb: no devices/emulators found` | Start emulator or connect device |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | `adb uninstall com.photodiary` then re-install |
| Emulator stuck at boot animation | `adb -s emulator-5554 emu kill` and restart |
| `Error type 3` on `am start` | Use full package: `com.photodiary/.MainActivity` |
| Screenshot is black/blank | App may be in background; run `launch` first |
| Tap does nothing | Re-dump UI with `_dump` and check current bounds |
| Build fails (Java version) | Ensure Java 21 is in PATH |
