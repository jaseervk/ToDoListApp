"""
Generate minimal valid PNG launcher icons for all mipmap densities.
Uses only Python stdlib (struct, zlib) — no Pillow needed.

Run from project root:
    python generate_icons.py
"""
import struct, zlib, os

def make_png(width, height, r, g, b):
    """Create an uncompressed solid-colour RGBA PNG."""
    def chunk(name, data):
        c = struct.pack('>I', len(data)) + name + data
        return c + struct.pack('>I', zlib.crc32(c[4:]) & 0xFFFFFFFF)

    signature = b'\x89PNG\r\n\x1a\n'
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 2, 0, 0, 0)  # 8-bit RGB
    ihdr = chunk(b'IHDR', ihdr_data)

    # Build raw scanlines: filter byte (0x00) + RGB * width per row
    row = b'\x00' + bytes([r, g, b]) * width
    raw = row * height
    idat = chunk(b'IDAT', zlib.compress(raw))
    iend = chunk(b'IEND', b'')
    return signature + ihdr + idat + iend

densities = {
    'mipmap-mdpi':    48,
    'mipmap-hdpi':    72,
    'mipmap-xhdpi':   96,
    'mipmap-xxhdpi':  144,
    'mipmap-xxxhdpi': 192,
}

BASE = os.path.join('app', 'src', 'main', 'res')
R, G, B = 0x6C, 0x63, 0xFF  # brand violet

for folder, size in densities.items():
    path = os.path.join(BASE, folder)
    os.makedirs(path, exist_ok=True)
    png = make_png(size, size, R, G, B)
    for name in ('ic_launcher.png', 'ic_launcher_round.png'):
        fpath = os.path.join(path, name)
        with open(fpath, 'wb') as f:
            f.write(png)
        print(f'  {fpath}  ({size}x{size})')

print('\nAll icons generated!')
