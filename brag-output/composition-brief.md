# Hyperframes Composition Brief: Chari-IPTV

## Objective
Create a short launch-style brag video for Chari-IPTV, a native Android TV app.

## Output
- Composition directory: `brag-output/composition/`
- Rendered video: `brag-output/brag.mp4`
- Format: landscape — 1920x1080
- Duration: 16 seconds

## Source Material
- Project root: `C:\xampp\htdocs\Chari-IPTV_TV`
- Primary files read: `README.md`, `INTERFAZ_USUARIO.md`
- Product name: Chari-IPTV
- Tagline / strongest claim: Live channels that actually load.
- Key UI or visual moment to recreate: The TV grid (LazyVerticalGrid) with 4 columns, hovering over a channel with a D-Pad (scaling up `focusedScale = 1.15f` and white border), then expanding to full-screen ExoPlayer video.
- Copy that must appear verbatim:
  - Live channels that actually load.
  - Instant playback. Zero backend.
  - Cero segundos de espera.

## Creative Direction
- Tone preset: cinematic
- Creative direction: Lanzamiento premium de app para Smart TV
- Interpretation: Wide UI shots, fast and decisive D-Pad navigation movements.
- Angle: Premium, fast, and no black screens. Showcasing the native performance and background validation.
- Hook: The app opens directly in the dark grid. The focus jumps rapidly between 3 channels.
- Outro / punchline: "Cero segundos de espera." followed by the logo.
- Avoid:
  - Generic SaaS language
  - Abstract filler visuals
  - Unrelated visual redesign

## Visual Identity
- Background: #0F172A
- Text: #FFFFFF
- Accent: #3B82F6
- Display font: Roboto
- Body font: Roboto
- Visual references from the project: The dark leanback UI, white highlight borders, D-pad navigation.

## Storyboard
Use the storyboard in `brag-output/brag-plan.md` as the creative contract.

Scene summary:
1. The Hook — 4s — 4-column grid, focus jumps 3 times right. "Live channels..."
2. The Promise — 5s — Focus drops down. "...that actually load."
3. Fullscreen Action — 4s — "OK" click simulated. Card expands to fullscreen video. "Instant playback. Zero backend."
4. Punchline — 3s — Dark screen. "Cero segundos de espera." and Logo.

## Audio
- Audio role: cinematic support
- Audio arc: Starts punchy, builds tension with D-pad clicks, releases on full-screen entry, finishes with a clean echo.
- Music: (None provided by default, Hyperframes should pick a cinematic bed if available, or stay silent if not)
- Music treatment: Swell into full-screen. Fade out under final logo.
- Music cue guidance: unavailable (detect at composition via `hyperframes beats` if available).
- Audio-reactive treatment: subtle; card focus borders can pulse slightly with bass.
- Audio-coupled moments:
  - Scene 1 & 2 — simulated interaction (D-Pad clicks)
  - Scene 3 — boom / whoosh (full screen expansion)
- SFX selection guidance: mechanical and precise for D-Pad, deep whoosh for full-screen.
- SFX analysis guidance: use lower high-frequency-risk sounds for repeated moments.
- Exact SFX choice: Hyperframes should choose filenames, timestamps, density, and volume based on the implemented animation.

## Hyperframes Instructions
Load the composition-building Hyperframes domain skills. /brag is its own workflow.

Requirements:
- Show the 4-column UI and D-Pad interaction.
- Keep all text readable.
- Keep the video within 15-25 seconds.
- Treat `/brag` audio notes as guidance. Choose SFX after the visual animation exists.
- Run `hyperframes check` before render.
