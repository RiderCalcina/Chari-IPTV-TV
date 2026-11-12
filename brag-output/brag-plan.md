# Brag Plan: Chari-IPTV

## What is this app?
Una aplicación nativa para Android TV que descarga, valida y reproduce canales IPTV de forma totalmente autónoma (sin servidor).

## The angle
El usuario de Smart TV está acostumbrado a que las listas IPTV fallen, se congelen o muestren canales muertos. Chari-IPTV resuelve esto validando los canales en segundo plano y cargándolos al instante, ofreciendo una experiencia nativa superior sin tiempos de espera. El ángulo es "Premium, rápida y sin pantallas negras".

## Hook (first 2-3 seconds)
La aplicación abre directamente en la grilla oscura elegante (`#0F172A`). El foco (D-Pad) salta rápidamente entre 3 canales. El borde brillante destaca de inmediato que es una experiencia 100% nativa de TV, no un port de celular. 
Texto enorme y centrado: "Live channels that actually load."

## Key moments (the middle)
- **Navegación veloz:** El foco se mueve por las tarjetas (`focusedScale = 1.15f`) a gran velocidad.
- **Transición sin interrupciones:** Un clic en la tarjeta y se expande inmediatamente a video en pantalla completa (ExoPlayer) sin pantallas de carga infinitas.
- **Acción oculta (background):** Un pequeño "Toast" nativo confirmando la validación en segundo plano (simulado o recreado).

## Outro / punchline
Volvemos de pantalla completa a la grilla con un fundido rápido.
Texto final: "Cero segundos de espera." seguido del logo de Chari-IPTV.

## User flow worth showing
El flujo principal es la esencia del uso de la TV:
Grilla (Leanback TVLazyVerticalGrid) → Navegación con control (D-Pad) → Clic (OK) → Reproducción a pantalla completa (ExoPlayer).

## Tone
- Preset: cinematic
- Creative direction: Lanzamiento premium de app para Smart TV
- Interpretation: Planos amplios de la interfaz, movimientos ágiles y decisivos simulando clics reales de un control remoto, poco texto pero con gran impacto visual.

## Format: landscape — 1920x1080
## Duration: 16 seconds

## Visual identity (from the project)
- Background: #0F172A
- Accent: #3B82F6 (Azul brillante para selecciones) / #60A5FA (Foco)
- Text: #FFFFFF / #F1F5F9
- Display font: Roboto
- Body font: Roboto
- Strongest visual element: El grid de canales de 4 columnas con el efecto hover (escala 1.15f y borde blanco grueso).

## Share copy (draft)
Chari-IPTV: Live channels for Android TV that actually load. Native, instant, and completely backend-free. 📺

## Audio direction
- Role: cinematic support
- Music: cinematic bed (ej. un track de percusión sutil y cuerdas graves)
- Music treatment: Empieza fuerte con un golpe (drop), se mantiene rítmico acompañando los clics del D-Pad, y tiene un pequeño "swell" (crecimiento) cuando entra a pantalla completa. Termina con un corte limpio.
- Music cue guidance: 
  - 1 strongCue timestamp en el clic final (aprox. 10s) que lanza el video a pantalla completa.
  - Beat-grid windows para los saltos de foco en la grilla.
- Audio-reactive treatment: subtle; los bordes de la tarjeta enfocada pueden latir levísimamente con los graves de la pista.
- SFX posture: moderada, pero vital. Los "clicks" del D-Pad deben sentirse mecánicos y precisos. Un sonido profundo ("woosh") al entrar a pantalla completa.
- Audio-coupled moments: Simulación de navegación (D-Pad clicks) sincronizados con golpes percusivos.
- Restraint rule: Nada de efectos de sonido agudos molestos de "arcade". Sonidos corporativos o de hardware físico.

## Storyboard

### Scene 1 — The Hook — 4s
La grilla oscura de 4 columnas con tarjetas de canales.
El foco aparece en la primera tarjeta, salta a la segunda y luego a la tercera en rápida sucesión.
El texto "Live channels..." aparece firme en el centro.
Sequential/interaction: yes — se simulan 3 movimientos del D-Pad hacia la derecha.
Audio intent: Golpes mecánicos potentes con cada movimiento del foco.
Audio-coupled idea: D-Pad clicks.
Music: cinematic
Transition mood: clean → Scene 2

### Scene 2 — The Promise — 5s
El foco baja rápidamente a la segunda fila de canales (D-Pad Down).
El texto cambia a: "...that actually load."
Sequential/interaction: yes — D-Pad down.
Audio intent: Tensión acumulada.
Audio-coupled idea: D-Pad click seguido de un "thud" al leer el texto.
Transition mood: dramatic (zoom rápido) → Scene 3

### Scene 3 — Fullscreen Action — 4s
Se simula el clic "OK" sobre la tarjeta enfocada.
La tarjeta hace un rápido efecto ripple e inmediatamente hace zoom ocupando toda la pantalla y mostrando un video en reproducción.
Texto corto abajo: "Instant playback. Zero backend."
Sequential/interaction: yes — Clic "OK" en el control.
Audio intent: Explosivo, amplio, liberador.
Audio-coupled idea: "Swoosh" / "Whoosh" profundo al expandirse la tarjeta, seguido de audio ambiente simulando el video.
Transition mood: hard cut → Scene 4

### Scene 4 — Punchline — 3s
La pantalla se oscurece casi al instante.
Texto final al centro: "Cero segundos de espera."
Aparece el logo de Chari-IPTV (TV.png).
Sequential/interaction: none
Audio intent: Golpe final decisivo y eco.
Audio-coupled idea: "Boom" final sutil para el logo.
Music: cinematic
Transition mood: none

**Music mood for this video:** cinematic
**Audio summary:** Base tensa y grave, pautada por el sonido preciso de un control de TV, culminando en un golpe orquestal al entrar a pantalla completa.
