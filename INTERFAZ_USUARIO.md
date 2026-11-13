# 📺 Interfaz de Usuario: Chari-IPTV TV

La interfaz de usuario de **Chari-IPTV TV** está diseñada específicamente para Android TV utilizando el moderno kit de herramientas **Jetpack Compose (androidx.tv.material3)**. A continuación, se detalla cada aspecto visual e interactivo de la aplicación.

---

## 1. Diseño General (Look & Feel)
- **Tema Oscuro (Dark Mode):** Toda la aplicación tiene un fondo oscuro profundo (Color `#0F172A`), ideal para entornos con poca luz como la sala de estar.
- **Navegación D-Pad Pura:** Todos los elementos interactivos están optimizados para el control remoto tradicional (botones direccionales: arriba, abajo, izquierda, derecha y OK).
- **Enfoque Exclusivo en Habla Hispana:** Todos los canales y categorías disponibles en la interfaz pertenecen rigurosamente a señales en idioma español (Hispanoamérica y España), asegurando una experiencia completamente adaptada al público hispanohablante.

---

## 2. Barra Principal de Botones / Filtros (Menú Superior)
En la parte superior se presentan botones interactivos estilo tarjeta refinada (`FilterCardButton`) de perfil fino, bordes nítidos y enfoque suave (`focusedScale = 1.08f`), permitiendo explorar el contenido por temáticas o países en el siguiente orden ergonómico:
- **⭐ Botón "Importante" (Primero en la barra):** Destacado con acento dorado/amarillo. Al pulsarlo abre un diálogo interactivo panorámico con Códigos QR simétricos nativos para donaciones (PayPal y Yape), visita al sitio web oficial del desarrollador y aviso legal de exención de responsabilidad.
- **❤️ Botón "Favoritos" (Pestaña Inicial por Defecto):** Al iniciar la aplicación, se sitúa de forma predeterminada en este filtro, permitiendo al usuario acceder directamente a sus señales preferidas sin rodeos. Si aún no hay canales guardados, muestra una pantalla orientativa amigable.
- **🌎 Botón "Países" (Entre Favoritos y Deportes):** Activa la selección geográfica inmediatamente después de favoritos. Al seleccionarse, despliega una **segunda fila (sub-selector)** con todos los países soportados de Hispanoamérica y España.
- **⚽ Botón "Deportes":** Filtra canales de transmisiones deportivas (fútbol, automovilismo, básquet, etc.) en español.
- **📰 Botón "Noticias":** Filtra canales informativos y de actualidad en vivo en español.
- **🎬 Botón "Películas":** Filtra señales dedicadas al cine en habla hispana.
- **📺 Botón "Series":** Filtra señales dedicadas a series y ficción televisiva en español.
- **👶 Botón "Infantil":** Filtra canales de dibujos animados y contenido familiar para niños en español.
- **🎵 Botón "Música":** Filtra canales musicales de habla hispana.

### Sub-selector de Países, Geolocalización Automática y Sincronización
- **Detección Geográfica Inteligente (Híbrida):** Al arrancar la aplicación, el sistema consulta de forma instantánea el país del sistema Android TV (`Locale.getDefault().country`) y verifica en segundo plano la IP de conexión mediante GeoIP (`https://api.country.is/`). Si el usuario se conecta desde un país hispanohablante soportado (ej. Perú, México, Argentina, España, etc.), la aplicación preselecciona automáticamente dicho país. Si el usuario elige manualmente otro país, su preferencia personal se respeta en todo momento.
- Al estar activo el botón "Países", se despliega una segunda fila con chips interactivos estilizados con la **bandera oficial** y el nombre de cada país (🇵🇪 Perú, 🇦🇷 Argentina, 🇨🇱 Chile, 🇨🇴 Colombia, 🇪🇸 España, 🇲🇽 México, 🇧🇴 Bolivia, 🇨🇷 Costa Rica, 🇨🇺 Cuba, 🇩🇴 Rep. Dominicana, 🇪🇨 Ecuador, 🇬🇹 Guatemala, 🇭🇳 Honduras, 🇳🇮 Nicaragua, 🇵🇦 Panamá, 🇵🇷 Puerto Rico, 🇵🇾 Paraguay, 🇸🇻 El Salvador, 🇺🇾 Uruguay, 🇻🇪 Venezuela).
- **Sincronización Total de Posición y Foco:** Al acceder a "Países", la lista horizontal se desplaza automáticamente (`countryRowState.animateScrollToItem`) para centrar el país seleccionado en la pantalla y el cursor de foco se ubica directamente sobre dicho botón. Esto garantiza que el botón del país activo siempre sea visible y esté en perfecta concordancia con los canales de la grilla, permitiendo explorar otros países con las flechas izquierda/derecha antes de bajar a los canales.

### Atajo Rápido para Volver a las Pestañas (Subir sin Esfuerzo)
- **Tecla 'Atrás' (Back) del Control Remoto:** Al haber bajado varios niveles o filas en la cuadrícula de canales, presionar una sola vez la tecla **Atrás (Back)** del mando a distancia devuelve la vista al inicio de la grilla de inmediato (`gridState.animateScrollToItem(0)`) y sitúa el cursor de enfoque directamente sobre la pestaña o filtro actual, evitando tener que pulsar la flecha Arriba docenas de veces.
- **Botón Visual `⬆ Subir a Pestañas [Atrás]`:** Cuando el usuario se desplaza hacia abajo en la lista, aparece un chip interactivo visible en la barra de estado superior. Puede ser seleccionado y pulsado con el control para regresar instantáneamente a las pestañas.

### Re-escaneo Inteligente
- Si se presiona nuevamente el botón de la categoría o país actualmente seleccionado, aparecerá un diálogo de confirmación para **volver a escanear** los canales en vivo desde el servidor, renovando la base de datos local Room.

### Cinta de Noticias RSS (Marquee)
- Justo debajo de los botones/filtros y arriba de la grilla, una marquesina continua (`basicMarquee`) muestra los titulares y actualizaciones del blog oficial en tiempo real.

---

## 3. Grilla de Canales (Channel Grid)
- **Distribución:** Es una cuadrícula (`TvLazyVerticalGrid`) organizada en **4 columnas** fijas.
- **Tarjetas de Canal (Cards):** Cada canal está representado por una tarjeta rectangular (proporción 16:11).
  - *Fondo:* Gris oscuro (`#1E293B`).
  - *Logo:* Muestra el logotipo del canal centrado. Utiliza el motor Coil con soporte SVG (`coil-svg`) y cabeceras de navegador de escritorio para evitar errores de red 403. Durante la descarga inicial muestra un indicador sutil; si el canal carece de logo o el enlace está roto (404), muestra un emoji representativo (`📺`) como fallback.
  - *Nombre:* El nombre del canal aparece centrado abajo del logo, truncado a un máximo de 2 líneas.
- **Indicador de Favorito:** En la esquina superior derecha de cada tarjeta hay un botón circular con un corazón. Para marcar o desmarcar un canal como favorito, se puede hacer clic directamente sobre el corazón o **mantener presionado el botón OK** (D-Pad Center) del control remoto (Long Press). Aparecerá una notificación animada en la pantalla confirmando la acción (❤️ o 🤍).
- **Efectos de Foco (Hover):** Cuando se navega con el control remoto sobre una tarjeta:
  - La tarjeta aumenta ligeramente su tamaño (`focusedScale = 1.15f`).
  - Se ilumina con un borde blanco de 4dp de grosor.
  - El fondo cambia a un gris más claro (`#334155`).
- **Estado Vacío Intuitivo:** Si la categoría o la pestaña Favoritos no tiene canales, muestra un diseño explicativo limpio indicando cómo agregar canales preferidos mediante el ícono de corazón.
- **Orden de Canales:** Los canales se muestran ordenados alfabéticamente dentro de la categoría o país activo, garantizando una disposición uniforme.
- **Preservación de Foco:** Al regresar a la grilla tras reproducir un canal, la vista recuerda su estado y restaura el foco en el último canal seleccionado gracias a `focusRestorer()`.

---

## 4. Ventana Modal "Importante" (3 Códigos QR Simétricos y Soporte)
Al presionar el botón **⭐ Importante**, se despliega una ventana modal interactiva con estética profunda y estilizada:
- **Tono Oscuro OLED Premium:** Fondo obsidian/slate (`#090D16`) con tarjetas interiores (`#111827`) y delicados bordes de 1dp que maximizan el contraste y la elegancia en televisores.
- **Visualización Completa Sin Recortes:** Códigos QR simétricos redimensionados a `118.dp x 118.dp` con espaciados y tipografía optimizada, garantizando que el 100% de los textos, descripciones y direcciones web (`paypal.com`, `Yape (Perú)`, `www.qwertyaserty.com`) sean perfectamente legibles sin recortes verticales.
- **Mensaje de Agradecimiento y Gratuidad:** En la cabecera destaca un aviso especial: *"❤️ ¡Muchas gracias por sus donaciones! Esta app es 100% gratuita y de libre acceso."*
- **Simetría Visual:** Tres columnas idénticas y perfectamente equilibradas (`weight = 1f`), cada una con un contenedor blanco de bordes redondeados y códigos QR empaquetados localmente:
  - **Columna 1 (💳 PayPal - Internacional):** Código QR local (`qr_paypal.png`) para realizar donaciones voluntarias con tarjeta o cuenta PayPal.
  - **Columna 2 (📱 Yape - Perú / BCP):** Código QR local (`qr_yape.jpg`) para realizar donaciones directas desde Perú mediante la aplicación Yape.
  - **Columna 3 (🌐 Web Oficial - Desarrollador):** Código QR (`qr_web.png`) para visitar el portal oficial del desarrollador (`www.qwertyaserty.com`), consultar novedades, reportes y contacto.
- **Botón "Cerrar" Más Fino:** Diseñado en formato cápsula/píldora (`RoundedCornerShape(20.dp)`), con trazo sutil, tipografía estilizada y enfoque automático inmediato (`FocusRequester`), permitiendo cerrar el diálogo con un solo toque de OK.
- **Aviso Legal y Exención de Responsabilidad:** En la parte inferior destaca un recuadro formal delimitado por un borde sutil: *"ℹ️ Aviso legal: Esta aplicación es un reproductor multimedia y no almacena, retransmite ni controla el contenido emitido por cada canal. Todos los enlaces provienen de fuentes públicas y abiertas en internet. El desarrollador no se responsabiliza por la programación, disponibilidad, derechos de autor o contenidos provistos por terceros."*

---

## 5. Indicador de Carga (Loading State)
- Mientras se descargan y validan los canales de una categoría o país por primera vez, o durante un re-escaneo, la grilla es reemplazada por una pantalla de carga.
- La barra de navegación de botones **nunca desaparece** durante la carga. El indicador de progreso se renderiza *debajo* de los botones, permitiendo cambiar de filtro en cualquier momento sin trabas de foco.
- Muestra un **Círculo de Progreso** (CircularProgressIndicator) blanco en el centro con el texto: *"Buscando canales de [Nombre de Filtro / País]..."*.

---

## 6. Reproductor de Video (ExoPlayerView) y Manejo Amigable de Errores
- **Pantalla Completa:** Al seleccionar un canal, el reproductor ocupa toda la pantalla con un fondo negro puro. La grilla queda "debajo" conservando su estado en memoria.
- **Manejo Estricto del Ciclo de Vida y Prevención de Fugas de Audio:** Cada reproductor `ExoPlayer` se instancia unívocamente por canal y se destruye de forma estricta (`stop()`, `clearMediaItems()`, `release()`) mediante `DisposableEffect` al cambiar de canal o cerrar el reproductor. Además, `MainActivity.onStop()` y `onDestroy()` limpian cualquier sesión activa, impidiendo por completo la mezcla de audios o que una señal siga sonando de fondo tras salir al menú del televisor.
- **Controles Nativos e Interacción:**
  - **Salir del Video:** Botón **Retroceder (Back / Return)** del control remoto.
  - **Alternar Favorito:** Presionar la tecla direccional **Arriba (D-Pad Up)** en el control remoto para añadir o quitar el canal de favoritos con feedback mediante Snackbar animado.
- **Prevención de Apagado:** Envía la bandera `FLAG_KEEP_SCREEN_ON` para evitar que el televisor active el salvapantallas o reposo durante la reproducción.
- **Pantalla Superpuesta para Canales No Disponibles (Friendly Error Overlay):**
  - Si un canal falla por geobloqueo, servidor caído o error de red, ya no deja la pantalla en negro.
  - Muestra una ventana superpuesta con el logo del canal, el nombre en grande, una etiqueta de advertencia (`⚠️ Señal Temporalmente No Disponible`) y un mensaje claro y comprensible explicando la causa (ej. mantenimiento, servidor inactivo o bloqueo geográfico).
  - Incluye dos botones enfocables para el control remoto:
    1. **🔄 Reintentar:** Vuelve a conectar con el servidor emisor de inmediato.
    2. **⬅️ Volver a Canales:** Regresa a la cuadrícula de canales sin perder el foco en la posición anterior.
- **Temporizador de Timeout (Anti-Buffering Infinito):** Si un stream queda congelado en estado de carga durante más de 14 segundos sin responder, el sistema activa automáticamente la pantalla amigable de canal no disponible.
- **Decodificador de Audio por Software (FFmpeg Integrado):** Incorpora el decodificador nativo de audio por software `media3-ffmpeg-decoder` (Jellyfin). Si el hardware del televisor o unos auriculares Bluetooth conectados no admiten pistas de audio envolventes como **AC3 (Dolby Digital), E-AC3 o DTS**, el motor conmuta automáticamente a FFmpeg para decodificar a estéreo en tiempo real, garantizando sonido en el 100% de las transmisiones.
- **Modo Pantalla Dividida (Dual View / 2 Canales en Simultáneo):**
  - En la esquina superior derecha del reproductor, el botón **🔀 Pantalla Dividida (Dual View)** despliega un carrusel interactivo en la parte inferior para seleccionar un segundo canal de la categoría actual o de favoritos.
  - Al elegirlo, la pantalla se divide simétricamente al 50% / 50% entre ambos canales en vivo.
  - Un indicador interactivo muestra cuál de los dos canales tiene el audio activo (`🔊` vs `🔇`). El usuario puede alternar el foco de audio pulsando sobre el panel o usando el botón de control en pantalla.
  - Pulsar la tecla **Atrás (Back)** en el control remoto cierra limpiamente el segundo canal y regresa a pantalla completa.
