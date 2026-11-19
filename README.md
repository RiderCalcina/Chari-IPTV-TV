# Chari-IPTV TV - Aplicación Nativa para Android TV 📺 (v1.2)

**Chari-IPTV TV** es una aplicación nativa de última generación diseñada específicamente para dispositivos con Android TV (Smart TVs, TV Boxes, Fire TV Sticks, Google TV). Desarrollada con **Jetpack Compose para TV (androidx.tv.material3)** y el sistema de diseño **Obsidian Cinema TV**, brinda una experiencia inmersiva, cinematográfica, fluida y 100% autónoma en la sala de estar.

> 📖 **Nota de Interfaz:** Para una descripción detallada de todos los elementos visuales, comportamiento y flujos de navegación de la aplicación, consulta la [Documentación de Interfaz de Usuario (INTERFAZ_USUARIO.md)](./INTERFAZ_USUARIO.md).

---

## 🚀 Novedades de la Versión 1.2

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
*   **Identificador de Versión Fijo en Pantalla:** La versión oficial instalada se muestra de forma permanente y nítida en la esquina inferior derecha del menú principal y del reproductor (`Chari-IPTV TV v1.2`).
*   **Estética "Obsidian Cinema TV":** Fondos OLED ultraoscuros (`#090D16`), tarjetas Slate elevadas (`#1E293B`) y acentos de foco en azul eléctrico brillante (`#2563EB`) y ámbar VIP (`#F59E0B`).
*   **100% Libre de Cuentas de Usuario y Datos Falsos:** No requiere inicio de sesión, ni perfiles, ni sincronización externa; toda la información mostrada es verídica (reloj del sistema en tiempo real `HH:mm`, estado real de proxy anti-geobloqueo y catálogo real sin guías EPG simuladas).

---

## 🌟 Características Principales

*   **100% Autónoma:** Chari-IPTV TV no requiere servidores backend locales ni middlewares. Se conecta directamente a internet, descargando, parseando y validando las listas oficiales de canales IPTV en tiempo real.
*   **Zapping Rápido sin Salir del Video:** Despliegue con **D-Pad Izquierda (◀)** de la lista vertical de canales de la sección en curso. Permite cambiar de canal con **OK** o cerrar el menú con **D-Pad Derecha (▶)** o **Atrás**.
*   **Integración con Pantalla de Inicio de Android TV (Leanback Home Channels):** Utiliza `androidx.tvprovider` para publicar una fila interactiva nativa (*"Chari-IPTV: Canales Destacados"*) en el launcher de Android TV / Google TV y sincronizar la fila del sistema *"Continuar viendo"* (Watch Next). Al seleccionar cualquier tarjeta en la pantalla principal del televisor, la app se abre y reproduce directamente ese canal.
*   **Exclusividad 100% Habla Hispana y Ecosistema Multi-Fuente:** Integra un motor multi-proveedor (`IptvProvider`) con tres fuentes legales en español: **TDTChannels** (señales oficiales de España en alta definición), **Pluto TV España** (canales temáticos 24/7) e **IPTV-Org** (canales de toda Hispanoamérica), garantizando diversidad y failover automático ante caídas de listas.
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

*   **Versión de la App:** 1.2 (VersionCode 3)
*   **Lenguaje:** Kotlin 2.0+ (JVM Toolchain 17 / Compatible JDK 21)
*   **Target SDK:** Android SDK 36 (Android 16), Min SDK 24 (Android 7.0+)
*   **UI Toolkit:** Jetpack Compose for TV (`androidx.tv:tv-foundation:1.0.0-alpha11`, `androidx.tv:tv-material:1.0.0`) y Material 3
*   **Android TV Home Provider:** `androidx.tvprovider:tvprovider:1.0.0` (Preview Channels, Preview Programs, Watch Next)
*   **Reproductor Multimedia:** AndroidX Media3 ExoPlayer 1.9.0 (`OkHttpDataSource`, HLS `.m3u8`, MPEG-DASH `.mpd`, Jellyfin FFmpeg Audio Decoder, IptvLoadErrorPolicy)
*   **Cliente HTTP & Red:** OkHttp 4.12.0 con **DNS-over-HTTPS (DoH)** y Micro-Proxy HLS en Loopback (`127.0.0.1:8899`)
*   **Inyección de Dependencias:** Dagger Hilt 2.51.1
*   **Asincronía & Reactividad:** Kotlin Coroutines (`async` / `awaitAll`), Dispatchers (`IO` / `Main`), StateFlow
*   **Carga de Imágenes:** Coil Compose 2.6.0 + Coil SVG 2.6.0 (`SubcomposeAsyncImage`, `ImageLoaderFactory`, DiskCache 100MB)
*   **Persistencia Local:** Room Database 2.6.1 (Caché de Canales v5 y Favoritos autónomos con clave compuesta)
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
3. Compila el APK optimizado con R8 (8.44 MB):
   ```powershell
   .\gradlew.bat assembleRelease
   ```
   *Ruta del binario generado:* `app\build\outputs\apk\release\app-release.apk`

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
# Conectar por IP en red local
adb connect 192.168.1.X:5555

# Instalar o actualizar el APK Release
adb install -r app\build\outputs\apk\release\app-release.apk
```

### Instalación Alternativa (Sin ADB)
*   **Memoria USB (Pendrive):** Copia el archivo `app-release.apk` a un pendrive, conéctalo a la TV y ábrelo con un explorador de archivos (*File Commander*, *X-plore*).
*   **Envío por Red Local (Wi-Fi):** Utiliza la aplicación **"Send Files to TV"** para transferir el APK directamente desde tu PC o teléfono a tu Android TV.
