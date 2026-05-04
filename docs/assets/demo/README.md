# Demo GIF Placeholder

Replace this placeholder with an actual screen recording demonstrating:
1. App launch → Dashboard
2. Selecting "Compose Chat"
3. Chat session connecting
4. Asking a question that triggers a tool call
5. The approval flow (if applicable)
6. Tool result displayed in chat

## Recommended specs

- Format: GIF or MP4
- Resolution: 1080×2280 (or 720×1280 for smaller file size)
- Duration: 15-30 seconds
- File name: `resolvekit-demo.gif` (update README.md if using .mp4)

## How to record

```bash
# Record via ADB
adb shell screenrecord /sdcard/demo.mp4 --time-limit 30
adb pull /sdcard/demo.mp4 docs/assets/demo/

# Convert to GIF with ffmpeg
ffmpeg -i demo.mp4 -vf "fps=15,scale=540:-1" -loop 0 resolvekit-demo.gif
```
