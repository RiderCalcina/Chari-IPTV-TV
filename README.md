# Chari-IPTV TV - Aplicación Nativa para Android TV 📺

**Chari-IPTV TV** es una aplicación nativa de última generación diseñada específicamente para dispositivos con Android TV (Smart TVs, TV Boxes, Fire TV Sticks, Google TV). Reconstruida desde cero con **Jetpack Compose para TV (androidx.tv.material3)**, brinda una experiencia inmersiva, fluida y 100% autónoma en la sala de estar.

> 📖 **Nota de Interfaz:** Para una descripción detallada de todos los elementos visuales, comportamiento y flujos de navegación de la aplicación, consulta la [Documentación de Interfaz de Usuario (INTERFAZ_USUARIO.md)](./INTERFAZ_USUARIO.md).

---

## 🚀 Características Principales

*   **100% Autónoma:** Chari-IPTV TV no requiere servidores backend locales ni middlewares. Se conecta directamente a internet, descargando, parseando y validando las listas oficiales de canales IPTV en tiempo real.
*   **Integración con Pantalla de Inicio de Android TV (Leanback Home Channels):** Utiliza `androidx.tvprovider` para publicar una fila interactiva nativa (*"Chari-IPTV: Canales Destacados"*) en el launcher de Android TV / Google TV y sincronizar la fila del sistema *"Continuar viendo"* (Watch Next). Al seleccionar cualquier tarjeta en la pantalla principal del televisor, la app se abre y reproduce directamente ese canal.
*   **Exclusividad 100% Habla Hispana y Ecosistema Multi-Fuente:** Integra un motor multi-proveedor (`IptvProvider`) con tres fuentes legales en español: **TDTChannels** (señales oficiales de España en alta definición: La 1, La 2, Antena 3, Telecinco, etc.), **Pluto TV España** (canales temáticos 24/7) e **IPTV-Org** (canales de toda Hispanoamérica), garantizando diversidad y failover automático ante caídas de listas.
*   **Priorización Inteligente de Emisiones 24/7:** El motor de repositorio organiza y clasifica los canales situando al frente de la cuadrícula las transmisiones continuas 24/7 activas, evitando que canales comunitarios intermitentes o temporales `[Not 24/7]` entorpezcan la navegación del usuario.
*   **Activación Selectiva de Proxy Anti-Geobloqueo (HTTP 403 / 451):** Servidor HTTP embebido en loopback local (`LocalHlsProxyServer`). Todas las transmisiones operan de manera directa y nativa por defecto; el proxy loopback solo y exclusivamente interviene si el servidor emisor rechaza la conexión con código HTTP 403 (Forbidden) o 451 (Geoblock), reescribiendo cabeceras (`Referer`, `Origin`, `User-Agent`) y manifiestos `.m3u8` en tiempo real.
*   **DNS-over-HTTPS (DoH) Anti-Censura:** Pila de red OkHttp con resolución DNS cifrada (`1.1.1.1` de Cloudflare y Google DNS) con fallback resiliente. Bypasea censura y bloqueos a nivel DNS aplicados por operadores residenciales durante eventos en vivo sin requerir una VPN externa.
*   **Filtros por Categoría & Género con Filtrado Riguroso:** Botones interactivos para acceder directamente a **Deportes**, **Noticias**, **Películas**, **Series**, **Infantil** y **Música**. La categoría Series y Películas excluye con precisión emisiones generalistas, radios, noticias y religión.
*   **Sub-selector de Países Dinámico con Detección GeoIP:** Al presionar "Países", se despliega una sub-fila con banderas oficiales y nombres (Perú, México, España, Argentina, Colombia, Nicaragua, etc.). La aplicación detecta la región del usuario por GeoIP y preselecciona su país local de forma no intrusiva.
*   **Diseño Visual Renovado de Tarjetas:** Tarjetas de canal estilizadas en tono gris neutro / piedra claro (`#E5E7EB` y `#F3F4F6` al enfocar con el control remoto) con elevación perimétrica por sombras (`shadow 8dp`).
*   **Logos de Canales & Fallback TV:** Carga asíncrona optimizada con Coil y soporte SVG. Si el canal no dispone de logotipo público, se renderiza de forma limpia y exclusiva el isotipo oficial del televisor (`default_tv.png`).
*   **Sistema de Favoritos con Estrellas (⭐):** Gestión ágil de favoritos persistida en Room Database con estrellas (`⭐` / `☆`). Permite marcar o desmarcar canales al instante manteniendo presionado el botón OK (Long Press) o con un clic directo.
*   **Navegación Rápida con Control Remoto (D-Pad):**
    - Presionar la tecla **Atrás (Back)** devuelve la vista inmediatamente al inicio de la cuadrícula (`gridState.scrollToItem(0)`) y sitúa el enfoque sobre las pestañas superiores.
    - Foco automático inicial sobre la pestaña *Favoritos* o la última categoría visitada.
*   **Ventana Modal "Importante" (3 Códigos QR Simétricos):** Ubicada en la barra principal, con diseño panorámico y tres tarjetas simétricas para donaciones internacionales vía **PayPal**, donaciones en Perú mediante **Yape**, y visita a la web oficial del desarrollador (**www.qwertyaserty.com**).
*   **Cinta de Noticias RSS Integrada:** Marquesina de noticias deslizante continua (`basicMarquee`) con las publicaciones del blog oficial.
*   **Motor de Video ExoPlayer & Audio FFmpeg:** Integra `Media3 ExoPlayer 1.2.0` con decodificador de audio por software `media3-ffmpeg-decoder` (soporte completo de audio AC3, E-AC3 y DTS), gestión de transmisiones HLS y MPEG-DASH, política inteligente de reintentos (`IptvLoadErrorPolicy`), prevención de reposo (`FLAG_KEEP_SCREEN_ON`) y ciclo de vida seguro.

---

## 🛠️ Stack Tecnológico

*   **Lenguaje:** Kotlin 2.0+ (JVM Toolchain 17 / Compatible JDK 21)
*   **Target SDK:** Android SDK 36 (Android 16), Min SDK 24 (Android 7.0+)
*   **UI Toolkit:** Jetpack Compose for TV (`androidx.tv:tv-foundation:1.0.0-alpha11`, `androidx.tv:tv-material:1.0.0`) y Material 3
*   **Android TV Home Provider:** `androidx.tvprovider:tvprovider:1.0.0` (Preview Channels, Preview Programs, Watch Next)
*   **Reproductor Multimedia:** AndroidX Media3 ExoPlayer 1.2.0 (`OkHttpDataSource`, HLS `.m3u8`, MPEG-DASH `.mpd`, Jellyfin FFmpeg Audio Decoder, IptvLoadErrorPolicy)
*   **Cliente HTTP & Red:** Retrofit 2.11.0 + OkHttp 4.12.0 con **DNS-over-HTTPS (DoH)** y Micro-Proxy HLS en Loopback (127.0.0.1)
*   **Inyección de Dependencias:** Dagger Hilt 2.51.1
*   **Asincronía & Reactividad:** Kotlin Coroutines (`async` / `awaitAll`), Dispatchers (`IO` / `Main`), StateFlow
*   **Carga de Imágenes:** Coil Compose 2.6.0 + Coil SVG 2.6.0 (`SubcomposeAsyncImage`, `ImageLoaderFactory`, DiskCache 100MB)
*   **Persistencia Local:** Room Database 2.6.1 (Caché de Canales v5 y Favoritos autónomos con clave compuesta)
*   **Build System:** Gradle (Kotlin DSL con Version Catalog `libs.versions.toml`)

---

## 📂 Arquitectura del Proyecto

El flujo de trabajo es el siguiente:
1.  **Descarga Curada en Español:** El repositorio consulta la lista oficial `languages/spa.m3u` junto con TDTChannels y Pluto TV, asegurando únicamente contenido en español.
2.  **Selección de Filtro o País:** Cuando el usuario selecciona una categoría o país, el estado limpia inmediatamente la vista anterior y muestra el spinner de carga.
3.  **Filtrado & Priorización:** Se excluyen señales geobloqueadas, no hispanas y cadenas panregionales satelitales repetitivas; las transmisiones 24/7 se sitúan al inicio de la lista.
4.  **Almacenamiento en Caché (Room):** Los canales activos se persisten en Room bajo su respectivo identificador de filtro o país, permitiendo carga instantánea en visitas posteriores.
5.  **Recomendaciones en Android TV:** Al reproducir canales o modificar favoritos, `TvRecommendationManager` actualiza la fila de inicio y *"Continuar viendo"* en el launcher del televisor.
6.  **Reproducción con Fallback Selectivo:** Al pulsar sobre un canal, se inicia `ExoPlayerView` a pantalla completa. En caso de HTTP 403 Forbidden, el reproductor conmuta al proxy local para resolver la señal.

---

## ⚙️ Cómo Compilar e Instalar

Puedes compilar la aplicación utilizando Android Studio o directamente desde la terminal de comandos de Windows (Powershell/CMD).

### Método 1: Desde la Terminal (Gradle CLI)
1. Abre tu terminal y dirígete a la carpeta del proyecto:
   ```powershell
   cd C:\xampp\htdocs\Chari-IPTV_TV
   ```
2. (Opcional, si no tienes definido JAVA_HOME):
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   ```
3. Ejecuta el comando de compilación:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
4. El archivo instalable lo encontrarás en:
   `app\build\outputs\apk\debug\app-debug.apk`

### Método 2: Desde Android Studio
1. Abre **Android Studio** -> *Open Project* -> Selecciona la carpeta `Chari-IPTV_TV`.
2. Espera a que termine la sincronización de Gradle (barra inferior).
3. Ve al menú superior: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Haz clic en el aviso flotante "Locate" para acceder a la carpeta del APK generado.

### Instalación en la TV
Una vez generado el archivo **`app-debug.apk`**, puedes instalarlo en tu Smart TV o TV Box de dos formas:
*   **Memoria USB (Pendrive):** Copia el APK a un pendrive, conéctalo a la TV y ejecútalo mediante un explorador de archivos (ej. *File Commander* o *X-plore*).
*   **Envío por Red Local (Wi-Fi):** Usa la app gratuita **"Send Files to TV"** (disponible en Google Play Store tanto en el móvil/PC como en el Smart TV) para transferir el APK directamente e instalarlo.
