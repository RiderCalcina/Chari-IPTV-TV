# 📺 Interfaz de Usuario: Chari-IPTV (v1.2)

La interfaz de usuario de **Chari-IPTV** está diseñada específicamente para Android TV y Google TV utilizando el moderno kit de herramientas **Jetpack Compose (androidx.tv.material3)** y las pautas de diseño para salas de estar **Obsidian Cinema TV (10-Foot UI)**. A continuación, se detalla cada aspecto visual, interactivo y de navegación de la aplicación.

---

## 1. Diseño General (Look & Feel: Obsidian Cinema TV)
- **Tema Oscuro Puro OLED (Obsidian Dark):** Fondo canvas ultraoscuro (`#090D16`), calibrado para evitar el sangrado de luz en paneles OLED y reducir la fatiga visual en entornos oscuros.
- **Superficie de Tarjetas Elevadas (Slate):** Los contenedores y bandejas de navegación descansan sobre tono pizarra (`#1E293B`) con bordes translúcidos sutiles de 1dp (`#33FFFFFF`), logrando una separación nítida sin líneas agresivas.
- **Navegación D-Pad Pura:** El 100% de los elementos interactivos responden al control remoto convencional (botones direccionales: arriba, abajo, izquierda, derecha, OK y Atrás).
- **Zonas Seguras contra Sobrebarrido (*TV Safe Margins*):** Márgenes perimétricos garantizados de 48-64dp para impedir recortes de texto en pantallas antiguas o proyectores.
- **Enfoque Exclusivo en Habla Hispana:** Todos los canales y categorías disponibles pertenecen rigurosamente a señales oficiales en idioma español (Hispanoamérica y España).
- **Transparencia y Cero Datos Falsos:** La app no cuenta con sistema de usuarios (sin login, cuentas ni avatares). Tampoco muestra guías EPG simuladas, métricas inventadas de red o previews de video falsas; toda la telemetría e indicadores mostrados (reloj `HH:mm`, modo de conexión en directo o proxy, indicador de fuente espejo `Fuente X/Y`) son 100% reales.

---

## 2. Barra Principal de Botones / Filtros (Menú Superior)
En la parte superior se presentan botones interactivos estilo cápsula refinada (`FilterCardButton`) de perfil fino, bordes nítidos y enfoque suave (`focusedScale = 1.08f`), permitiendo explorar el contenido por temáticas o países en el siguiente orden ergonómico:
- **Botón "Importante" (Primero en la barra):** Cápsula con contorno ámbar translúcido y tipografía dorada en estado de reposo. Al situar el control remoto encima, se activa un **marco perimétrico blanco brillante de 2.5dp (`Color.White`)**, escala ampliada a 1.08f y fondo ámbar sólido vibrante (`#F59E0B`) con tipografía de alto contraste, logrando una selección nítida e indiscutible idéntica al resto de botones del sistema. Al pulsarlo abre un diálogo interactivo panorámico con 3 Códigos QR simétricos para donaciones (PayPal y Yape), enlace a la web oficial del desarrollador y exención legal.
- **⭐ Botón "Favoritos" (Pestaña Inicial por Defecto):** Al iniciar la aplicación por primera vez, se sitúa de forma predeterminada en este filtro. En inicios posteriores, recuerda la última pestaña visitada por el usuario. Si aún no hay canales guardados, muestra una pantalla explicativa limpia.
- **🌎 Botón "Países" (Entre Favoritos y Deportes):** Activa la selección geográfica. Al seleccionarse, despliega una **segunda fila (sub-selector)** con todos los países soportados de Hispanoamérica y España.
- **⚽ Botón "Deportes":** Filtra canales de transmisiones deportivas en español, con purga de servidores permanentemente caídos y variantes con bloqueo geográfico.
- **📰 Botón "Noticias":** Filtra canales informativos y de actualidad en vivo en español.
- **🎬 Botón "Películas":** Filtra señales de cine en habla hispana, excluyendo canales generalistas, noticias o radios.
- **📺 Botón "Series":** Filtra canales de series, novelas y ficción televisiva en español.
- **👶 Botón "Infantil":** Filtra canales de dibujos animados y contenido familiar para niños.
- **🎵 Botón "Música":** Filtra canales musicales de habla hispana.
- **Botón "Studio Ghibli" (Cine On-Demand sin Íconos):** Situado al final de la fila de navegación, justo a la derecha de Música. Presenta un diseño sobrio y limpio sin emojis ni adornos visuales. Brinda acceso exclusivo a la colección oficial completa de los 25 largometrajes cinematográficos de Studio Ghibli en audio latino y riguroso orden cronológico (1984–2023: *Nausicaä del Valle del Viento*, *El castillo en el cielo*, *Mi vecino Totoro*, *El viaje de Chihiro*, *El increíble castillo vagabundo*, *El chico y la garza*, etc.), optimizados con streams de BitChute y respaldo en Internet Archive sin intermediarios.

### Cabecera Superior (Header):
- **Logotipo Oficial:** Marca registrada `Chari-` (blanco) + `IPTV` (azul cian `#B4C5FF`) de diseño cinematográfico estilizado.
- **Reloj Digital en Tiempo Real:** Ubicado en la esquina superior derecha, refleja la hora exacta del sistema Android TV (`HH:mm`) actualizándose automáticamente cada minuto.

### Sub-selector de Países Dinámico:
- **Detección Geográfica Inteligente (Híbrida):** Consulta el país del sistema Android TV y verifica en segundo plano la IP de conexión mediante GeoIP (`https://api.country.is/`). Si el usuario se conecta desde un país hispanohablante soportado (ej. Perú, México, Argentina, España, etc.), la aplicación preselecciona automáticamente dicho país. Si el usuario elige manualmente otro país, su preferencia personal se respeta y persiste en `SharedPreferences`.
- Despliega chips interactivos estilizados con la **bandera oficial** y el nombre de cada país (🇵🇪 Perú, 🇦🇷 Argentina, 🇨🇱 Chile, 🇨🇴 Colombia, 🇪🇸 España, 🇲🇽 México, 🇧🇴 Bolivia, 🇨🇷 Costa Rica, 🇨🇺 Cuba, 🇩🇴 Rep. Dominicana, 🇪🇨 Ecuador, 🇬🇹 Guatemala, 🇭🇳 Honduras, 🇳🇮 Nicaragua, 🇵🇦 Panamá, 🇵🇷 Puerto Rico, 🇵🇾 Paraguay, 🇸🇻 El Salvador, 🇺🇾 Uruguay, 🇻🇪 Venezuela).
- **Priorización 24/7:** Los canales activos con transmisión continua se ubican automáticamente al frente del listado.

### Atajo Rápido para Volver a las Pestañas (Subir sin Esfuerzo):
- **Tecla 'Atrás' (Back) del Control Remoto:** Al haber bajado varios niveles o filas en la cuadrícula de canales, presionar una sola vez la tecla **Atrás (Back)** devuelve la vista al inicio de la grilla de inmediato (`gridState.scrollToItem(0)`) sin animaciones lentas y sitúa el cursor de enfoque directamente sobre la pestaña o filtro actual.
- **Botón Visual `⬆ Subir a Pestañas [Atrás]`:** Aparece dinámicamente cuando el usuario se desplaza hacia abajo en la lista.

### Cinta de Noticias RSS (Marquee):
- Justo debajo de los botones/filtros y arriba de la grilla, una marquesina continua (`basicMarquee`) muestra los titulares y actualizaciones del blog oficial en tiempo real con etiqueta destacada `NOTICIAS`.

---

## 3. Grilla de Canales y Cine (Channel & Movie Grid)
- **Distribución Adaptativa de Columnas:**
  - **Canales de TV en Vivo (4 Columnas):** Distribución uniforme en 4 columnas con espaciado equilibrado de 14dp para todas las categorías estándar (Favoritos, Países, Deportes, Noticias, Películas, Series, Infantil, Música).
  - **Sección Studio Ghibli Cine On-Demand (2 Columnas):** La cuadrícula se expande dinámicamente a **2 columnas** (`GridCells.Fixed(2)`), otorgando el ancho necesario para desplegar tarjetas de formato cinematográfico horizontal con metadatos y sinopsis detallada.
- **Tarjetas de Canal Convencionales (Cards 16:11):** Para emisiones de televisión en directo:
  - *Color de Fondo en Reposo:* Slate oscuro (`#1E293B`) con sombra suave y borde translúcido de 1dp.
  - *Cambio Dinámico al Enfocar (Focus):* Al posicionar el cursor del control remoto encima, el fondo conmuta a **azul real brillante (`#2563EB`)**, la escala aumenta al 108% (`focusedScale = 1.08f`) y se activa un **marco blanco perimétrico de 3.5dp (`Color.White`)**.
  - *Logo:* Muestra el logotipo del canal centrado con ajuste proporcional (`ContentScale.Fit`). Utiliza Coil con soporte SVG y cabeceras de navegador de escritorio. Si el canal no dispone de logotipo público, se renderiza el isotipo oficial del televisor (`default_tv.png`).
  - *Nombre:* Centrado en la parte inferior sobre un zócalo semitransparente, en blanco nítido truncado a una línea.
  - *Badge EN VIVO / CINE:* Insignia superior izquierda con punto rojo en vivo para televisión o azul cian para películas en favoritos.
  - *Indicador de Favorito:* Botón circular en la esquina superior derecha (`⭐` si es favorito o `☆` si no lo es). Se puede alternar manteniendo presionado el botón OK (Long Press) o haciendo clic directo sobre la estrella (disponible en todas las categorías de TV en vivo; omitido en la sección exclusiva de cine Studio Ghibli).
- **Tarjetas de Cine Studio Ghibli (`GhibliMovieCardView` - Gran Formato):** Exclusivas para la sección `Studio Ghibli`:
  - *Dimensiones:* Formato horizontal espacioso (180dp de altura fija).
  - *Póster Vertical Izquierdo (Relación 2:3):* Carátula vertical en alta definición cargada asíncronamente con esquinas redondeadas y escalado proporcional exacto.
  - *Metadatos Cinematográficos:* Insignia `🎬 PELÍCULA`, año de estreno y duración exacta (`yyyy • Xh XXm`).
  - *Créditos:* Nombre del director oficial (ej. *Studio Ghibli • Dir. Hayao Miyazaki*).
  - *Sinopsis Argumental en Español:* Resumen argumental de 3 líneas con elipsis elegante, alimentado localmente por `GhibliData` con cero latencia de red.
  - *Acción Rápida:* Atajo `[OK] Audio Latino • Presiona OK para reproducir`. En esta sección se ha suprimido el botón de estrella y la función de favoritos para mantener una experiencia limpia y centrada en el catálogo cinematográfico.
  - *Supresión de Botón Escanear:* En el catálogo Studio Ghibli el botón `[🔄 Escanear]` se omite por tratarse de un repositorio digital estático de alta fidelidad.
- **Preservación de Foco:** Al regresar a la grilla tras reproducir un canal o película, la vista recuerda su estado y restaura el foco en el último elemento seleccionado gracias a `focusRestorer()`.

---

## 4. Barra Inferior Fija (TvFooterBar)
Ubicada en la parte inferior de la pantalla principal:
- **Lado Izquierdo:** Guía de teclas del control remoto (`[OK] Reproducir`, `[MANTENER OK] Favorito` —oculto automáticamente al navegar dentro de la sección Studio Ghibli—, `[ATRÁS] Subir a Pestañas`) y conteo real de canales o películas disponibles en la categoría/país en curso.
- **Lado Derecho (Mandatorio):** Identificador de versión oficial de la aplicación, legible y fijo en la esquina inferior derecha:  
  **`Chari-IPTV v1.2`** (obtenido dinámicamente de `PackageManager`).

---

## 5. Ventana Modal "Importante" (3 Códigos QR Simétricos y Soporte)
Al presionar el botón **Importante**, se despliega una ventana modal interactiva con estética Obsidian:
- **Fondo Obsidian OLED:** Superficie `#090D16` con tarjetas interiores `#181B25` y bordes perimétricos sutiles.
- **Banner de Gratuidad:** *"¡Muchas gracias por sus donaciones! Esta app es 100% gratuita y de libre acceso."* (presentado limpiamente sin títulos repetitivos ni íconos distractores).
- **Tres Columnas Simétricas (88dp):** Códigos QR optimizados a 88dp de lado, garantizando escaneo inmediato con smartphones y proporción vertical equilibrada para televisores 720p / 1080p:
  - **Columna 1 (💳 PayPal - Internacional):** Código QR local para donaciones voluntarias con tarjeta o saldo PayPal.
  - **Columna 2 (📱 Yape - Perú / BCP):** Código QR local para donaciones directas desde Perú mediante Yape.
  - **Columna 3 (🌐 Web Oficial - Desarrollador):** Código QR para visitar el portal oficial (`www.qwertyaserty.com`), novedades y contacto directo.
- **Aviso Legal y Propiedad Intelectual Íntegro & Desplazamiento Vertical:**
  - *Naturaleza del Software:* Cliente reproductor multimedia sin fines de lucro, sin servidores de retransmisión propios.
  - *Transmisiones en Vivo (IPTV):* Enlaces extraídos de índices públicos abiertos en internet (iptv-org, TDTChannels).
  - *Cine On-Demand (Studio Ghibli):* Enlaces a archivos públicos de preservación histórica alojados en Internet Archive (`archive.org`).
  - *Marcas Registradas y Copyright:* Pertenencia exclusiva a Studio Ghibli Inc. (Hayao Miyazaki, Isao Takahata) y las cadenas de televisión correspondientes.
  - *Exención de Responsabilidad:* Deslinde ante fallos de enlaces externos, geobloqueos o caídas de servidores ajenos.
  - *Soporte de Scroll D-Pad:* Incorpora desplazamiento vertical (`verticalScroll`) para garantizar que la totalidad de los 5 puntos legales y la leyenda de cierre se visualicen al 100% en pantallas de cualquier relación de aspecto o escala de fuentes.
- **Cierre Natural con Control Remoto:** Se eliminó el botón visual "Cerrar", optimizando el flujo para Android TV: presionar la tecla física **ATRÁS (Back)** en el control remoto cierra el diálogo al instante y devuelve el foco al menú principal. En la parte inferior se incluye la leyenda: `[ATRÁS] Presiona Atrás en el control remoto para cerrar`.

---

## 6. Reproductor de Video (ExoPlayerView) y Zapping Lateral

### Pantalla Completa & Conmutación en Caliente
- Al seleccionar cualquier canal, el reproductor ocupa toda la pantalla con fondo negro puro.
- La instancia de `ExoPlayer` optimizada con decodificador de audio FFmpeg reproduce de inmediato la señal HLS o MPEG-DASH.
- **Aislamiento de Ciclo de Vida:** La instancia de ExoPlayer se recrea limpiamente por cada URL y reintento (`remember(activeStreamUrl, retryCount)`), evitando por completo la contaminación de búferes o decodificadores entre transmisiones distintas.

### Cajón Lateral de Canales (*Quick Channel Drawer* - Zapping Rápido):
- **Activación:** Mientras se reproduce cualquier canal, presionar **D-Pad Izquierda (◀)** en el control remoto.
- **Despliegue Lateral:** Se desliza suavemente desde la izquierda una lista vertical semitransparente (`width = 340dp`, altura completa) con fondo Obsidian al 94% de opacidad, permitiendo continuar viendo el video de fondo sin cortes.
- **Contenido del Cajón:**
  - *Cabecera:* Nombre de la sección actual (ej. *⚽ Deportes*, *🌎 Perú*, *⭐ Favoritos*) y total de canales disponibles.
  - *Canal Activo:* Marcado con la etiqueta cian brillante `▶ EN REPRODUCCIÓN` y punto pulsante rojo.
  - *Canales Disponibles:* Logotipo, nombre del canal y estrella de favorito.
- **Auto-Ocultado por Inactividad (4 Segundos):** Si el cajón lateral está desplegado y no se detecta actividad del control remoto durante 4 segundos, el menú se repliega automáticamente hacia la izquierda de forma suave y sin cortes. Cualquier pulsación direccional (Arriba, Abajo, OK) o desplazamiento en la lista reinicia de inmediato el temporizador de inactividad.
- **Navegación & Cambio de Canal:**
  - `▲ / ▼`: Navegar entre los canales de la misma sección.
  - `OK / Enter`: Cambia el stream de inmediato en caliente (*in-place*) sin salir del reproductor ni recargar la pantalla. Si se presiona sobre el canal que ya se está reproduciendo, cierra el cajón.
  - `▶ (Derecha)` o `Atrás`: Cierran el cajón lateral y devuelven el foco completo al video a pantalla completa.

### Intercepción Nativa en la Actividad (`MainActivity.dispatchKeyEvent`):
- Los eventos físicos del control remoto se capturan directamente en `MainActivity.dispatchKeyEvent`.
- En reproducción normal, garantiza que las teclas direccionales no sean absorbidas por el `SurfaceView`.
- **Ante errores (`isError == true`), cede inmediatamente el control**, permitiendo que el D-Pad navegue y accione los botones de la interfaz de error con total libertad.

### HUD Superior e Inferior del Reproductor:
- **Detección Automática VOD / En Vivo:** ExoPlayerView distingue automáticamente si la señal es una emisión de televisión en directo o un archivo estático de cine bajo demanda (`.mp4`, `.mkv` o sección `ghibli`).
- **HUD Superior:** Logotipo, título grande, insignia dinámica (`DIRECTO` con pulsante rojo para televisión en vivo o `CINE VOD` en azul cian para películas y series bajo demanda), y telemetría de conexión:
  - `Modo: Conexión Directa` (emisión nativa directa a máxima velocidad).
  - `Modo: Proxy con Cabeceras` o `Modo: Proxy con Referer` (cuando se conmuta automáticamente tras un 403 Forbidden).
  - `Cine On-Demand · Archivo Multimedia` (en contenido VOD).
  - Indicador de fuente activa `Fuente X/Y` cuando el canal posee múltiples streams espejo registrados.
  - Reloj digital en tiempo real (`HH:mm`).
- **HUD Inferior:**
  - **Barra de Tiempo Interactiva (SeekBar VOD):** Para transmisiones bajo demanda, se despliega una barra de progreso continua con la posición actual del video y la duración total en formato `HH:mm:ss`, acompañada del estado `▶ EN REPRODUCCIÓN` o `⏸ EN PAUSA`.
  - **Atajos para Cine VOD:**
    - `[OK]`: Alternar Pausa y Reanudar video.
    - `[◀ / ▶]`: Rebobinar o adelantar ±15 segundos.
    - `[▼]`: Desplegar el catálogo lateral de capítulos y películas.
    - `[ATRÁS]`: Volver al menú principal.
  - **Atajos para TV En Vivo:**
    - `[◀]`: Lista de canales lateral (Quick Drawer).
    - `[OK]`: Alternar visibilidad de la información (HUD).
    - `[▲]`: Alternar Favorito (`⭐`).
    - `[ATRÁS]`: Volver al menú principal.
  - **Versión Oficial:** Se visualiza fija en la esquina inferior derecha (`Chari-IPTV v1.2`).
- **Auto-Ocultado:** El HUD se oculta automáticamente tras 5 segundos de inactividad o puede alternarse con el control remoto.

### Sistema de Reanudación de Reproducción Inteligente (Exclusivo Studio Ghibli):
- **Alcance Exclusivo:** Esta función opera únicamente para el catálogo cinematográfico de **Studio Ghibli (25 películas)** y no interfiere con los canales de televisión en vivo ni otras categorías.
- **Persistencia en Almacenamiento Local (`GhibliPlaybackStore`):**
  - Durante la reproducción de cualquiera de las 25 películas, la app guarda periódicamente la posición temporal (ms) y la duración total en `SharedPreferences`.
  - Asimismo, ante eventos del ciclo de vida (`onPause`, `onStop`, salida imprevista o pulsación del botón HOME del control remoto) o al cerrar el reproductor con `[ATRÁS]`, la posición exacta queda asegurada en memoria persistente.
  - *Umbrales Inteligentes:* Requiere un avance mínimo de 15 segundos para registrar progreso. Si el usuario ya vio el 95% de la película o restan menos de 60 segundos para el final, el progreso se limpia automáticamente para reiniciar desde 0:00 en la próxima visita.
- **Diálogo Modal al Reingresar a la Película:**
  - Si el usuario sale de la app o vuelve al menú y posteriormente reanuda la misma película, la app detecta el progreso guardado e inhabilita el inicio forzado automático (`playWhenReady = false`), precargando el stream en silencio en segundo plano.
  - Se despliega un diálogo modal centrado con diseño estilizado para Android TV:
    - *Insignia:* `🎬 STUDIO GHIBLI · CONTINUAR REPRODUCCIÓN`.
    - *Título:* Nombre oficial de la película.
    - *Indicador Temporal:* `Dejaste la película en <HH:mm:ss> de <HH:mm:ss>`.
    - *Pregunta:* "¿Deseas continuar la reproducción donde la dejaste o comenzar desde el principio?".
    - *Botón Enfocado por Defecto:* **[▶ Continuar (<HH:mm:ss>)]** (Al presionar OK, realiza `seekTo` al punto exacto y reanuda la reproducción al instante).
    - *Botón Secundario:* **[⏮ Comenzar de nuevo]** (Al presionar OK, resetea la posición guardada a 0:00 y arranca desde el inicio).
    - *Control Remoto:* `[◀ / ▶]` alterna entre ambos botones; `[OK]` confirma; `[ATRÁS]` cierra el reproductor y regresa a la cuadrícula de películas.

### Manejo de Fallos & Reconexión Automática Resiliente (3 Intentos):
- **Reconexión Automática sin Interrupciones:** Ante microcortes de red, pérdidas de paquetes o caídas momentáneas del servidor emisor, la app **no** despliega el cartel de error de inmediato. En su lugar, ejecuta automáticamente un ciclo de **al menos 3 reintentos continuos** (`autoRetryCount < 3`), manteniendo al usuario en la experiencia de reproducción.
- **Feedback Visual en Pantalla:** Durante el proceso de reconexión, el indicador de buffering informa en tiempo real:
  > *"Reconectando señal (intento X de 3)..."*
- **Watchdog Inteligente (15s en TV / 35s en VOD):** El temporizador de protección en segundo plano acompaña estos intentos antes de conmutar a fuentes espejo secundarias o declarar la señal fuera de emisión.
- **Failover a Proxy Local:** Si una señal emite HTTP 403 Forbidden o HTTP 451, el reproductor conmuta de manera transparente al micro-proxy loopback (`LocalHlsProxyServer`) reescribiendo cabeceras para saltar el bloqueo geográfico.
- **Fuentes Espejo:** Si el canal posee fuentes espejo alternativas, realiza conmutación automática secuencial antes de emitir error.
- **Diálogo Modal de Desconexión:** Solo tras agotar todos los 3 reintentos automáticos y servidores alternativos sin éxito, se muestra el diálogo modal centrado con el logotipo del canal, aviso de *Señal Temporalmente No Disponible* y dos botones enfocables con D-Pad:
  - **🔄 Reintentar:** Reinicia manualmente el ciclo de carga y reconexión del stream.
  - **⬅️ Volver a Canales (Navegación Contextual):**
    - *Navegación desde Menú Lateral:* Si el usuario estaba zappeando mediante el menú lateral izquierdo (`QuickChannelDrawer`) y el canal falla (ej. HTTP 404 / 403), pulsar `[Volver a Canales]` o accionar la tecla **D-Pad Izquierda (◀)** del control remoto reabre de inmediato el **menú lateral izquierdo** en la capa superior sin salir de la sesión de reproducción, permitiendo elegir otro canal con total inmediatez.
    - *Navegación desde Grilla de Tarjetas:* Si el canal fue iniciado directamente desde la cuadrícula principal (`ChannelGridScreen`) y falla de entrada, pulsar `[Volver a Canales]` cierra el reproductor y devuelve el foco exactamente a la cuadrícula de tarjetas donde se encontraba el usuario.
    - *Acceso Directo con D-Pad Izquierda:* En la pantalla de error, presionar la tecla física **◀ (Izquierda)** del control remoto siempre permite desplegar el menú lateral de canales de la sección actual para cambiar de emisión rápidamente.

### Diagnóstico de Señales HTTP 403 Forbidden & Medidas Aplicadas:
Cuando una señal responde con el error `HTTP 403: posible geobloqueo o señal fuera de línea`:
- **Origen de la Falla:** Se debe primordialmente a dos factores de origen externo:
  1. *Servidores Restreamers Privados (Cesbo Astra / Xtream Codes en puerto 8000):* Canales deportivos como DSports alojados en IPs públicas temporales utilizan tokens de sesión o restricción a subredes de un ISP determinado; cuando el token vence o la retransmisión se cancela, el servidor responde con `403 Forbidden`.
  2. *Geobloqueo Estricto de CDNs Comerciales:* Redes de distribución (Akamai, CloudFront, Cloudflare) validan la IP del cliente o exigen cabeceras `Referer` y `Origin` del dominio oficial.
- **Medidas Preventivas en la Aplicación:**
  - *Filtrado y Purga en `Constants.kt`:* Se bloquea de forma preventiva el acceso a servidores Astra muertos o inestables conocidos (`38.187.7.252`, `187.102.208.209`).
  - *Emulación de Cabeceras en Proxy:* Si la señal es legítima y solo requiere validación de procedencia, el micro-proxy loopback inyecta las cabeceras HTTP correspondientes.
  - *Failover a Fuentes Espejo:* Si el canal cuenta con URLs alternativas registradas, se conmuta silenciosamente a la siguiente fuente antes de alertar al usuario.

---

## 7. Integración con el Launcher de Android TV (Leanback Home Screen)
- **Fila de Canales Destacados:** Gracias a `androidx.tvprovider`, publica la fila interactiva *"Chari-IPTV: Canales Destacados"* en la pantalla de inicio del televisor.
- **Fila "Continuar viendo" (Watch Next):** Cada reproducción registra automáticamente el canal en la fila del sistema.
- **Deep Linking Nativo:** Al seleccionar cualquier tarjeta en la pantalla de inicio de Android TV, se inicia directamente la reproducción a pantalla completa.
