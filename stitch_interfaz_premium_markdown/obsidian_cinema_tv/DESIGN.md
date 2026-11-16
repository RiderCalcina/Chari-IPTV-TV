---
name: Obsidian Cinema TV
colors:
  surface: '#0f131c'
  surface-dim: '#0f131c'
  surface-bright: '#353943'
  surface-container-lowest: '#0a0e17'
  surface-container-low: '#181b25'
  surface-container: '#1c1f29'
  surface-container-high: '#262a34'
  surface-container-highest: '#31353f'
  on-surface: '#dfe2ef'
  on-surface-variant: '#c3c6d7'
  inverse-surface: '#dfe2ef'
  inverse-on-surface: '#2c303a'
  outline: '#8d90a0'
  outline-variant: '#434655'
  surface-tint: '#b4c5ff'
  primary: '#b4c5ff'
  on-primary: '#002a78'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#0053db'
  secondary: '#ffb95f'
  on-secondary: '#472a00'
  secondary-container: '#ee9800'
  on-secondary-container: '#5b3800'
  tertiary: '#ffb596'
  on-tertiary: '#581e00'
  tertiary-container: '#bc4800'
  on-tertiary-container: '#ffede6'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#ffddb8'
  secondary-fixed-dim: '#ffb95f'
  on-secondary-fixed: '#2a1700'
  on-secondary-fixed-variant: '#653e00'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb596'
  on-tertiary-fixed: '#360f00'
  on-tertiary-fixed-variant: '#7d2d00'
  background: '#0f131c'
  on-background: '#dfe2ef'
  surface-variant: '#31353f'
typography:
  display-tv:
    fontFamily: Plus Jakarta Sans
    fontSize: 56px
    fontWeight: '800'
    lineHeight: 64px
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 26px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 20px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
  ticker-text:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '500'
    lineHeight: 20px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-tv: 2rem
  margin: 3.5rem
  margin-tv-safe: 4rem
  space-xs: 0.375rem
  space-sm: 0.75rem
  space-md: 1.25rem
  space-lg: 2rem
  space-xl: 3rem
---

## Brand & Style
The design system establishes a premium, theatrical 10-foot experience crafted specifically for lean-back viewing on ultra-high-definition television displays. Designed for Spanish-speaking live television and streaming audiences, the identity balances cinematic immersion with hyper-legible utility from an 8-to-12-foot viewing distance.

### Visual Aesthetic
The style fuses **Cinematic Minimalism** with **Luminescent Depth**:
- **Obsidian Immersion:** Deep midnight fields eliminate edge bleeding on OLED and LED panels, prioritizing the broadcast video feed.
- **Luminescent Focus Architecture:** Remote navigation relies on radiant electric blue and VIP amber halos rather than mere scale shifts, delivering immediate physical feedback to D-pad directional clicks.
- **Glassmorphism & Tactical Translucency:** Floating UI cards, live metadata overlays, and system dialogs leverage frosted, low-opacity slate backdrops with subtle chromatic rims to maintain spatial continuity over live video streams.

## Colors
The palette is calibrated for high-contrast viewing under dim living-room environments, avoiding pure white glare while adhering to strict accessibility contrast ratios.

- **Obsidian Dark (Canvas Neutral - `#090D16`):** The foundational void. Reduces panel power draw and eliminates viewer eye strain during prolonged evening viewing.
- **Slate Elevate (Card Surface - `#1E293B`):** Structural card containers and navigation trays, providing distinct surface separation from the background without harsh line divisions.
- **Electric Royal Blue (Primary Accent - `#2563EB`):** The operational focus state. Used for active D-pad selections, selected channel states, timeline scrub heads, and primary action affordances.
- **Radiant Gold / Amber (VIP Accent - `#F59E0B`):** Reserved for VIP tier privileges, donation drives, urgent announcements, and the "Importante" broadcast status indicator.
- **Signal Red (`#EF4444`):** Strictly reserved for the pulsating 'EN VIVO' (Live) broadcast indicators and critical stream errors.

## Typography
All typography rules are optimized for 1080p and 4K TV scaling at standard 10-foot distances. Minimum text size is pegged strictly at 12px for micro-labels to prevent illegibility across varying panel calibrations.

- **Plus Jakarta Sans** provides open counters and geometric clarity for bold Spanish headline accents, channel names, and navigation filters.
- **Inter** handles high-density body metadata, EPG schedules, channel overviews, and system settings, ensuring optical clarity at lower contrast ranges.
- Letter-spacing on uppercase status badges (`EN VIVO`, `24/7`, `IMPORTANTE`) is expanded to `+0.08em` for rapid visual parsing.

## Layout & Spacing
The layout adheres strictly to an overscan-safe 10-foot grid architecture:

- **TV Safe Margin (`margin-tv-safe` - 4rem / 64px):** Guarantees zero clipping of content across legacy displays, edge-lit panels, and projection units.
- **4-Column 16:11 Grid:** The primary content view adopts a 4-column arrangement on widescreen TVs, utilizing 16:11 aspect-ratio channel cards. This ratio optimizes station identity logos while leaving space for two lines of localized programmatic metadata beneath.
- **D-Pad Directional Flow:** Vertical scrolling operates in discrete row snaps, anchoring the active row at 35% from the screen top to maintain preview visibility of following content.

## Elevation & Depth
Depth signals focus priority in lean-back navigation without physical touch.

- **Non-Focused Plane (Level 0):** Background sits at `#090D16`. Cards rest on `#1E293B` at 70% opacity with a fine 1px border of `rgba(255, 255, 255, 0.08)`.
- **Focused Elevation (Level 1 - D-Pad Active):** The selected card scales smoothly by `1.04x`. The 1px perimeter border transitions to primary `#2563EB` backed by an outer soft-glow dispersion: `0 0 24px rgba(37, 99, 235, 0.45)`.
- **VIP Focused Elevation:** VIP/Donation cards in focus employ `#F59E0B` glow halos: `0 0 28px rgba(245, 158, 11, 0.50)`.
- **Modals & Overlays (Level 2):** QR code prompts and modal sheets float atop a `backdrop-filter: blur(24px)` scrim using `rgba(9, 13, 22, 0.85)` surface treatment.

## Shapes
A unified rounded structure balances contemporary TV aesthetic with sleek industrial discipline.

- **Channel Cards:** Standardized with `0.5rem` (8px) corners to provide clean grid alignment.
- **Filter Pills & Badges:** Use full capsule rounding (`9999px`) for micro-tags and segmented category headers (`Deportes`, `Noticias`, `Cine`).
- **Modal Containers:** Built on `rounded-lg` (1rem / 16px) to ground high-importance dialogs gracefully within the center of the display canvas.

## Components

### Channel Cards (16:11)
- **Dimensions & Ratio:** Strict 16:11 aspect ratio container.
- **Visual Anatomy:** Solid dark slate surface `#1E293B`, subtle channel watermark badge in the top-right, and live status pill pinned to the top-left.
- **Footer Section:** Darkened gradient scrim behind channel title (white, bold) and current broadcast name (slate 400).
- **Focus Behavior:** Scale `1.04x`, border highlight `#2563EB`, outer drop glow. Motion transitions must resolve within 150ms using `ease-out`.

### Badges ('EN VIVO' & '24/7')
- **'EN VIVO':** Translucent scarlet glass fill `rgba(239, 68, 68, 0.2)` with a 1px border of `#EF4444`. Includes a pulsating circular live dot.
- **'24/7' & 'FHD':** Translucent slate glass fill `rgba(255, 255, 255, 0.1)` with crisp white text.
- **'IMPORTANTE':** Radiant amber fill `rgba(245, 158, 11, 0.2)` with gold typography and `#F59E0B` stroke.

### D-Pad Category Pills
- Horizontal scroll row sitting above the channel matrix.
- Inactive: Borderless, slate-800 text with transparent fill.
- Focused: White text on `#2563EB` solid fill, accompanying a soft blue backlight glow.

### Symmetrical QR Donation & Pairing Modal
- Centered 540px symmetrical card over a blurred dark obsidian backdrop.
- High-contrast white QR container flanked by dual instructions in Spanish.
- Radiant gold button affordance below for dismissing or refreshing codes.

### Marquee News Ticker
- Docked permanently to the bottom edge inside safe margins.
- Dark semi-opaque slate backing with a vibrant left-anchored amber badge (`ALERTA` / `NOTICIAS`).
- Infinite smooth linear crawl for Spanish text updates, pausing automatically during user channel browsing.