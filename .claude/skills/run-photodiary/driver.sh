#!/usr/bin/env bash
# PhotoDiary driver — builds, installs, launches, and drives the Android app via ADB.
# Usage: source driver.sh && <function>
# Screen: 1080×2400, 420dpi
# App is single-Activity (Compose Navigation). current_screen() always shows
# MainActivity; use ui_text() / ui_desc() to determine the current composable.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
APK="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
PKG="com.photodiary"
ACTIVITY="$PKG/.MainActivity"
SCREENSHOT_DIR="$SCRIPT_DIR/screenshots"

# ── Setup ────────────────────────────────────────────────────────────────

_require_device() {
  if ! adb devices | grep -q 'device$'; then
    echo "ERROR: No device connected. Connect one or start the emulator." >&2
    echo "  emulator -avd Pixel_7 -no-window -no-audio -no-boot-anim &" >&2
    return 1
  fi
}

# ── Build & Install ──────────────────────────────────────────────────────

build() {
  cd "$PROJECT_DIR"
  ./gradlew assembleDebug
}

install() {
  _require_device
  adb install -r "$APK"
}

# ── Launch ────────────────────────────────────────────────────────────────

launch() {
  _require_device
  adb shell am start -n "$ACTIVITY"
  sleep 3
}

stop() {
  _require_device
  adb shell am force-stop "$PKG"
}

restart() {
  stop
  launch
}

# ── Screenshot ────────────────────────────────────────────────────────────

ss() {
  local name="${1:-screenshot}"
  mkdir -p "$SCREENSHOT_DIR"
  local path="$SCREENSHOT_DIR/${name}.png"
  adb exec-out screencap -p > "$path"
  echo "Screenshot: $path"
}

# ── Input ─────────────────────────────────────────────────────────────────

tap() {
  _require_device
  adb shell input tap "$1" "$2"
}

swipe() {
  _require_device
  adb shell input swipe "$1" "$2" "$3" "$4" "${5:-300}"
}

key() {
  _require_device
  adb shell input keyevent "$1"
}

text() {
  _require_device
  adb shell input text "$1"
}

# ── UI introspection ──────────────────────────────────────────────────────

_dump() {
  adb shell uiautomator dump 2>&1 > /dev/null
  adb shell "cat /sdcard/window_dump.xml" 2>&1
}

ui_text() {
  _dump | grep -oP 'text="[^"]*"' | grep -v 'text=""' | sort -u
}

ui_desc() {
  _dump | grep -oP 'content-desc="[^"]*"' | grep -v 'content-desc=""' | sort -u
}

on_screen() {
  # Check if a text or content-desc string is present on screen
  _dump | grep -q "$1" && echo "yes" || echo "no"
}

# ── App state ─────────────────────────────────────────────────────────────

is_running() {
  adb shell dumpsys window | grep -q "$PKG" && echo "yes" || echo "no"
}

current_screen() {
  adb shell dumpsys window | grep mCurrentFocus | grep -oP '[^/]+(?=})' | tail -1
}

# ── PhotoDiary-specific helpers ───────────────────────────────────────────

# Timeline screen elements (1080×2400)
FAB="964 2221"           # content-desc="新建日记"
SEARCH_ICON="503 221"    # content-desc="搜索"
THEME_ICON="629 221"     # content-desc="切换主题"
PHOTOWALL_ICON="755 221" # content-desc="照片墙"
TAGMGR_ICON="881 221"    # content-desc="标签管理"
ABOUT_ICON="1007 221"    # content-desc="关于"

# Create/Edit entry screen elements
CANCEL_BTN="87 221"      # text="取消"
SAVE_BTN="969 221"       # text="保存"
TITLE_FIELD="540 420"    # EditText placeholder="标题"
CONTENT_FIELD="540 1180" # EditText placeholder="写点什么..."
DATE_PICKER="540 598"    # content-desc="选择日期"

fab() { tap $FAB; }
search() { tap $SEARCH_ICON; }
cancel() { tap $CANCEL_BTN; }
save() { tap $SAVE_BTN; }

create_entry() {
  # Create a diary entry: title and optional content
  local title="$1"
  local content="${2:-}"
  fab
  sleep 2
  tap $TITLE_FIELD
  sleep 1
  text "$title"
  if [ -n "$content" ]; then
    tap $CONTENT_FIELD
    sleep 1
    text "$content"
  fi
  save
  sleep 2
}

# ── Convenience: full cycle ───────────────────────────────────────────────

smoke() {
  echo "=== Building ===" && build
  echo "=== Installing ===" && install
  echo "=== Launching ===" && launch
  echo "=== Screenshot (timeline) ===" && ss "timeline"
  echo "=== Creating test entry ===" && create_entry "Smoke Test" "Hello from driver.sh"
  echo "=== Screenshot (after save) ===" && ss "after_save"
  echo "=== Done ==="
}
