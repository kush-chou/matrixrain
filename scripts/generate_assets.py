#!/usr/bin/env python3
"""
Matrix Rain Asset Generator
Generates high-resolution Android launcher mipmaps, Fastlane store graphics,
feature graphics, and OpenGraph web assets.
"""

import math
import os
import random
from PIL import Image, ImageDraw, ImageFont, ImageFilter

# Deterministic seed for reproducible asset generation
random.seed(42)

FONT_MONO = "/System/Library/Fonts/Menlo.ttc"
GLYPHS = "ｦｱｳｴｵｶｷｹｺｻｼｽｾｿﾀﾂﾃﾅﾆﾇﾈﾊﾋﾎﾏﾐﾑﾒﾓﾔﾕﾗﾘﾜ9876543210:•<>[]*#$"

def get_font(size):
    try:
        return ImageFont.truetype(FONT_MONO, size)
    except Exception:
        return ImageFont.load_default()

def draw_matrix_rain_background(
    draw, width, height, 
    primary_color=(0, 255, 102), 
    lead_color=(255, 255, 255), 
    font_size=20, 
    density_step=22,
    lead_chance=0.7
):
    font = get_font(font_size)
    columns = math.ceil(width / density_step)

    for col in range(columns):
        x = col * density_step
        col_speed = random.uniform(0.6, 1.4)
        stream_len = random.randint(10, 30)
        head_y = random.uniform(-100, height + 200)

        for step in range(stream_len):
            y = head_y - (step * font_size * 1.1)
            if y < -font_size or y > height + font_size:
                continue

            char = random.choice(GLYPHS)
            alpha = max(0.05, 1.0 - (step / stream_len))

            if step == 0 and random.random() < lead_chance:
                color = lead_color
            else:
                # Gradient fade down the trail
                r = int(primary_color[0] * alpha)
                g = int(primary_color[1] * alpha)
                b = int(primary_color[2] * alpha)
                color = (r, g, b)

            draw.text((x, y), char, font=font, fill=color)

def generate_play_store_icon(output_path):
    """512x512 High-Res App Icon for Fastlane and App Stores."""
    size = (512, 512)
    img = Image.new("RGBA", size, (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)

    # Matrix code cascade background
    draw_matrix_rain_background(draw, 512, 512, font_size=24, density_step=26)

    # Subtle CRT scanlines
    for y in range(0, 512, 4):
        draw.line([(0, y), (512, y)], fill=(0, 0, 0, 60), width=1)

    # Central glowing matrix emblem / glyph frame
    center_glow = Image.new("RGBA", size, (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(center_glow)
    
    # Outer cyber circle / squircle
    margin = 56
    glow_draw.rounded_rectangle(
        [margin, margin, 512 - margin, 512 - margin],
        radius=96,
        outline=(0, 255, 102, 220),
        width=4
    )
    # Inner accent corners
    glow_draw.line([(margin + 20, margin + 20), (margin + 60, margin + 20)], fill=(255, 255, 255, 240), width=3)
    glow_draw.line([(margin + 20, margin + 20), (margin + 20, margin + 60)], fill=(255, 255, 255, 240), width=3)
    
    glow_draw.line([(512 - margin - 20, 512 - margin - 20), (512 - margin - 60, 512 - margin - 20)], fill=(255, 255, 255, 240), width=3)
    glow_draw.line([(512 - margin - 20, 512 - margin - 20), (512 - margin - 20, 512 - margin - 60)], fill=(255, 255, 255, 240), width=3)

    # Core Iconic Character in Center: '雨' / 'ネ' or Neo terminal prompt
    hero_font = get_font(180)
    hero_char = "雨"
    bbox = glow_draw.textbbox((0, 0), hero_char, font=hero_font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    tx = (512 - tw) // 2 - bbox[0]
    ty = (512 - th) // 2 - bbox[1]

    # Glow layer
    glow_draw.text((tx, ty), hero_char, font=hero_font, fill=(0, 255, 102, 140))
    glow_filtered = center_glow.filter(ImageFilter.GaussianBlur(12))
    img.alpha_composite(glow_filtered)

    # Crisp hero text on top
    draw.text((tx, ty), hero_char, font=hero_font, fill=(230, 255, 240, 255))
    draw.text((tx, ty - 2), hero_char, font=hero_font, fill=(255, 255, 255, 200))

    # Small tech badge
    badge_font = get_font(20)
    draw.text((256 - 44, 400), "MATRIX", font=badge_font, fill=(0, 255, 102, 220))

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"✔ Generated Play Store Icon: {output_path} (512x512)")

def generate_feature_graphic(output_path):
    """1024x500 Feature Graphic."""
    size = (1024, 500)
    img = Image.new("RGBA", size, (2, 5, 4, 255))
    draw = ImageDraw.Draw(img)

    # Full cyber matrix rain field
    draw_matrix_rain_background(draw, 1024, 500, font_size=20, density_step=24)

    # Gradient vignette darken sides for text contrast
    vignette = Image.new("RGBA", size, (0, 0, 0, 0))
    vdraw = ImageDraw.Draw(vignette)
    for x in range(1024):
        # Darken center-left slightly for logo readability
        dist_from_left = abs(x - 380) / 400.0
        alpha = int(max(0, 140 - (dist_from_left * 80)))
        vdraw.line([(x, 0), (x, 500)], fill=(0, 0, 0, min(180, alpha + 60)))
    img.alpha_composite(vignette)

    # Top terminal badge
    tag_font = get_font(18)
    draw.rounded_rectangle([70, 70, 310, 104], radius=16, fill=(0, 25, 10, 220), outline=(0, 255, 102, 180), width=1)
    draw.text((90, 78), "⚡ SYSTEM READY • v2.0", font=tag_font, fill=(0, 255, 102, 255))

    # Main Title: MATRIX RAIN
    title_font = get_font(68)
    draw.text((70, 125), "MATRIX RAIN", font=title_font, fill=(0, 255, 102, 255))
    draw.text((70, 123), "MATRIX RAIN", font=title_font, fill=(240, 255, 245, 230))

    # Subtitle
    sub_font = get_font(24)
    draw.text((70, 215), "Digital Rain Live Wallpaper for Android", font=sub_font, fill=(220, 240, 230, 255))

    # Feature Pills
    pills = ["0% Standby Drain", "True OLED Black", "120Hz ProMotion", "100% FOSS"]
    pill_font = get_font(18)
    px = 70
    py = 280
    for pill in pills:
        bbox = draw.textbbox((0, 0), pill, font=pill_font)
        pw = bbox[2] - bbox[0] + 28
        draw.rounded_rectangle([px, py, px + pw, py + 38], radius=8, fill=(0, 30, 15, 200), outline=(0, 255, 102, 120), width=1)
        draw.text((px + 14, py + 8), pill, font=pill_font, fill=(200, 255, 220, 240))
        px += pw + 14

    # Extra highlight pill
    extra_pill = "⚡ 100% Offline • Touch Interactive Shockwave"
    ebbox = draw.textbbox((0, 0), extra_pill, font=pill_font)
    epw = ebbox[2] - ebbox[0] + 28
    draw.rounded_rectangle([70, 335, 70 + epw, 335 + 38], radius=8, fill=(0, 40, 30, 200), outline=(0, 229, 255, 180), width=1)
    draw.text((84, 343), extra_pill, font=pill_font, fill=(0, 229, 255, 255))

    # Right side: Phone Mockup Visual
    phone_w, phone_h = 190, 380
    phone_x = 735
    phone_y = 60
    # Outer phone body
    draw.rounded_rectangle([phone_x, phone_y, phone_x + phone_w, phone_y + phone_h], radius=28, fill=(15, 18, 20, 255), outline=(0, 255, 102, 160), width=3)
    # Inner screen
    screen_pad = 8
    screen_w = phone_w - 2 * screen_pad
    screen_h = phone_h - 2 * screen_pad
    phone_screen = Image.new("RGBA", (screen_w, screen_h), (0, 0, 0, 255))
    sdraw = ImageDraw.Draw(phone_screen)
    draw_matrix_rain_background(sdraw, screen_w, screen_h, font_size=12, density_step=14)
    # Clock on screen
    w_font = get_font(28)
    sdraw.text((screen_w // 2 - 36, 60), "13:37", font=w_font, fill=(255, 255, 255, 240))
    # Paste screen
    screen_mask = Image.new("L", (screen_w, screen_h), 0)
    smask_draw = ImageDraw.Draw(screen_mask)
    smask_draw.rounded_rectangle([0, 0, screen_w, screen_h], radius=20, fill=255)
    img.paste(phone_screen, (phone_x + screen_pad, phone_y + screen_pad), screen_mask)

    # Punch hole camera
    draw.ellipse([phone_x + phone_w // 2 - 4, phone_y + 14, phone_x + phone_w // 2 + 4, phone_y + 22], fill=(0, 0, 0, 255), outline=(40, 40, 40, 255), width=1)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"✔ Generated Feature Graphic: {output_path} (1024x500)")

def generate_phone_screenshot(output_path, title, subtitle, theme="classic"):
    """1080x1920 High-Res Phone Screenshot."""
    size = (1080, 1920)
    img = Image.new("RGBA", size, (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)

    palette = {
        "classic": (0, 255, 102),
        "cyan": (0, 229, 255),
        "crimson": (255, 51, 68),
        "gold": (255, 190, 0)
    }.get(theme, (0, 255, 102))

    # Full wallpaper digital code rain
    draw_matrix_rain_background(draw, 1080, 1920, primary_color=palette, font_size=28, density_step=32)

    # CRT scanlines
    for y in range(0, 1920, 6):
        draw.line([(0, y), (1080, y)], fill=(0, 0, 0, 45), width=2)

    # Android Status bar simulation at top
    sb_font = get_font(32)
    draw.text((60, 40), "13:37", font=sb_font, fill=(255, 255, 255, 220))
    draw.text((920, 40), "5G 100%", font=sb_font, fill=(255, 255, 255, 220))

    # Top Marketing Banner / Headline Card
    card_margin = 60
    draw.rounded_rectangle(
        [card_margin, 120, 1080 - card_margin, 380],
        radius=28,
        fill=(5, 15, 10, 230),
        outline=(palette[0], palette[1], palette[2], 200),
        width=3
    )
    t_font = get_font(52)
    sub_font = get_font(30)
    draw.text((card_margin + 40, 160), title, font=t_font, fill=(255, 255, 255, 255))
    draw.text((card_margin + 40, 250), subtitle, font=sub_font, fill=(palette[0], palette[1], palette[2], 230))
    draw.text((card_margin + 40, 305), "Deep OLED blacks • Zero standby battery drain", font=get_font(24), fill=(180, 200, 190, 200))

    # Android Clock Widget in center
    clock_font = get_font(140)
    date_font = get_font(36)
    draw.text((360, 750), "13:37", font=clock_font, fill=(255, 255, 255, 255))
    draw.text((380, 920), "Friday, October 9", font=date_font, fill=(palette[0], palette[1], palette[2], 240))

    # Touch ripple indicator simulation
    ripple_center = (540, 1300)
    for r in [60, 120, 180, 240]:
        alpha = int(180 * (1 - r / 300))
        draw.ellipse([ripple_center[0] - r, ripple_center[1] - r,
                      ripple_center[0] + r, ripple_center[1] + r],
                     outline=(palette[0], palette[1], palette[2], alpha), width=3)
    draw.text((430, 1280), "TOUCH SHOCKWAVE", font=get_font(26), fill=(255, 255, 255, 220))

    # Bottom Android Navigation bar
    draw.line([(440, 1880), (640, 1880)], fill=(255, 255, 255, 180), width=6)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"✔ Generated Phone Screenshot: {output_path} (1080x1920)")

def generate_opengraph_preview(output_path):
    """1200x630 OpenGraph Preview Card for Social Media."""
    size = (1200, 630)
    img = Image.new("RGBA", size, (2, 5, 3, 255))
    draw = ImageDraw.Draw(img)

    draw_matrix_rain_background(draw, 1200, 630, font_size=24, density_step=28)

    # Center card overlay
    c_box = [100, 100, 1100, 530]
    draw.rounded_rectangle(c_box, radius=32, fill=(0, 15, 8, 230), outline=(0, 255, 102, 180), width=3)

    # Terminal prompt header
    draw.rounded_rectangle([140, 140, 360, 175], radius=12, fill=(0, 35, 18, 240), outline=(0, 255, 102, 120), width=1)
    draw.text((156, 146), ">_ matrixrain.app", font=get_font(20), fill=(0, 255, 102, 255))

    title_font = get_font(64)
    draw.text((140, 210), "MATRIX RAIN", font=title_font, fill=(0, 255, 102, 255))
    draw.text((140, 208), "MATRIX RAIN", font=title_font, fill=(255, 255, 255, 220))

    sub_font = get_font(30)
    draw.text((140, 305), "The Ultimate Cyberpunk Live Wallpaper for Android", font=sub_font, fill=(220, 240, 230, 240))

    meta_font = get_font(22)
    draw.text((140, 380), "Android Phones • AMOLED Optimized • 100% Free on F-Droid", font=meta_font, fill=(0, 229, 255, 240))

    # CTA Pill
    draw.rounded_rectangle([140, 435, 430, 485], radius=24, fill=(0, 255, 102, 255))
    draw.text((165, 448), "EXPLORE THE CONSTRUCT", font=get_font(20), fill=(0, 0, 0, 255))

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    img.save(output_path, "PNG")
    print(f"✔ Generated OpenGraph Card: {output_path} (1200x630)")

def generate_mipmap_icons(res_dir):
    """Generates legacy Android mipmap density PNG icons."""
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    # Generate from a base 512x512 icon
    base = Image.new("RGBA", (512, 512), (0, 0, 0, 255))
    bdraw = ImageDraw.Draw(base)
    draw_matrix_rain_background(bdraw, 512, 512, font_size=32, density_step=36)
    hero_font = get_font(220)
    hero_char = "雨"
    bbox = bdraw.textbbox((0, 0), hero_char, font=hero_font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    tx = (512 - tw) // 2 - bbox[0]
    ty = (512 - th) // 2 - bbox[1]
    bdraw.text((tx, ty), hero_char, font=hero_font, fill=(0, 255, 102, 255))
    bdraw.text((tx, ty - 2), hero_char, font=hero_font, fill=(255, 255, 255, 220))

    for folder, dim in densities.items():
        out_dir = os.path.join(res_dir, folder)
        os.makedirs(out_dir, exist_ok=True)
        resized = base.resize((dim, dim), Image.Resampling.LANCZOS)
        
        # Square icon
        resized.save(os.path.join(out_dir, "ic_launcher.png"), "PNG")
        
        # Round icon with circular mask
        round_img = Image.new("RGBA", (dim, dim), (0, 0, 0, 0))
        rmask = Image.new("L", (dim, dim), 0)
        rmdraw = ImageDraw.Draw(rmask)
        rmdraw.ellipse([0, 0, dim, dim], fill=255)
        round_img.paste(resized, (0, 0), rmask)
        round_img.save(os.path.join(out_dir, "ic_launcher_round.png"), "PNG")

        print(f"✔ Generated Mipmaps for {folder} ({dim}x{dim})")

def main():
    print("🎨 Generating Matrix Rain Visual Assets Suite...")

    # Fastlane Store Assets
    fastlane_base = "fastlane/metadata/android/en-US/images"
    generate_play_store_icon(f"{fastlane_base}/icon.png")
    generate_feature_graphic(f"{fastlane_base}/featureGraphic.png")

    generate_phone_screenshot(f"{fastlane_base}/phoneScreenshots/1_classic_matrix.png", "AUTHENTIC DIGITAL RAIN", "Engineered for 120Hz Displays", theme="classic")
    generate_phone_screenshot(f"{fastlane_base}/phoneScreenshots/2_touch_shockwave.png", "INTERACTIVE TOUCH RIPPLE", "Tap to Deflect Falling Glyphs", theme="cyan")
    generate_phone_screenshot(f"{fastlane_base}/phoneScreenshots/3_custom_palettes.png", "CRIMSON VIRUS & GOLD NEO", "Multi-Theme Cyberpunk Engine", theme="crimson")

    # Web Assets
    web_assets = "web/assets"
    os.makedirs(web_assets, exist_ok=True)
    generate_opengraph_preview(f"{web_assets}/og-preview.png")
    
    # Favicons & Touch Icons
    fav = Image.open(f"{fastlane_base}/icon.png")
    fav.resize((180, 180), Image.Resampling.LANCZOS).save(f"{web_assets}/apple-touch-icon.png", "PNG")
    fav.resize((32, 32), Image.Resampling.LANCZOS).save(f"{web_assets}/favicon.png", "PNG")
    fav.resize((32, 32), Image.Resampling.LANCZOS).save("web/favicon.ico", "ICO")
    print("✔ Generated Web Favicons & Apple Touch Icons in web/assets")

    # Android app mipmap densities
    generate_mipmap_icons("app/src/main/res")

    print("\n🎉 All Matrix Rain graphical assets successfully generated!")

if __name__ == "__main__":
    main()
