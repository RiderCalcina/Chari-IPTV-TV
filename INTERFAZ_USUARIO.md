# 📺 Interfaz de Usuario: Chari-IPTV TV (v1.1)

La interfaz de usuario de **Chari-IPTV TV** está diseñada específicamente para Android TV y Google TV utilizando el moderno kit de herramientas **Jetpack Compose (androidx.tv.material3)** y las pautas de diseño para salas de estar **Obsidian Cinema TV (10-Foot UI)**. A continuación, se detalla cada aspecto visual, interactivo y de navegación de la aplicación.

---

## 1. Diseño General (Look & Feel: Obsidian Cinema TV)
- **Tema Oscuro Puro OLED (Obsidian Dark):** Fondo canvas ultraoscuro (`#090D16`), calibrado para evitar el sangrado de luz en paneles OLED y reducir la fatiga visual en entornos oscuros.
- **Superficie de Tarjetas Elevadas (Slate):** Los contenedores y bandejas de navegación descansan sobre tono pizarra (`#1E293B`) con bordes translúcidos sutiles de 1dp (`#33FFFFFF`), logrando una separación nítida sin líneas agresivas.
- **Navegación D-Pad Pura:** El 100% de los elementos interactivos responden al control remoto convencional (botones direccionales: arriba, abajo, izquierda, derecha, OK y Atrás).
- **Zonas Seguras contra Sobrebarrido (*TV Safe Margins*):** Márgenes perimétricos garantizados de 48-64dp para impedir recortes de texto en pantallas antiguas o proyectores.
- **Enfoque Exclusivo en Habla Hispana:** Todos los canales y categorías disponibles pertenecen rigurosamente a señales oficiales en idioma español (Hispanoamérica y España).
- **Transparencia y Cero Datos Falsos:** La app no cuenta con sistema de usuarios (sin login, cuentas ni avatares). Tampoco muestra guías EPG simuladas, métricas inventadas de red o previews de video falsas; toda la telemetría e indicadores mostrados (reloj `HH:mm`, estado del proxy anti-geobloqueo, listas de canales) son 100% reales.

---

## 2. Barra Principal de Botones / Filtros (Menú Superior)
En la parte superior se presentan botones interactivos estilo cápsula refinada (`FilterCardButton`) de perfil fino, bordes nítidos y enfoque suave (`focusedScale = 1.08f`), permitiendo explorar el contenido por temáticas o países en el siguiente orden ergonómico:
- **Botón "Importante" (Primero en la barra):** Destacado con acento dorado/ámbar (`#F59E0B`). Al pulsarlo abre un diálogo interactivo panorámico con 3 Códigos QR simétricos para donaciones (PayPal y Yape), visita al sitio web oficial del desarrollador y aviso legal de exención de responsabilidad.
- **⭐ Botón "Favoritos" (Pestaña Inicial por Defecto):** Al iniciar la aplicación por primera vez, se sitúa de forma predeterminada en este filtro. En inicios posteriores, recuerda la última pestaña visitada por el usuario. Si aún no hay canales guardados, muestra una pantalla explicativa limpia.
- **🌎 Botón "Países" (Entre Favoritos y Deportes):** Activa la selección geográfica. Al seleccionarse, despliega una **segunda fila (sub-selector)** con todos los países soportados de Hispanoamérica y España.
- **⚽ Botón "Deportes":** Filtra canales de transmisiones deportivas en español.
- **📰 Botón "Noticias":** Filtra canales informativos y de actualidad en vivo en español.
- **🎬 Botón "Películas":** Filtra señales de cine en habla hispana, excluyendo canales generalistas, noticias o radios.
- **📺 Botón "Series":** Filtra canales de series, novelas y ficción televisiva en español.
- **👶 Botón "Infantil":** Filtra canales de dibujos animados y contenido familiar para niños.
- **🎵 Botón "Música":** Filtra canales musicales de habla hispana.

### Cabecera Superior (Header):
- **Logotipo Oficial:** Texto `Chari` (blanco) + `IPTV` (azul `#B4C5FF`) junto a la insignia luminosa `EN VIVO` con punto rojo.
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

## 3. Grilla de Canales (Channel Grid)
- **Distribución:** Cuadrícula fija en **4 columnas** con espaciado equilibrado de 14dp.
- **Tarjetas de Canal (Cards 16:11):** Cada canal está representado por una tarjeta en proporción 16:11.
  - *Color de Fondo en Reposo:* Slate oscuro (`#1E293B`) con sombra suave y borde translúcido de 1dp.
  - *Cambio Dinámico al Enfocar (Focus):* Al posicionar el cursor del control remoto encima, el fondo conmuta a **azul real brillante (`#2563EB`)**, la escala aumenta al 108% (`focusedScale = 1.08f`) y se activa un **marco blanco perimétrico de 3.5dp (`Color.White`)**.
  - *Logo:* Muestra el logotipo del canal centrado con ajuste proporcional (`ContentScale.Fit`). Utiliza Coil con soporte SVG y cabeceras de navegador de escritorio. Si el canal no dispone de logotipo público, se renderiza el isotipo oficial del televisor (`default_tv.png`).
  - *Nombre:* Centrado en la parte inferior sobre un zócalo semitransparente, en blanco nítido truncado a una línea.
  - *Badge EN VIVO:* Insignia superior izquierda con punto rojo en vivo.
  - *Indicador de Favorito:* Botón circular en la esquina superior derecha (`⭐` si es favorito o `☆` si no lo es). Se puede alternar manteniendo presionado el botón OK (Long Press) o haciendo clic directo sobre la estrella.
- **Preservación de Foco:** Al regresar a la grilla tras reproducir un canal, la vista recuerda su estado y restaura el foco en el último canal seleccionado gracias a `focusRestorer()`.

---

## 4. Barra Inferior Fija (TvFooterBar)
Ubicada en la parte inferior de la pantalla principal:
- **Lado Izquierdo:** Guía de teclas del control remoto (`[OK] Reproducir`, `[MANTENER OK] Favorito`, `[ATRÁS] Subir a Pestañas`) y conteo real de canales activos en la categoría/país en curso.
- **Lado Derecho (Mandatorio):** Identificador de versión oficial de la aplicación, legible y fijo en la esquina inferior derecha:  
  **`Chari-IPTV TV v1.1`** (obtenido dinámicamente de `PackageManager`).

---

## 5. Ventana Modal "Importante" (3 Códigos QR Simétricos y Soporte)
Al presionar el botón **Importante**, se despliega una ventana modal interactiva con estética Obsidian:
- **Fondo Obsidian OLED:** Superficie `#090D16` con tarjetas interiores `#181B25` y bordes de 1dp.
- **Agradecimiento y Gratuidad:** *"❤️ ¡Muchas gracias por sus donaciones! Esta app es 100% gratuita y de libre acceso."*
- **Tres Columnas Simétricas:**
  - **Columna 1 (💳 PayPal - Internacional):** Código QR local para donaciones con tarjeta o cuenta PayPal.
  - **Columna 2 (📱 Yape - Perú / BCP):** Código QR local para donaciones directas desde Perú mediante Yape.
  - **Columna 3 (🌐 Web Oficial - Desarrollador):** Código QR para visitar el portal oficial (`www.qwertyaserty.com`), novedades y contacto.
- **Botón "Cerrar":** Formato cápsula con enfoque automático inmediato (`FocusRequester`), cerrable con un toque de OK.
- **Aviso Legal:** Exención formal de responsabilidad sobre transmisiones abiertas y derechos de autor.

---

## 6. Reproductor de Video (ExoPlayerView) y Zapping Lateral

### Pantalla Completa & Conmutación en Caliente
- Al seleccionar cualquier canal, el reproductor ocupa toda la pantalla con fondo negro puro.
- La instancia de `ExoPlayer` optimizada con decodificador de audio FFmpeg reproduce de inmediato la señal HLS o MPEG-DASH.

### Cajón Lateral de Canales (*Quick Channel Drawer* - Zapping Rápido):
- **Activación:** Mientras se reproduce cualquier canal, presionar **D-Pad Izquierda (◀)** en el control remoto.
- **Despliegue Lateral:** Se desliza suavemente desde la izquierda una lista vertical semitransparente (`width = 340dp`, altura completa) con fondo Obsidian al 94% de opacidad, permitiendo continuar viendo el video de fondo sin cortes.
- **Contenido del Cajón:**
  - *Cabecera:* Nombre de la sección actual (ej. *⚽ Deportes*, *🌎 Perú*, *⭐ Favoritos*) y total de canales disponibles.
  - *Canal Activo:* Marcado con la etiqueta cian brillante `▶ EN REPRODUCCIÓN` y punto pulsante rojo.
  - *Canales Disponibles:* Logotipo, nombre del canal y estrella de favorito.
- **Navegación & Cambio de Canal:**
  - `▲ / ▼`: Navegar entre los canales de la misma sección.
  - `OK / Enter`: Cambia el stream de inmediato en caliente (*in-place*) sin salir del reproductor ni recargar la pantalla. Si se presiona sobre el canal que ya se está reproduciendo, cierra el cajón.
  - `▶ (Derecha)` o `Atrás`: Cierran el cajón lateral y devuelven el foco completo al video a pantalla completa.

### Intercepción Nativa en la Actividad (`MainActivity.dispatchKeyEvent`):
- Los eventos físicos del control remoto se capturan directamente en `MainActivity.dispatchKeyEvent` antes de que el `SurfaceView` de ExoPlayer pueda absorberlos, garantizando que el D-Pad responda siempre al 100%.

### HUD Superior e Inferior del Reproductor:
- **HUD Superior:** Logotipo del canal, nombre en grande, insignia `DIRECTO`, estado real de la conexión (`Modo: Conexión Directa HLS` o `Modo: Proxy Anti-Geobloqueo Activo`) y reloj en tiempo real.
- **HUD Inferior:** Atajos de reproducción (`[◀] Lista de Canales`, `[ATRÁS] Volver a Menú`, `[▲] Alternar Favorito`) y versión **`v1.1`** en la esquina inferior derecha.
- **Auto-Ocultado:** El HUD se oculta automáticamente tras 5 segundos de inactividad o puede alternarse con el botón central **OK**.

### Manejo de Fallos & Proxy Anti-Geobloqueo:
- Si una señal emite HTTP 403 Forbidden o HTTP 451, el reproductor activa automáticamente el micro-proxy loopback (`LocalHlsProxyServer`) reescribiendo cabeceras para saltar el bloqueo geográfico.
- Si la señal está caída físicamente, se muestra una ventana amigable de error con el logo, descripción y dos botones enfocables: **🔄 Reintentar** y **⬅️ Volver a Canales**.

---

## 7. Integración con el Launcher de Android TV (Leanback Home Screen)
- **Fila de Canales Destacados:** Gracias a `androidx.tvprovider`, publica la fila interactiva *"Chari-IPTV: Canales Destacados"* en la pantalla de inicio del televisor.
- **Fila "Continuar viendo" (Watch Next):** Cada reproducción registra automáticamente el canal en la fila del sistema.
- **Deep Linking Nativo:** Al seleccionar cualquier tarjeta en la pantalla de inicio de Android TV, se inicia directamente la reproducción a pantalla completa.
