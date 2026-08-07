# XGram logo

The XGram mark combines an **X** with a paper plane, using the app's cyan and midnight-blue liquid-glass palette.

- `xgram-logo.svg` is the scalable source artwork.
- `xgram-logo.png` is the 1024 px preview/export.
- `generate_logo.py` recreates the preview and Android legacy launcher PNGs with Pillow.
- Android 8+ adaptive icon layers live in `app/src/main/res/drawable/ic_launcher_*.xml`.

To regenerate the PNG exports:

```bash
python3 branding/generate_logo.py
```
