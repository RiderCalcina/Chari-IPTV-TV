# Chari-IPTV TV - Aplicación Nativa para Android TV 📺

**Chari-IPTV TV** (anteriormente conocida como TVDROID) es una aplicación nativa diseñada específicamente para dispositivos con Android TV (Smart TVs, TV Boxes, Fire TV Sticks). Esta aplicación es una evolución y migración del proyecto original web (IPTVPRO_ESP), reconstruida desde cero con **Jetpack Compose para TV** para brindar una experiencia inmersiva, de alto rendimiento y totalmente autónoma en la sala de estar.

> 📖 **Nota de Interfaz:** Para una descripción detallada de todos los elementos visuales, comportamiento y flujos de navegación de la aplicación, consulta la [Documentación de Interfaz de Usuario (INTERFAZ_USUARIO.md)](./INTERFAZ_USUARIO.md).

---

## 🚀 Características Principales

*   **100% Autónoma:** A diferencia de la versión web, Chari-IPTV TV no necesita un servidor backend local (XAMPP/PHP). Se conecta directamente a internet, descargando, parseando y validando la lista oficial de canales IPTV.
*   **Exclusividad 100% Habla Hispana:** La aplicación se alimenta de la lista curada oficial en español (`languages/spa.m3u`), asegurando que todos los canales y categorías provengan exclusivamente de España e Hispanoamérica, sin señales en idiomas extranjeros.
*   **Filtros por Categoría & Género en la Pantalla Principal:** Botones interactivos tipo tarjeta para acceder directamente a **Deportes**, **Noticias**, **Películas**, **Series**, **Infantil** y **Música**, validando transmisiones en vivo activas.
*   **Sub-selector de Países Dinámico:** Al presionar el botón "Países", se despliega una sub-fila interactiva con banderas oficiales y nombres para explorar canales locales de toda Hispanoamérica y España (Perú, México, Argentina, Chile, Colombia, etc.).
*   **Ventana Modal "Importante" (3 Códigos QR Simétricos):** Ubicada en primer lugar en la barra principal, con diseño panorámico y tres tarjetas simétricas para escanear con la cámara del teléfono: donaciones internacionales vía **PayPal**, donaciones en Perú mediante **Yape**, y visita a la web oficial del desarrollador (**www.qwertyaserty.com**), con mensaje de agradecimiento y aclaración de que la app es 100% gratuita.
*   **Carga Optimizada de Logos (Coil + SVG):** Motor de imágenes con soporte nativo para gráficos vectoriales `.svg`, cabeceras simuladas de navegador de escritorio (`Chrome User-Agent`) para evitar bloqueos HTTP 403 de Wikimedia/Imgur, y caché local en disco (100 MB) y memoria RAM.
*   **Validación Local de Canales:** Antes de mostrar los canales en pantalla, la aplicación realiza una petición HTTP parcial (`Range: bytes=0-500`) en segundo plano para garantizar que el canal está emitiendo en vivo, evitando mostrar canales caídos.
*   **Caché Persistente (Room Database v5):** Los canales validados se guardan en una base de datos local permanente con clave primaria compuesta `(url, countryCode)`. La lista de Favoritos es 100% autónoma y almacena el nombre, logo y URL directamente, garantizando que los canales guardados se muestren siempre de forma inmediata sin depender de la caché temporal.
*   **Cinta de Noticias RSS Integrada:** Marquesina de noticias deslizante continua (`basicMarquee`) que mantiene a los usuarios informados directamente en la pantalla de la TV con las publicaciones del blog oficial.
*   **Sección Exclusiva de Favoritos con Feedback Visual:** Base de datos local para gestionar tus canales preferidos. Al añadir o quitar un canal, se muestra un *Snackbar* personalizado y animado con iconos de corazones (❤️ o 🤍).
*   **Interfaz Leanback / Modo Oscuro:** Construida con *Jetpack Compose for TV*, presenta un Grid oscuro absoluto (`#0F172A`) optimizado para evitar fatiga visual en la sala de estar.
*   **Receptor Oficial de Google Cast:** La app está configurada formalmente como un receptor nativo de Chromecast, permitiendo que aplicaciones móviles descubran y transmitan hacia la TV.
*   **Motor de Video ExoPlayer & Audio FFmpeg:** Integra `Media3 ExoPlayer 1.2.0` con decodificador de audio por software `media3-ffmpeg-decoder` (soporte completo de audio AC3, E-AC3 y DTS), gestión de transmisiones HLS y MPEG-DASH, política inteligente de reintentos (`IptvLoadErrorPolicy`), prevención de reposo (`FLAG_KEEP_SCREEN_ON`) y ciclo de vida seguro.

---

## 🛠️ Stack Tecnológico

*   **Lenguaje:** Kotlin 2.0+ (JVM Toolchain 17 / Compatible JDK 21)
*   **Target SDK:** Android SDK 36 (Android 16), Min SDK 24 (Android 7.0+)
*   **UI Toolkit:** Jetpack Compose for TV (`androidx.tv:tv-foundation:1.0.0-alpha11`, `androidx.tv:tv-material:1.0.0`) y Material 3
*   **Reproductor Multimedia:** AndroidX Media3 ExoPlayer 1.2.0 (HLS `.m3u8`, MPEG-DASH `.mpd`, Jellyfin FFmpeg Audio Decoder, IptvLoadErrorPolicy)
*   **Cliente HTTP & Red:** Retrofit 2.11.0 + OkHttp 4.12.0 (bypass SSL, timeout optimizado y User-Agent de navegador)
*   **Inyección de Dependencias:** Dagger Hilt 2.51.1
*   **Asincronía & Reactividad:** Kotlin Coroutines (`async` / `awaitAll`), Dispatchers (`IO` / `Main`), StateFlow
*   **Carga de Imágenes:** Coil Compose 2.6.0 + Coil SVG 2.6.0 (`SubcomposeAsyncImage`, `ImageLoaderFactory`, DiskCache 100MB)
*   **Persistencia Local:** Room Database 2.6.1 (Caché de Canales v5 y Favoritos autónomos con clave compuesta)
*   **Build System:** Gradle (Kotlin DSL con Version Catalog `libs.versions.toml`)

---

## 📂 Arquitectura del Proyecto

El flujo de trabajo es el siguiente:
1.  **Descarga Curada en Español:** El repositorio consulta la lista oficial `languages/spa.m3u` (`https://iptv-org.github.io/iptv/languages/spa.m3u`) asegurando únicamente contenido en español.
2.  **Selección de Filtro o País:** Cuando el usuario selecciona una categoría (ej. Deportes, Noticias, Películas) o un país (ej. Perú, México), el motor filtra las transmisiones por `group-title` o código de país.
3.  **Verificación Concurrente:** Usando corrutinas paralelas, se verifica el estado en vivo de los canales mediante una conexión de 500 bytes. Solo los canales que responden con códigos 20x/30x (y contenido multimedia) pasan a la vista.
4.  **Almacenamiento en Caché (Room):** Los canales activos se persisten en Room bajo su respectivo identificador de filtro, permitiendo carga instantánea en visitas posteriores.
5.  **Renderizado UI:** La grilla (`TvLazyVerticalGrid`) renderiza 4 columnas de tarjetas interactivas adaptadas al control remoto D-Pad con foco escalado a `1.15f`.
6.  **Reproducción:** Al pulsar sobre un canal, la cuadrícula suspende su renderizado y se inicia `ExoPlayerView` a pantalla completa.

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
