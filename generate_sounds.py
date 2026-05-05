"""
Generate three short WAV sound effects for the Todo app and save them as
PCM WAV files in the target raw resource directory.

Requirements: Python 3.8+ with built-in `wave` module only (no extra libs).

Run from the project root:
    python generate_sounds.py
"""

import wave
import struct
import math
import os
from typing import List

OUTPUT_DIR = os.path.join(
    "app", "src", "main", "res", "raw"
)
os.makedirs(OUTPUT_DIR, exist_ok=True)

SAMPLE_RATE = 44100
CHANNELS    = 1
SAMPLE_WIDTH = 2  # 16-bit


def write_wav(filename, frames, sample_rate=SAMPLE_RATE):
    """Write a list of normalised float samples [-1.0, 1.0] as a 16-bit mono WAV."""
    path = os.path.join(OUTPUT_DIR, filename)
    with wave.open(path, "w") as wf:
        wf.setnchannels(CHANNELS)
        wf.setsampwidth(SAMPLE_WIDTH)
        wf.setframerate(sample_rate)
        packed = struct.pack(f"<{len(frames)}h",
                             *[int(s * 32767) for s in frames])
        wf.writeframes(packed)
    print(f"  Written: {path}  ({len(frames)} samples, {len(frames)/sample_rate*1000:.0f} ms)")


def sine(freq, duration, amp=0.6, sr=SAMPLE_RATE):
    n = int(sr * duration)
    return [amp * math.sin(2 * math.pi * freq * i / sr) for i in range(n)]


def envelope(samples, attack=0.01, release=0.1, sr=SAMPLE_RATE):
    """Apply a simple linear attack/release envelope."""
    n      = len(samples)
    atk    = int(attack  * sr)
    rel    = int(release * sr)
    result = []
    for i, s in enumerate(samples):
        if i < atk:
            gain = i / atk
        elif i > n - rel:
            gain = (n - i) / rel
        else:
            gain = 1.0
        result.append(s * gain)
    return result


def mix(*tracks):
    """Mix multiple tracks of the same length together, normalised."""
    length = max(len(t) for t in tracks)
    mixed  = [0.0] * length
    for t in tracks:
        for i, s in enumerate(t):
            mixed[i] += s
    peak = max(abs(s) for s in mixed) or 1.0
    return [s / peak * 0.85 for s in mixed]


# ── sound_add.wav — soft double high beep ────────────────────────────────────
print("Generating sound_add.wav …")
note1 = envelope(sine(1046.50, 0.06, amp=0.4))   # C6
note2 = envelope(sine(1046.50, 0.10, amp=0.5))   # C6
silence = [0.0] * int(SAMPLE_RATE * 0.05)
add_frames = note1 + silence + note2
write_wav("sound_add.wav", add_frames)


# ── sound_complete.wav — satisfying two-tone ding ─────────────────────────────
print("Generating sound_complete.wav …")
ding1 = envelope(sine(880.0, 0.10, amp=0.55), release=0.09)  # A5
ding2 = envelope(sine(1108.73, 0.18, amp=0.45), release=0.16) # C#6
gap   = [0.0] * int(SAMPLE_RATE * 0.03)
complete_frames = ding1 + gap + ding2
write_wav("sound_complete.wav", complete_frames)


# ── sound_delete.wav — descending low synth ────────────────────────────────────────
print("Generating sound_delete.wav …")
note1 = envelope(sine(220.0, 0.08, amp=0.6), release=0.07)  # A3
note2 = envelope(sine(164.81, 0.12, amp=0.6), release=0.1)  # E3
delete_frames = note1 + note2
write_wav("sound_delete.wav", delete_frames)


# ── sound_splash.wav — 2.5 second ambient intro chord ────────────────────────
print("Generating sound_splash.wav …")
# A majestic Cmaj9 chord that fades in and out smoothly
c_note = sine(261.63, 2.5, amp=0.25)
e_note = sine(329.63, 2.5, amp=0.25)
g_note = sine(392.00, 2.5, amp=0.25)
b_note = sine(493.88, 2.5, amp=0.2)
chord = mix(c_note, e_note, g_note, b_note)
# Long attack and release for a swelling pad effect
splash_frames = envelope(chord, attack=0.5, release=1.5)
write_wav("sound_splash.wav", splash_frames)

print("\nAll sound files generated successfully!")
