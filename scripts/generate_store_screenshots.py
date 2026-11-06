#!/usr/bin/env python3
"""
Generate new store screenshots:
4_cyberpunk_palettes.png (Split or showcase of Cyan, Crimson, Amber, Violet, Monochrome)
5_special_drops.png (Monochrome background + Red Virus drop + Gold Neo drop + MATRIX hidden message + clean lockscreen clock)
"""

import math
import os
import random
from PIL import Image, ImageDraw, ImageFont, ImageFilter

random.seed(1337)

FONT_MENLO = "/System/Library/Fonts/Menlo.ttc"
FONT_TECH = "app/src/main/assets/fonts/share_tech_mono.ttf"

def get_font(size, tech=False):
    path = FONT_TECH if tech and os.path.exists(FONT_TECH) else FONT_MENLO
    try:
        return ImageFont.truetype(path, size)
    except Exception:
        return ImageFont.load_default()

def get_random_matrix_char(rng):
    t = rng.randint(0, 100)
    if t < 70:
        return chr(0xFF66 + rng.randint(0, 0xFF9D - 0xFF66))
    elif t < 85:
        return chr(ord('0') + rng.randint(0, 9))
    else:
        return chr(0x21 + rng.randint(0, 89))

def generate_special_drops_screenshot(output_path):
    """
    1080x2424 screenshot showcasing Monochrome theme with Red Virus drop,
    Gold Neo drop, and Hidden 'MATRIX' message with a clean minimal clock.
    """
    w, h = 1080, 2424
    img = Image.new("RGBA", (w, h), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)

    font_size = 46
    density_step = 54
    font = get_font(font_size)
    cols = math.ceil(w / density_step)

    rng = random.Random(42)

    # Columns configuration
    virus_col = cols // 2 - 1      # Center-left: Red Virus drop
    matrix_col = 3                # Left: 'MATRIX' hidden word drop
    neo_col = cols - 4            # Right: 'NEO' gold drop

    for col in range(cols):
        x = col * density_step + 4
        stream_len = rng.randint(18, 38)
        head_y = rng.uniform(200, h + 300)

        is_virus = (col == virus_col)
        is_neo = (col == neo_col)
        is_matrix = (col == matrix_col)

        # Word letters if special
        matrix_word = "MATRIX"
        neo_word = "NEO"

        for step in range(stream_len):
            y = head_y - (step * font_size * 1.08)
            if y < -font_size or y > h + font_size:
                continue

            if is_matrix and step < len(matrix_word):
                ch = matrix_word[step]
            elif is_neo and step < len(neo_word):
                ch = neo_word[step]
            else:
                ch = get_random_matrix_char(rng)

            alpha = max(0.08, 1.0 - (step / stream_len))

            if is_virus:
                # Radiant Crimson Virus Drop
                if step == 0:
                    color = (255, 238, 240, 255) # Head core
                elif step < 4:
                    color = (255, 51, 68, int(255 * alpha))
                else:
                    color = (220, 30, 48, int(230 * alpha))
            elif is_neo:
                # Brilliant Gold Neo Drop
                if step == 0:
                    color = (255, 255, 240, 255)
                elif step < 4:
                    color = (255, 215, 0, int(255 * alpha))
                else:
                    color = (220, 180, 0, int(220 * alpha))
            elif is_matrix:
                # Sharp white with slight cyan aura for hidden message
                if step < len(matrix_word):
                    color = (255, 255, 255, 255)
                else:
                    color = (200, 200, 200, int(200 * alpha))
            else:
                # Monochrome / Silver background rain
                if step == 0 and rng.random() < 0.65:
                    color = (255, 255, 255, 255) # Brilliant head
                else:
                    intensity = int(220 * alpha)
                    color = (intensity, intensity, intensity, 255)

            draw.text((x, y), ch, font=font, fill=color)

    # CRT scanlines
    for y in range(0, h, 6):
        draw.line([(0, y), (w, y)], fill=(0, 0, 0, 40), width=2)

    # Sleek Lockscreen Cyber Clock Overlay
    clock_box = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    cdraw = ImageDraw.Draw(clock_box)

    clock_font = get_font(130, tech=True)
    sub_font = get_font(28, tech=True)
    time_str = "13:37:00"
    date_str = "WAKE UP, NEO • BATTERY 100%"

    bbox = cdraw.textbbox((0, 0), time_str, font=clock_font)
    tw = bbox[2] - bbox[0]
    tx = (w - tw) // 2
    ty = 1080

    # Glow for clock
    cdraw.text((tx, ty), time_str, font=clock_font, fill=(255, 255, 255, 120))
    clock_blurred = clock_box.filter(ImageFilter.GaussianBlur(10))
    img.alpha_composite(clock_blurred)

    # Crisp text
    draw.text((tx, ty), time_str, font=clock_font, fill=(255, 255, 255, 255))
    sbbox = draw.textbbox((0, 0), date_str, font=sub_font)
    stw = sbbox[2] - sbbox[0]
    draw.text(((w - stw) // 2, ty + 150), date_str, font=sub_font, fill=(200, 200, 200, 240))

    # Top Status Bar simulation
    sb_font = get_font(26, tech=True)
    draw.text((60, 48), "13:37", font=sb_font, fill=(255, 255, 255, 220))
    draw.text((w - 180, 48), "LTE  100%", font=sb_font, fill=(255, 255, 255, 220))

    # Bottom gesture bar
    draw.line([(w // 2 - 100, h - 36), (w // 2 + 100, h - 36)], fill=(255, 255, 255, 180), width=6)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"✔ Generated: {output_path}")

def generate_palettes_showcase(output_path):
    """
    1080x2424 screenshot showcasing the 5 alternate cyberpunk palettes:
    Cyan, Crimson, Amber, Violet, Monochrome.
    """
    w, h = 1080, 2424
    img = Image.new("RGBA", (w, h), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)

    palettes = [
        {"name": "NEON CYAN", "color": (0, 229, 255), "head": (238, 255, 255)},
        {"name": "CRIMSON VIRUS", "color": (255, 51, 68), "head": (255, 238, 240)},
        {"name": "AMBER GOLD", "color": (255, 183, 0), "head": (255, 251, 234)},
        {"name": "CYBER VIOLET", "color": (213, 0, 249), "head": (250, 230, 255)},
        {"name": "MONOCHROME", "color": (224, 224, 224), "head": (255, 255, 255)},
    ]

    col_width = w / len(palettes)
    font_size = 38
    density_step = 42
    font = get_font(font_size)

    rng = random.Random(77)

    for idx, p in enumerate(palettes):
        x_start = idx * col_width
        x_end = (idx + 1) * col_width
        cols_in_band = math.ceil(col_width / density_step)

        for c in range(cols_in_band):
            x = x_start + c * density_step + 6
            if x >= x_end - 6:
                continue

            stream_len = rng.randint(18, 36)
            head_y = rng.uniform(150, h + 200)

            for step in range(stream_len):
                y = head_y - (step * font_size * 1.08)
                if y < -font_size or y > h + font_size:
                    continue

                ch = get_random_matrix_char(rng)
                alpha = max(0.08, 1.0 - (step / stream_len))

                if step == 0:
                    fill = (p["head"][0], p["head"][1], p["head"][2], 255)
                else:
                    fill = (int(p["color"][0] * alpha), int(p["color"][1] * alpha), int(p["color"][2] * alpha), 255)

                draw.text((x, y), ch, font=font, fill=fill)

        # Subtle vertical separator line between bands
        if idx > 0:
            draw.line([(x_start, 0), (x_start, h)], fill=(30, 30, 30, 160), width=2)

    # Scanlines
    for y in range(0, h, 6):
        draw.line([(0, y), (w, y)], fill=(0, 0, 0, 40), width=2)

    # Top Header Banner
    banner_margin = 48
    draw.rounded_rectangle([banner_margin, 120, w - banner_margin, 310], radius=24, fill=(10, 12, 14, 230), outline=(0, 229, 255, 180), width=2)
    b_font = get_font(42, tech=True)
    sub_font = get_font(24, tech=True)
    draw.text((banner_margin + 36, 155), "6 CYBERPUNK COLOR THEMES", font=b_font, fill=(255, 255, 255, 255))
    draw.text((banner_margin + 36, 220), "Cyan • Crimson • Amber • Violet • Monochrome • Classic", font=sub_font, fill=(0, 229, 255, 240))
    draw.text((banner_margin + 36, 260), "Pure #000000 OLED Background on every palette", font=get_font(20, tech=True), fill=(180, 190, 200, 200))

    # Bottom labels for each band
    lbl_font = get_font(18, tech=True)
    for idx, p in enumerate(palettes):
        bx = idx * col_width + 10
        by = h - 180
        draw.rounded_rectangle([bx, by, bx + col_width - 20, by + 46], radius=10, fill=(12, 15, 18, 220), outline=(p["color"][0], p["color"][1], p["color"][2], 180), width=1)
        # Center text
        bbox = draw.textbbox((0, 0), p["name"], font=lbl_font)
        tw = bbox[2] - bbox[0]
        tx = bx + (col_width - 20 - tw) // 2
        draw.text((tx, by + 12), p["name"], font=lbl_font, fill=(p["color"][0], p["color"][1], p["color"][2], 255))

    # Top Status Bar
    sb_font = get_font(26, tech=True)
    draw.text((60, 48), "13:37", font=sb_font, fill=(255, 255, 255, 220))
    draw.text((w - 180, 48), "5G  100%", font=sb_font, fill=(255, 255, 255, 220))

    # Bottom gesture bar
    draw.line([(w // 2 - 100, h - 36), (w // 2 + 100, h - 36)], fill=(255, 255, 255, 180), width=6)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"✔ Generated: {output_path}")

if __name__ == "__main__":
    generate_palettes_showcase("fastlane/metadata/android/en-US/images/phoneScreenshots/4_cyberpunk_palettes.png")
    generate_special_drops_screenshot("fastlane/metadata/android/en-US/images/phoneScreenshots/5_special_drops.png")
