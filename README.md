# Chari-IPTV - Aplicación Nativa para Android TV 📺 (v1.2)

**Chari-IPTV** es una aplicación nativa de última generación diseñada específicamente para dispositivos con Android TV (Smart TVs, TV Boxes, Fire TV Sticks, Google TV). Desarrollada con **Jetpack Compose para TV (androidx.tv.material3)** y el sistema de diseño **Obsidian Cinema TV**, brinda una experiencia inmersiva, cinematográfica, fluida y 100% autónoma en la sala de estar.

> 📖 **Nota de Interfaz:** Para una descripción detallada de todos los elementos visuales, comportamiento y flujos de navegación de la aplicación, consulta la [Documentación de Interfaz de Usuario (INTERFAZ_USUARIO.md)](./INTERFAZ_USUARIO.md).

---

## 🚀 Novedades de la Versión 1.2

*   **Catálogo Completo y Cronológico `Studio Ghibli` (25 Películas Oficiales):** La sección cinematográfica **`Studio Ghibli`** integra la filmografía oficial completa de 25 películas en riguroso orden cronológico histórico (1984–2023) desde *Nausicaä del Valle del Viento* hasta *El chico y la garza*.
    *   *Streams Híbridos (BitChute + Respaldo Archive.org):* Incorporación de streams de BitChute como fuente principal y enlaces de Internet Archive como respaldo automático en `alternateSources`.
    *   *Sistema Inteligente de Reanudación de Reproducción:* Exclusivo para Studio Ghibli (`GhibliPlaybackStore`), si el usuario sale de la película o de la app y regresa más tarde, la aplicación le pregunta mediante un diálogo modal enfocado para TV si desea **Continuar donde lo dejó** (con indicación de tiempo transcurrido) o **Comenzar de nuevo** desde 0:00.
    *   *Sin Botón Escanear:* Se retiró el botón `[🔄 Escanear]` en este apartado por tratarse de un catálogo cinematográfico completo y curado.
    *   *Sin Función de Favoritos:* Se suprimieron las estrellas en las tarjetas y la opción de favoritos en esta sección; la barra inferior `TvFooterBar` oculta automáticamente la leyenda `[MANTENER OK] Favorito` al ingresar a Studio Ghibli.
    *   *FastStart & Watchdog Adaptativo (35s):* Optimización de streaming continuo y conmutación automática de fuentes espejo ante demoras de red.
*   **Tarjetas Cinematográficas Adaptativas de 2 Columnas con Sinopsis:** Al ingresar a la sección `Studio Ghibli`, la cuadrícula se transforma automáticamente de 4 columnas a **2 columnas** (`GridCells.Fixed(2)`), desplegando tarjetas de cine de gran formato (180dp de altura) con póster vertical en proporción 2:3, insignia `🎬 PELÍCULA`, año de lanzamiento, duración, director, sinopsis argumental completa en español curada localmente (`GhibliData`) y atajo de reproducción D-Pad con audio latino.
*   **Ventana Modal "Importante" Refinada & Aviso Legal Profundizado:**
    *   *Cabecera Limpia:* Remoción de títulos repetitivos y emojis distractores; presentación mediante banner directo de agradecimiento y donaciones voluntarias (PayPal y Yape).
    *   *Aviso Legal y Derechos de Autor:* Profundización integral estipulando la naturaleza de software cliente multimedia sin servidores de retransmisión propios, indexación de repositorios públicos de internet (*iptv-org*, *TDTChannels*), enlaces a archivos de preservación histórica en Internet Archive (*archive.org*) y titularidad de derechos de *Studio Ghibli Inc.* y cadenas de televisión.
    *   *Cierre Nativo con Control Remoto:* Eliminación del botón gráfico `[Cerrar]`; el diálogo se descarta de forma intuitiva presionando la tecla física **ATRÁS** del mando a distancia.
*   **Mitigación de Errores HTTP 403 Forbidden & Purga de Servidores Astra:**
    *   Análisis técnico y filtrado preventivo en `Constants.kt` de servidores restreamers de puerto 8000 caídos o con tokens privados vencidos (`38.187.7.252`, `187.102.208.209`), eliminando la causa raíz de bloqueos recurrentes en transmisiones deportivas de pago cerradas (*DSports*).
    *   Failover automático hacia micro-proxy loopback con cabeceras `Referer`/`Origin` y conmutación silenciosa a fuentes espejo alternativas.
*   **Reconexión Automática ante Señales Inestables (Mínimo 3 Intentos):** Ante microcortes de red, pérdidas de paquetes o caídas temporales del servidor emisor, la app ya no interrumpe al usuario ni despliega el diálogo de desconexión de inmediato; ejecuta un ciclo de **al menos 3 reintentos automáticos continuos** antes de declarar la señal como inaccesible, informando en pantalla: *"Reconectando señal (intento X de 3)..."*..
*   **Purga de Canales Residuales y Migración Room v13:** Implementación de `MIGRATION_12_13` (`DELETE FROM cached_channels`) en `AppDatabase.kt` para eliminar canales residuales o enlaces inactivos de repositorios externos en Perú y demás países, manteniendo al 100% los favoritos del usuario e incorporando filtros estrictos contra dominios VOD en listas de televisión en vivo.
*   **Arquitectura de Red Dual (Aislamiento Seguro & Media):** Separación estricta de clientes OkHttp mediante calificadores de inyección Dagger Hilt (`@SecureClient` y `@MediaClient`). Las consultas a GitHub M3U, GeoIP y el blog de noticias validan al 100% los certificados SSL/TLS y el hostname del sistema, mientras que el cliente permisivo se aísla exclusivamente a la reproducción de flujos IPTV en ExoPlayer y el proxy local.
*   **Blindaje Anti-SSRF en el Proxy Local:** `LocalHlsProxyServer` analiza cada URL objetivo rechazando de forma inmediata cualquier intento de conexión hacia subredes locales privadas (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), interfaces de administración del router o direcciones loopback (`127.0.0.1`, `localhost`).
*   **Pre-calentamiento Especulativo DNS (Zapping Instantáneo):** Al posicionar el foco del control remoto sobre cualquier tarjeta de la cuadrícula, el repositorio realiza la resolución DNS anticipada del servidor emisor a través de la pila DoH. Al presionar OK, el socket ya está caliente, reduciendo el tiempo de arranque de video (TTFF) drásticamente.
*   **Adaptabilidad Dinámica de Bitrate en Wi-Fi:** `PlayerBuilder` incorpora reglas adaptativas multi-códec (`allowVideoMixedMimeTypeAdaptiveness`, `allowVideoNonSeamlessAdaptiveness`) para permitir descensos de resolución fluidos ante caídas momentáneas del ancho de banda en conexiones inalámbricas inestables.
*   **Blindaje de Copias de Seguridad:** Desactivación de `allowBackup` en el manifiesto para impedir extracciones de datos no autorizadas por depuración ADB.
*   **Identidad de Marca Oficial (`Chari-IPTV`):** Homologación del nombre oficial `Chari-IPTV` en toda la interfaz, footer dinámico (`Chari-IPTV v1.2`), metadatos del sistema y Android TV Launcher. Remoción del badge `EN VIVO` en la barra superior para un diseño despejado y sofisticado.
*   **Enfoque de Alto Contraste en Botón "Importante":** Adopción del marco de foco blanco perimétrico de 2.5dp (`Color.White`), aumento de escala a 1.08f y fondo ámbar sólido vibrante (`#F59E0B`) con tipografía de alto contraste cuando el control remoto se sitúa sobre él, garantizando que se note con la misma claridad que el resto de botones interactivos.
*   **Auto-Ocultado Inteligente del Cajón Lateral (4 Segundos):** El menú lateral de zapping (*Quick Channel Drawer*) se oculta automáticamente tras 4 segundos de inactividad, evitando obstruir la reproducción si el usuario no realiza ninguna acción. Cualquier pulsación del D-Pad o desplazamiento en la lista reinicia el temporizador.
*   **Reproducción Direct-First con Failover Inteligente a Proxy:** ExoPlayer intenta reproducir de forma directa y nativa por defecto. Solo si el servidor emisor devuelve un error HTTP 403 Forbidden o HTTP 451 (geobloqueo), conmuta dinámicamente al micro-proxy loopback local (`127.0.0.1:8899`) inyectando cabeceras de emulación (`Referer`, `Origin`, `User-Agent`). Esto reduce la latencia de arranque al mínimo absoluto en más del 85% de las señales.
*   **Soporte de Fuentes Espejo (Multi-Mirror Failover):** Agrupación inteligente de streams duplicados o alternativos para un mismo canal (`Fuente X/Y`). Si un stream primario sufre caídas de señal, el sistema permite conmutar o realizar failover automático hacia el espejo funcional.
*   **Aislamiento de Ciclo de Vida en ExoPlayer:** Instanciación limpia y dedicada por URL (`remember(activeStreamUrl, retryCount)`). Previene por completo la contaminación de decodificadores, formatos de audio y búferes al alternar entre emisiones con diferentes codecs o resoluciones.
*   **Prevención de Bloqueos (Anti-ANR) y Desbloqueo D-Pad en Diálogos de Error:**
    *   `activity.playerKeyHandler` en `MainActivity` cede el control del botón central `OK / DPAD_CENTER` cuando el reproductor entra en estado de error, permitiendo navegar y pulsar de inmediato los botones **"Reintentar"** y **"Volver a Canales"**.
    *   Liberación asíncrona de recursos (`exoPlayer.release()`) en un hilo en segundo plano al desmontar la vista o volver a la grilla, eliminando el congelamiento del hilo principal de UI (*Main UI Thread Freeze / ANR*).
    *   Detención inmediata del pipeline de medios y vaciado de items ante timeout o caída de señal.
*   **Depuración de Catálogo & Filtrado GEO Quirúrgico:**
    *   Purga de servidores permanentemente bloqueados (ej. `barinaslive1.boldmss.com`, eliminando la causa raíz de fallos recurrentes en deportes).
    *   Exclusión limpia de variantes geobloqueadas de TDTChannels mediante palabra exacta `\bgeo\b` (`Esport3 GEO`, `Teledeporte GEO`), protegiendo al 100% canales legítimos de documentales (*National Geographic*, *Nat Geo Wild*).
*   **Optimización R8 Extrema para Producción (-65% de Peso):**
    *   Reducción del tamaño final del instalador de **24.1 MB a 8.44 MB** en APK Release mediante minificación de código R8, encogimiento agresivo de recursos no utilizados (`shrinkResources`), stripping de registros de depuración (`-assumenosideeffects`) y reglas de ProGuard personalizadas.
    *   Soporte completo para Android App Bundle (`AAB`, 8.53 MB) optimizado para publicación en Google Play Console con Baseline Profiles.

---

## 🚀 Novedades de la Versión 1.1

*   **Zapping Lateral en Reproducción (*Quick Channel Drawer*):** Al estar reproduciendo cualquier canal a pantalla completa, basta con presionar **D-Pad Izquierda (◀)** en el control remoto para desplegar una lista lateral semitransparente con todos los canales disponibles del mismo apartado (categoría o país activo). Permite cambiar de canal de inmediato (*in-place*) sin salir del reproductor ni cortar la inmersión del usuario.
*   **Intercepción Nativa de Teclas (`dispatchKeyEvent`):** Arquitectura de captura de eventos a nivel de ventana en `MainActivity` que garantiza respuesta inmediata al 100% de las pulsaciones del control remoto, evitando que el `SurfaceView` de ExoPlayer bloquee o absorba las teclas direccionales.
*   **Identificador de Versión Fijo en Pantalla:** La versión oficial instalada se muestra de forma permanente y nítida en la esquina inferior derecha del menú principal y del reproductor (`Chari-IPTV v1.2`).
*   **Estética "Obsidian Cinema TV":** Fondos OLED ultraoscuros (`#090D16`), tarjetas Slate elevadas (`#1E293B`) y acentos de foco en azul eléctrico brillante (`#2563EB`) y ámbar VIP (`#F59E0B`).
*   **100% Libre de Cuentas de Usuario y Datos Falsos:** No requiere inicio de sesión, ni perfiles, ni sincronización externa; toda la información mostrada es verídica (reloj del sistema en tiempo real `HH:mm`, estado real de proxy anti-geobloqueo y catálogo real sin guías EPG simuladas).

---

## 🌟 Características Principales

*   **100% Autónoma:** Chari-IPTV no requiere servidores backend locales ni middlewares. Se conecta directamente a internet, descargando, parseando y validando las listas oficiales de canales IPTV en tiempo real.
*   **Zapping Rápido sin Salir del Video:** Despliegue con **D-Pad Izquierda (◀)** de la lista vertical de canales de la sección en curso. Permite cambiar de canal con **OK** o cerrar el menú con **D-Pad Derecha (▶)** o **Atrás**.
*   **Integración con Pantalla de Inicio de Android TV (Leanback Home Channels):** Utiliza `androidx.tvprovider` para publicar una fila interactiva nativa (*"Chari-IPTV: Canales Destacados"*) en el launcher de Android TV / Google TV y sincronizar la fila del sistema *"Continuar viendo"* (Watch Next). Al seleccionar cualquier tarjeta en la pantalla principal del televisor, la app se abre y reproduce directamente ese canal.
*   **Exclusividad 100% Habla Hispana y Ecosistema Multi-Fuente Multi-Proveedor:** Integra un motor multi-proveedor (`IptvProvider`) extensible con fuentes legales en español:
    *   **TDTChannels:** Señales oficiales de España en alta definición (La 1, Antena 3, Telecinco, etc.).
    *   **Pluto TV España:** Canales temáticos y de entretenimiento continuo 24/7.
    *   **IPTV-Org:** Canales abiertos de toda Hispanoamérica con filtrado estricto anti-extranjero.
    *   **Studio Ghibli Cine On-Demand (VOD):** Catálogo multimedia dedicado con los 25 largometrajes completos de Studio Ghibli en español latino (*Nausicaä del Valle del Viento*, *El viaje de Chihiro*, *Mi vecino Totoro*, *El increíble castillo vagabundo*, *La princesa Mononoke*, *El castillo en el cielo*, *El chico y la garza*, etc.) con sinopsis en español integrada, fuentes primarias BitChute y respaldo en Internet Archive con enlaces de alta estabilidad.
*   **Reproductor Híbrido con Barra de Tiempo (SeekBar) para VOD:** `ExoPlayerView` detecta automáticamente si el medio en reproducción es un archivo de video estático (`.mp4`, `.mkv`, o categoría `ghibli`) o una transmisión en vivo. En contenido VOD, el reproductor despliega una barra de tiempo interactiva con la posición actual, duración total formateada (`HH:mm:ss`), estado de pausa/reproducción y soporte de salto temporal:
    *   `[OK]` / `[ENTER]`: Alternar Pausa y Reanudar video.
    *   `[◀] / [▶]`: Rebobinar o adelantar ±15 segundos al instante.
    *   `[▼]`: Desplegar el catálogo lateral de capítulos y películas.
    *   Badge adaptativo: Conmuta dinámicamente de `DIRECTO` a `CINE VOD`.
    *   **Reconexión Automática Resiliente:** En transmisiones en directo ante inestabilidad o caídas de paquetes, ejecuta de inmediato un ciclo de **al menos 3 reintentos continuos** con feedback visual en pantalla antes de activar la ventana de desconexión.
*   **Priorización Inteligente de Emisiones 24/7:** El motor de repositorio organiza y clasifica los canales situando al frente de la cuadrícula las transmisiones continuas 24/7 activas, evitando que canales comunitarios intermitentes o temporales `[Not 24/7]` entorpezcan la navegación del usuario.
*   **Activación Selectiva de Proxy Anti-Geobloqueo (HTTP 403 / 451):** Servidor HTTP embebido en loopback local (`LocalHlsProxyServer`). Todas las transmisiones operan de manera directa y nativa por defecto; el proxy loopback solo y exclusivamente interviene si el servidor emisor rechaza la conexión con código HTTP 403 (Forbidden) o 451 (Geoblock), reescribiendo cabeceras (`Referer`, `Origin`, `User-Agent`) y manifiestos `.m3u8` en tiempo real.
*   **DNS-over-HTTPS (DoH) Anti-Censura:** Pila de red OkHttp con resolución DNS cifrada (`1.1.1.1` de Cloudflare y Google DNS) con fallback resiliente. Bypasea censura y bloqueos a nivel DNS aplicados por operadores residenciales durante eventos en vivo sin requerir una VPN externa.
*   **Filtros por Categoría & Género con Filtrado Riguroso:** Botones interactivos para acceder directamente a **Deportes**, **Noticias**, **Películas**, **Series**, **Infantil** y **Música**. La categoría Series y Películas excluye con precisión emisiones generalistas, radios, noticias y religión.
*   **Sub-selector de Países Dinámico con Detección GeoIP:** Al presionar "Países", se despliega una sub-fila con banderas oficiales y nombres (Perú, México, España, Argentina, Colombia, etc.). La aplicación detecta la región del usuario por GeoIP y preselecciona su país local de forma no intrusiva.
*   **Diseño Visual de Tarjetas con Alto Contraste de Foco:** Tarjetas de canal proporción 16:11 en tono slate oscuro (`#1E293B`) que al recibir el foco del control remoto conmutan su fondo de forma nítida a azul brillante (`#2563EB`) con marco blanco de 3.5dp y escala aumentada, permitiendo identificar la posición de forma inmediata.
*   **Logos de Canales & Fallback TV:** Carga asíncrona optimizada con Coil y soporte SVG. Si el canal no dispone de logotipo público, se renderiza el isotipo oficial del televisor (`default_tv.png`).
*   **Sistema de Favoritos con Estrellas (⭐):** Gestión ágil de favoritos persistida en Room Database con estrellas (`⭐` / `☆`). Permite marcar o desmarcar canales al instante manteniendo presionado el botón OK (Long Press) en la cuadrícula o con la tecla direccional **Arriba (▲)** durante la reproducción en vivo.
*   **Navegación Rápida con Control Remoto (D-Pad):**
    *   Presionar la tecla **Atrás (Back)** devuelve la vista inmediatamente al inicio de la cuadrícula (`gridState.scrollToItem(0)`) y sitúa el enfoque sobre las pestañas superiores.
    *   Foco automático inicial sobre la pestaña *Favoritos* o la última categoría visitada.
*   **Ventana Modal "Importante" (3 Códigos QR Simétricos):** Ubicada en la barra principal, con diseño panorámico y tres tarjetas simétricas para donaciones internacionales vía **PayPal**, donaciones en Perú mediante **Yape**, y visita a la web oficial del desarrollador (**www.qwertyaserty.com**).
*   **Cinta de Noticias RSS Integrada:** Marquesina de noticias deslizante continua (`basicMarquee`) con las publicaciones del blog oficial.
*   **Motor de Video ExoPlayer & Audio FFmpeg:** Integra `Media3 ExoPlayer 1.9.0` con decodificador de audio por software `media3-ffmpeg-decoder` (soporte completo de audio AC3, E-AC3 y DTS), gestión de transmisiones HLS y MPEG-DASH, política inteligente de reintentos (`IptvLoadErrorPolicy`), prevención de reposo (`FLAG_KEEP_SCREEN_ON`) y ciclo de vida seguro.

---

## 🛠️ Stack Tecnológico

*   **Identificador de Aplicación (Package):** `com.qwertyaserty.chariiptv`
*   **Versión de la App:** 1.2 (VersionCode 3)
*   **Lenguaje:** Kotlin 2.0+ (JVM Toolchain 17 / Compatible JDK 21)
*   **Target SDK:** Android SDK 36 (Android 16), Min SDK 24 (Android 7.0+)
*   **UI Toolkit:** Jetpack Compose for TV (`androidx.tv:tv-foundation:1.0.0-alpha11`, `androidx.tv:tv-material:1.0.0`) y Material 3
*   **Compatibilidad:** Android TV / Google TV / Fire TV / AOSP TV Box (`android.software.leanback = false`)
*   **Android TV Home Provider:** `androidx.tvprovider:tvprovider:1.0.0` (Preview Channels, Preview Programs, Watch Next)
*   **Reproductor Multimedia:** AndroidX Media3 ExoPlayer 1.9.0 (`OkHttpDataSource`, HLS `.m3u8`, MPEG-DASH `.mpd`, Jellyfin FFmpeg Audio Decoder, IptvLoadErrorPolicy)
*   **Cliente HTTP & Red:** OkHttp 4.12.0 con **DNS-over-HTTPS (DoH)** y Micro-Proxy HLS en Loopback (`127.0.0.1:8899`)
*   **Inyección de Dependencias:** Dagger Hilt 2.51.1
*   **Asincronía & Reactividad:** Kotlin Coroutines (`async` / `awaitAll`), Dispatchers (`IO` / `Main`), StateFlow
*   **Carga de Imágenes:** Coil Compose 2.6.0 + Coil SVG 2.6.0 (`SubcomposeAsyncImage`, `ImageLoaderFactory`, DiskCache 100MB)
*   **Persistencia Local:** Room Database 2.6.1 (Caché de Canales v13 con migración de purga `MIGRATION_12_13` y Favoritos autónomos con fuentes espejo)
*   **Minificación y Seguridad:** ProGuard / R8 con reglas optimizadas y supresión de llamadas de logs en Release
*   **Build System:** Gradle (Kotlin DSL con Version Catalog `libs.versions.toml`)

---

## 📂 Arquitectura del Proyecto

1.  **Descarga Curada en Español:** El repositorio consulta la lista oficial `languages/spa.m3u` junto con TDTChannels y Pluto TV, asegurando únicamente contenido en español.
2.  **Selección de Filtro o País:** Cuando el usuario selecciona una categoría o país, el estado limpia inmediatamente la vista anterior y muestra el spinner de carga.
3.  **Filtrado & Priorización:** Se excluyen señales geobloqueadas (`\bgeo\b`), servidores muertos (`barinaslive1.boldmss.com`), transmisiones no hispanas y cadenas panregionales satelitales repetitivas; las transmisiones 24/7 se sitúan al inicio de la lista.
4.  **Almacenamiento en Caché (Room):** Los canales activos se persisten en Room bajo su respectivo identificador de filtro o país, permitiendo carga instantánea en visitas posteriores.
5.  **Recomendaciones en Android TV:** Al reproducir canales o modificar favoritos, `TvRecommendationManager` actualiza la fila de inicio y *"Continuar viendo"* en el launcher del televisor.
6.  **Reproducción Direct-First con Failover & Zapping Lateral:** Al pulsar sobre un canal, se inicia `ExoPlayerView` de forma directa a pantalla completa. Ante HTTP 403/451 conmuta al proxy local con cabeceras. Desde la reproducción, presionar **D-Pad Izquierda (◀)** despliega el `QuickChannelDrawer` para saltar a otros canales del mismo apartado sin salir de la emisión.

---

## ⚙️ Cómo Compilar e Instalar

### Método 1: Compilación Release Optimizada (Recomendado para Producción)
1. Abre tu terminal en la raíz del proyecto:
   ```powershell
   cd C:\xampp\htdocs\Chari-IPTV_TV
   ```
2. Configura JAVA_HOME si es necesario:
   ```powershell
   $env:JAVA_HOME = "C:\Users\QwertyAserty\.jdks\temurin-21.0.13"
   ```
3. Compila el APK optimizado con R8 (~8.48 MB):
   ```powershell
   .\gradlew.bat assembleRelease
   ```
   *Ruta del binario generado:* `app\build\outputs\apk\release\Chari-IPTV_1.2.apk`

4. Para compilar el Android App Bundle para Google Play Store (8.53 MB):
   ```powershell
   .\gradlew.bat bundleRelease
   ```
   *Ruta del binario generado:* `app\build\outputs\bundle\release\app-release.aab`

### Método 2: Compilación Debug (Desarrollo)
```powershell
.\gradlew.bat assembleDebug
```
*Ruta:* `app\build\outputs\apk\debug\app-debug.apk`

### Instalación en el Smart TV o TV Box vía ADB
Si tienes activada la depuración USB / de red en tu televisor o TV Box (Android TV / Google TV):
```powershell
# Conectar por IP en red local (si es por Wi-Fi)
adb connect 192.168.1.X:5555

# Instalar o actualizar el APK Release
adb install -r "app\build\outputs\apk\release\Chari-IPTV_1.2.apk"
```

### Instalación Alternativa (Sin ADB)
*   **Memoria USB (Pendrive):** Copia el archivo `Chari-IPTV_1.2.apk` a un pendrive, conéctalo a la TV y ábrelo con un explorador de archivos (*File Commander*, *X-plore*).
*   **Envío por Red Local (Wi-Fi):** Utiliza la aplicación **"Send Files to TV"** o **LocalSend** para transferir el archivo `Chari-IPTV_1.2.apk` directamente desde tu PC o teléfono a tu Android TV.
