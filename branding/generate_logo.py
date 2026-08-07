#!/usr/bin/env python3
"""Generate the XGram brand preview and Android legacy launcher icons.

Requires Pillow. Adaptive icon layers are maintained as Android vector drawables in
app/src/main/res/drawable.
"""

from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
BRANDING_DIR = ROOT / "branding"
MASTER_SIZE = 1024
DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def interpolate(start, end, amount):
    return tuple(round(a + (b - a) * amount) for a, b in zip(start, end))


def polygon_gradient(size, points, start, end):
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).polygon(points, fill=255)

    gradient = Image.new("RGBA", (size, size))
    gradient_draw = ImageDraw.Draw(gradient)
    for y in range(size):
        color = interpolate(start, end, y / max(size - 1, 1))
        gradient_draw.line((0, y, size, y), fill=(*color, 255))
    gradient.putalpha(mask)
    return gradient


def create_logo(size=MASTER_SIZE, round_icon=False):
    scale = size / MASTER_SIZE

    shape_mask = Image.new("L", (size, size), 0)
    mask_draw = ImageDraw.Draw(shape_mask)
    if round_icon:
        mask_draw.ellipse((0, 0, size - 1, size - 1), fill=255)
    else:
        mask_draw.rounded_rectangle(
            (0, 0, size - 1, size - 1), radius=round(224 * scale), fill=255
        )

    background = Image.new("RGBA", (size, size))
    background_draw = ImageDraw.Draw(background)
    for y in range(size):
        color = interpolate((5, 15, 30), (10, 37, 65), y / max(size - 1, 1))
        background_draw.line((0, y, size, y), fill=(*color, 255))

    glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    glow_draw.ellipse(
        (220 * scale, 40 * scale, 880 * scale, 700 * scale),
        fill=(34, 211, 238, 55),
    )
    glow = glow.filter(ImageFilter.GaussianBlur(round(130 * scale)))
    background = Image.alpha_composite(background, glow)

    depth = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    depth_draw = ImageDraw.Draw(depth)
    depth_draw.polygon(
        [
            (0, 690 * scale),
            (760 * scale, 282 * scale),
            (size, 430 * scale),
            (size, size),
            (0, size),
        ],
        fill=(7, 27, 58, 225),
    )
    depth_draw.polygon(
        [
            (0, 900 * scale),
            (790 * scale, 475 * scale),
            (size, 635 * scale),
            (size, size),
            (0, size),
        ],
        fill=(17, 52, 88, 235),
    )
    background = Image.alpha_composite(background, depth)

    emblem = Image.new("RGBA", (size, size), (0, 0, 0, 0))

    ascending = [
        (244 * scale, 778 * scale),
        (650 * scale, 244 * scale),
        (808 * scale, 244 * scale),
        (402 * scale, 778 * scale),
    ]
    descending = [
        (250 * scale, 244 * scale),
        (408 * scale, 244 * scale),
        (802 * scale, 778 * scale),
        (644 * scale, 778 * scale),
    ]

    shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    offset = 36 * scale
    shadow_draw.polygon(
        [(x, y + offset) for x, y in ascending], fill=(0, 5, 17, 180)
    )
    shadow_draw.polygon(
        [(x, y + offset) for x, y in descending], fill=(0, 5, 17, 180)
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(round(20 * scale)))
    emblem = Image.alpha_composite(emblem, shadow)

    emblem = Image.alpha_composite(
        emblem,
        polygon_gradient(size, ascending, (16, 183, 216), (37, 211, 238)),
    )
    emblem = Image.alpha_composite(
        emblem,
        polygon_gradient(size, descending, (103, 232, 249), (34, 198, 224)),
    )

    plane_points = [
        (448 * scale, 500 * scale),
        (784 * scale, 348 * scale),
        (656 * scale, 696 * scale),
        (574 * scale, 598 * scale),
        (502 * scale, 656 * scale),
        (536 * scale, 558 * scale),
    ]
    plane_shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    plane_shadow_draw = ImageDraw.Draw(plane_shadow)
    plane_shadow_draw.polygon(
        [(x, y + 16 * scale) for x, y in plane_points], fill=(0, 10, 24, 115)
    )
    plane_shadow = plane_shadow.filter(ImageFilter.GaussianBlur(round(12 * scale)))
    emblem = Image.alpha_composite(emblem, plane_shadow)

    emblem_draw = ImageDraw.Draw(emblem)
    emblem_draw.polygon(plane_points, fill=(255, 255, 255, 255))
    emblem_draw.polygon(
        [
            (536 * scale, 558 * scale),
            (784 * scale, 348 * scale),
            (574 * scale, 598 * scale),
        ],
        fill=(218, 249, 255, 255),
    )

    logo = Image.alpha_composite(background, emblem)
    logo.putalpha(ImageChops.multiply(logo.getchannel("A"), shape_mask))
    return logo


def main():
    BRANDING_DIR.mkdir(parents=True, exist_ok=True)
    master = create_logo()
    master.save(BRANDING_DIR / "xgram-logo.png", optimize=True)

    for density, target_size in DENSITIES.items():
        output_dir = ROOT / "app" / "src" / "main" / "res" / f"mipmap-{density}"
        output_dir.mkdir(parents=True, exist_ok=True)
        create_logo(target_size).save(output_dir / "ic_launcher.png", optimize=True)
        create_logo(target_size, round_icon=True).save(
            output_dir / "ic_launcher_round.png", optimize=True
        )

    print("Generated XGram logo and Android launcher icons.")


if __name__ == "__main__":
    main()
