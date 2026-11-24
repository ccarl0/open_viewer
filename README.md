# open_viewer

**you no ads viewer** — a simple, open-source file viewer for Android.

## 🚫 NO ADS. Ever.

- open_viewer contains **no ads**, **no trackers**, **no analytics** and **no in-app purchases**.
- The app does **not even request the `INTERNET` permission**, so it physically cannot download ads
  or send your data anywhere. Every file is opened 100% offline, on your device.
- Recently opened files are stored only locally on your phone.

If you ever see an ad in an app called open_viewer, it is not this app.

## Home screen

The screen is split in two horizontal halves:

| Top half | Bottom half |
|---|---|
| **Open recents** – files you opened recently (tap to reopen, long-press to remove, *Clear* to wipe) | **Open by file extension** – every supported file type, grouped by category |

```
Open by file extension
└─ 3D
   └─ .STEP   (.step / .stp)
```

## Supported formats

| Category | Extension | Status |
|---|---|---|
| 3D | `.STEP` / `.stp` | ✅ viewer (rotate, pan, pinch-zoom, toggle edges, fit) |

More categories (e.g. 3D `.stl`, Audio `.mp3`, `.m4a`) are planned. Adding one means writing a viewer
Activity and registering it in `app/src/main/java/com/openviewer/app/FileTypes.kt`.

You can also open `.step` / `.stp` files directly from any file manager ("Open with → open_viewer").

## How the STEP viewer works

STEP files are parsed on-device by [OpenCascade](https://dev.opencascade.org/) compiled to WebAssembly
([occt-import-js](https://github.com/kovacsv/occt-import-js)) and rendered with
[three.js](https://threejs.org/), inside a WebView. All of it is bundled in
`app/src/main/assets/viewer/` — nothing is fetched from the network.

## Build

Requirements: JDK 17 and the Android SDK (API 34).

```bash
./gradlew assembleDebug
```

The APK ends up in `app/build/outputs/apk/debug/`. GitHub Actions also builds it on every push
(see the *Build APK* workflow artifacts).

## Licenses

- open_viewer source: see repository license.
- occt-import-js / OpenCascade: LGPL-2.1 (see `app/src/main/assets/viewer/license.*.txt`).
- three.js: MIT (see `app/src/main/assets/viewer/three/LICENSE.txt`).
