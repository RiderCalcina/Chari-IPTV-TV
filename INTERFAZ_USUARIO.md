# 📺 Interfaz de Usuario: Chari-IPTV TV

La interfaz de usuario de **Chari-IPTV TV** está diseñada específicamente para Android TV y Google TV utilizando el moderno kit de herramientas **Jetpack Compose (androidx.tv.material3)**. A continuación, se detalla cada aspecto visual e interactivo de la aplicación.

---

## 1. Diseño General (Look & Feel)
- **Tema Oscuro (Dark Mode):** Toda la aplicación tiene un fondo oscuro profundo (Color `#0F172A`), ideal para entornos con poca luz como la sala de estar.
- **Navegación D-Pad Pura:** Todos los elementos interactivos están optimizados para el control remoto tradicional (botones direccionales: arriba, abajo, izquierda, derecha, OK y Atrás).
- **Enfoque Exclusivo en Habla Hispana:** Todos los canales y categorías disponibles en la interfaz pertenecen rigurosamente a señales en idioma español (Hispanoamérica y España), asegurando una experiencia completamente adaptada al público hispanohablante.

---

## 2. Barra Principal de Botones / Filtros (Menú Superior)
En la parte superior se presentan botones interactivos estilo tarjeta refinada (`FilterCardButton`) de perfil fino, bordes nítidos y enfoque suave (`focusedScale = 1.08f`), permitiendo explorar el contenido por temáticas o países en el siguiente orden ergonómico:
- **Botón "Importante" (Primero en la barra):** Destacado con acento dorado/amarillo y tipografía limpia (sin iconos adicionales). Al pulsarlo abre un diálogo interactivo panorámico con Códigos QR simétricos para donaciones (PayPal y Yape), visita al sitio web oficial del desarrollador y aviso legal de exención de responsabilidad.
- **⭐ Botón "Favoritos" (Pestaña Inicial por Defecto):** Al iniciar la aplicación por primera vez, se sitúa de forma predeterminada en este filtro, permitiendo al usuario acceder directamente a sus señales preferidas sin rodeos. En inicios posteriores, recuerda la última pestaña visitada por el usuario. Si aún no hay canales guardados, muestra una pantalla orientativa amigable.
- **🌎 Botón "Países" (Entre Favoritos y Deportes):** Activa la selección geográfica inmediatamente después de favoritos. Al seleccionarse, despliega una **segunda fila (sub-selector)** con todos los países soportados de Hispanoamérica y España.
- **⚽ Botón "Deportes":** Filtra canales de transmisiones deportivas en español (fútbol, automovilismo, básquet, etc.).
- **📰 Botón "Noticias":** Filtra canales informativos y de actualidad en vivo en español.
- **🎬 Botón "Películas":** Filtra señales de cine en habla hispana, excluyendo canales generalistas, noticias, deportes o radios.
- **📺 Botón "Series":** Filtra canales de series, novelas y ficción televisiva en español mediante filtro estricto por palabras clave y metadata.
- **👶 Botón "Infantil":** Filtra canales de dibujos animados y contenido familiar para niños en español.
- **🎵 Botón "Música":** Filtra canales musicales de habla hispana.

### Sub-selector de Países, Geolocalización Automática y Sincronización
- **Detección Geográfica Inteligente (Híbrida):** Al arrancar la aplicación, el sistema consulta el país del sistema Android TV (`Locale.getDefault().country`) y verifica en segundo plano la IP de conexión mediante GeoIP (`https://api.country.is/`). Si el usuario se conecta desde un país hispanohablante soportado (ej. Perú, México, Argentina, España, etc.), la aplicación preselecciona automáticamente dicho país. Si el usuario elige manualmente otro país, su preferencia personal se respeta y persiste en `SharedPreferences`.
- Al estar activo el botón "Países", se despliega una segunda fila con chips interactivos estilizados con la **bandera oficial** y el nombre de cada país (🇵🇪 Perú, 🇦🇷 Argentina, 🇨🇱 Chile, 🇨🇴 Colombia, 🇪🇸 España, 🇲🇽 México, 🇧🇴 Bolivia, 🇨🇷 Costa Rica, 🇨🇺 Cuba, 🇩🇴 Rep. Dominicana, 🇪🇨 Ecuador, 🇬🇹 Guatemala, 🇭🇳 Honduras, 🇳🇮 Nicaragua, 🇵🇦 Panamá, 🇵🇷 Puerto Rico, 🇵🇾 Paraguay, 🇸🇻 El Salvador, 🇺🇾 Uruguay, 🇻🇪 Venezuela).
- **Limpieza Instantánea y Feedback al Cambiar de País:** Al seleccionar un país nuevo, la grilla limpia inmediatamente los canales residuales del país anterior y activa el indicador de carga, evitando cualquier confusión visual.
- **Sincronización Total de Posición y Foco:** Al acceder a "Países", la lista horizontal se desplaza automáticamente (`countryRowState.animateScrollToItem`) para centrar el país seleccionado en la pantalla y el cursor de foco se ubica directamente sobre dicho botón.
- **Priorización de Emisiones 24/7:** Los canales activos con transmisión ininterrumpida 24/7 se ubican automáticamente al frente del listado, evitando que feeds comunitarios `[Not 24/7]` fuera de línea bloqueen el acceso inicial.

### Atajo Rápido para Volver a las Pestañas (Subir sin Esfuerzo)
- **Tecla 'Atrás' (Back) del Control Remoto:** Al haber bajado varios niveles o filas en la cuadrícula de canales, presionar una sola vez la tecla **Atrás (Back)** del mando a distancia devuelve la vista al inicio de la grilla de inmediato (`gridState.scrollToItem(0)`) sin animaciones lentas y sitúa el cursor de enfoque directamente sobre la pestaña o filtro actual.
- **Botón Visual `⬆ Subir a Pestañas [Atrás]`:** Cuando el usuario se desplaza hacia abajo en la lista, aparece un chip interactivo visible en la barra de estado superior. Puede ser seleccionado y pulsado con el control para regresar instantáneamente a las pestañas.

### Re-escaneo Inteligente
- Si se presiona el botón **🔄 Escanear**, o se presiona nuevamente el botón del país/filtro activo estando la lista vacía, aparecerá un diálogo de confirmación destacado para **volver a escanear** los canales en vivo desde el servidor, renovando la base de datos local Room.

### Cinta de Noticias RSS (Marquee)
- Justo debajo de los botones/filtros y arriba de la grilla, una marquesina continua (`basicMarquee`) muestra los titulares y actualizaciones del blog oficial en tiempo real.

---

## 3. Grilla de Canales (Channel Grid)
- **Distribución:** Es una cuadrícula (`TvLazyVerticalGrid`) organizada en **4 columnas** fijas.
- **Tarjetas de Canal (Cards):** Cada canal está representado por una tarjeta rectangular (proporción 16:11).
  - *Color de Fondo en Reposo:* Tono oscuro slate (`#1E293B`) que se integra de manera armónica en el entorno nocturno y de sala de estar.
  - *Cambio Dinámico al Enfocar (Focus):* Al posicionar el cursor del control remoto encima, el fondo de la tarjeta conmuta de inmediato a **azul real brillante (`#2563EB`)**, iluminando por completo el elemento activo.
  - *Sombra y Bordes:* En reposo muestra un borde sutil translúcido (`#33FFFFFF`) de 1dp con elevación de 8dp. Al enfocar, se activa un **borde blanco grueso de 3.5dp de grosor (`Color.White`)** y una escala aumentada al **110% (`focusedScale = 1.10f`)**, permitiendo al usuario identificar de un solo vistazo y con total certeza dónde se encuentra ubicado en la cuadrícula.
  - *Logo:* Muestra el logotipo del canal centrado con ajuste proporcional (`ContentScale.Fit`). Utiliza Coil con soporte SVG (`coil-svg`) y cabeceras de navegador de escritorio. Si el canal no dispone de logotipo público o el enlace está roto (404), muestra el logotipo oficial por defecto de TV (`default_tv.png`).
  - *Nombre:* El nombre del canal aparece centrado abajo del logo, con tipografía en color blanco puro (`Color.White`), nítida y legible tanto en reposo como enfocada, truncada a un máximo de 2 líneas.
- **Indicador de Favorito:** En la esquina superior derecha de cada tarjeta hay un botón circular con una estrella (`⭐` si es favorito o `☆` si no lo es). Para marcar o desmarcar un canal como favorito, se puede hacer clic directamente sobre la estrella o **mantener presionado el botón OK** (D-Pad Center) del control remoto (Long Press). Aparecerá una notificación animada en la pantalla confirmando la acción (⭐ Añadido a favoritos o ☆ Eliminado de favoritos).
- **Efectos de Foco (Hover):** Cuando se navega con el control remoto sobre una tarjeta:
  - La tarjeta aumenta de tamaño (`focusedScale = 1.10f`).
  - El fondo conmuta drásticamente de `#1E293B` a `#2563EB`.
  - Se ilumina con un marco perimétrico blanco de 3.5dp.
- **Estado Vacío Intuitivo:** Si la categoría o la pestaña Favoritos no tiene canales, muestra un diseño explicativo limpio indicando cómo agregar canales preferidos mediante el ícono de estrella.
- **Preservación de Foco:** Al regresar a la grilla tras reproducir un canal, la vista recuerda su estado y restaura el foco en el último canal seleccionado gracias a `focusRestorer()`.

---

## 4. Ventana Modal "Importante" (3 Códigos QR Simétricos y Soporte)
Al presionar el botón **Importante**, se despliega una ventana modal interactiva con estética profunda y estilizada:
- **Tono Oscuro OLED Premium:** Fondo obsidian/slate (`#090D16`) con tarjetas interiores (`#111827`) y delicados bordes de 1dp que maximizan el contraste y la elegancia en televisores.
- **Visualización Completa Sin Recortes:** Códigos QR simétricos redimensionados a `118.dp x 118.dp` con espaciados y tipografía optimizada, garantizando que el 100% de los textos, descripciones y direcciones web (`paypal.com`, `Yape (Perú)`, `www.qwertyaserty.com`) sean perfectamente legibles sin recortes verticales.
- **Mensaje de Agradecimiento y Gratuidad:** En la cabecera destaca un aviso especial: *"❤️ ¡Muchas gracias por sus donaciones! Esta app es 100% gratuita y de libre acceso."*
- **Simetría Visual:** Tres columnas idénticas y perfectamente equilibradas (`weight = 1f`), cada una con un contenedor blanco de bordes redondeados y códigos QR empaquetados localmente:
  - **Columna 1 (💳 PayPal - Internacional):** Código QR local (`qr_paypal.png`) para realizar donaciones voluntarias con tarjeta o cuenta PayPal.
  - **Columna 2 (📱 Yape - Perú / BCP):** Código QR local (`qr_yape.jpg`) para realizar donaciones directas desde Perú mediante la aplicación Yape.
  - **Columna 3 (🌐 Web Oficial - Desarrollador):** Código QR (`qr_web.png`) para visitar el portal oficial del desarrollador (`www.qwertyaserty.com`), consultar novedades, reportes y contacto.
- **Botón "Cerrar":** Diseñado en formato cápsula/píldora (`RoundedCornerShape(20.dp)`), con trazo sutil, tipografía estilizada y enfoque automático inmediato (`FocusRequester`), permitiendo cerrar el diálogo con un solo toque de OK.
- **Aviso Legal y Exención de Responsabilidad:** En la parte inferior destaca un recuadro formal delimitado por un borde sutil: *"ℹ️ Aviso legal: Esta aplicación es un reproductor multimedia y no almacena, retransmite ni controla el contenido emitido por cada canal. Todos los enlaces provienen de fuentes públicas y abiertas en internet. El desarrollador no se responsabiliza por la programación, disponibilidad, derechos de autor o contenidos provistos por terceros."*

---

## 5. Indicador de Carga (Loading State)
- Mientras se descargan y validan los canales de una categoría o país por primera vez, o durante un re-escaneo, la grilla muestra un indicador de carga central.
- La barra de navegación de botones **nunca desaparece** durante la carga. El indicador de progreso se renderiza *debajo* de los botones, permitiendo cambiar de filtro en cualquier momento sin trabas de foco.
- Muestra un **Círculo de Progreso** (CircularProgressIndicator) blanco en el centro con el texto de estado.

---

## 6. Reproductor de Video (ExoPlayerView) y Manejo Amigable de Errores
- **Pantalla Completa:** Al seleccionar un canal, el reproductor ocupa toda la pantalla con un fondo negro puro. La grilla queda "debajo" conservando su estado en memoria.
- **Manejo Estricto del Ciclo de Vida y Prevención de Fugas de Audio:** Cada reproductor `ExoPlayer` se instancia unívocamente por canal y se destruye de forma estricta (`stop()`, `clearMediaItems()`, `release()`) mediante `DisposableEffect` al cambiar de canal o cerrar el reproductor. Además, `MainActivity.onStop()` y `onDestroy()` limpian cualquier sesión activa, impidiendo por completo la mezcla de audios o que una señal siga sonando de fondo tras salir al menú del televisor.
- **Controles Nativos e Interacción:**
  - **Salir del Video:** Botón **Retroceder (Back / Return)** del control remoto.
  - **Alternar Favorito:** Presionar la tecla direccional **Arriba (D-Pad Up)** en el control remoto para añadir o quitar el canal de favoritos con feedback mediante Snackbar animado.
- **Prevención de Apagado:** Envía la bandera `FLAG_KEEP_SCREEN_ON` para evitar que el televisor active el salvapantallas o reposo durante la reproducción.
- **HUD de Carga Limpio:** Durante la conexión inicial, muestra un cuadro flotante centrado con spinner circular y *"Conectando con &lt;Nombre de Canal&gt;..."*, sin textos adicionales redundantes.
- **Activación Selectiva de Proxy Anti-Geobloqueo:** Si el stream devuelve un error de geobloqueo (HTTP 403 Forbidden o HTTP 451), el reproductor activa de inmediato el micro-servidor proxy loopback (`LocalHlsProxyServer`) para omitir las restricciones de cabeceras. Las demás señales se reproducen de forma directa.
- **Pantalla Superpuesta para Canales No Disponibles (Friendly Error Overlay):**
  - Si un canal falla por servidor caído o indisponibilidad física del emisor, muestra una ventana superpuesta con el logo del canal, el nombre en grande, una etiqueta de advertencia (`⚠️ Señal Temporalmente No Disponible`) y un mensaje claro explicando la causa.
  - Incluye dos botones enfocables para el control remoto:
    1. **🔄 Reintentar:** Vuelve a conectar con el servidor emisor de inmediato.
    2. **⬅️ Volver a Canales:** Regresa a la cuadrícula de canales sin perder el foco en la posición anterior.
- **Temporizador de Timeout (Anti-Buffering Infinito):** Si un stream queda congelado en estado de carga durante más de 20 segundos sin responder, el sistema activa automáticamente la pantalla amigable de canal no disponible.
- **Decodificador de Audio por Software (FFmpeg Integrado):** Incorpora el decodificador nativo de audio por software `media3-ffmpeg-decoder` (Jellyfin). Si el hardware del televisor o unos auriculares Bluetooth conectados no admiten pistas de audio envolventes como **AC3 (Dolby Digital), E-AC3 o DTS**, el motor conmuta automáticamente a FFmpeg para decodificar a estéreo en tiempo real, garantizando sonido en el 100% de las transmisiones.

---

## 7. Integración con el Launcher de Android TV (Leanback Home Screen)
- **Fila de Canales Destacados:** Gracias a `androidx.tvprovider`, la aplicación publica automáticamente la fila *"Chari-IPTV: Canales Destacados"* en la pantalla de inicio del televisor (debajo de las aplicaciones favoritas, junto a Pluto TV o Claro-video).
- **Fila "Continuar viendo" (Watch Next):** Cada vez que el usuario reproduce un canal, se registra automáticamente en la fila del sistema Android TV con su logo y nombre.
- **Deep Linking Nativo:** Al seleccionar cualquier tarjeta en la pantalla de inicio de Android TV, se lanza directamente la reproducción a pantalla completa de la señal elegida sin pasar por menús intermedios.
