# Chari-IPTV - Aplicación Nativa para Android TV 📺

**Chari-IPTV** (anteriormente conocida como TVDROID) es una aplicación nativa diseñada específicamente para dispositivos con Android TV (Smart TVs, TV Boxes, Fire TV Sticks). Esta aplicación es una evolución y migración del proyecto original web (IPTVPRO_ESP), reconstruida desde cero para brindar una experiencia inmersiva, de alto rendimiento y totalmente autónoma en la sala de estar.

> 📖 **Nota de Interfaz:** Para una descripción detallada de todos los elementos visuales, comportamiento y flujos de usuario de la aplicación, consulta la [Documentación de Interfaz de Usuario (INTERFAZ_USUARIO.md)](./INTERFAZ_USUARIO.md).

---

## 🚀 Características Principales

*   **100% Autónoma:** A diferencia de la versión web, Chari-IPTV no necesita un servidor backend (XAMPP/PHP). Se conecta directamente a internet, descargando, parseando y validando la lista oficial de canales IPTV.
*   **Soporte Multi-País:** Los usuarios pueden desplazarse a través de un menú superior (Tabs) para explorar canales de televisión abierta y regional de toda Hispanoamérica (Perú, México, España, Argentina, Chile, Colombia, etc.).
*   **Validación Local de Canales:** Antes de mostrar los canales en pantalla, la aplicación realiza una petición HTTP parcial (`Range: bytes=0-500`) en segundo plano para garantizar que el canal está emitiendo en vivo, evitando mostrar canales caídos.
*   **Caché Persistente (Room):** Para evitar congelamientos o el uso excesivo de datos, la aplicación solo valida los canales del país seleccionado. Una vez validados, se guardan en una base de datos local permanente. Al salir y volver a ingresar a la app, los canales cargan de forma instantánea (0 segundos de espera). Para buscar canales nuevos o actualizar los caídos, el usuario puede forzar un escaneo remoto volviendo a seleccionar la pestaña del país en el que ya se encuentra.
*   **Sección Exclusiva de Favoritos:** La base de datos local gestiona tus canales preferidos. Tienes una pestaña exclusiva llamada **"Favoritos"** al principio del menú principal que agrupa en tiempo real todos los canales que has marcado, sin importar su país de origen.
*   **Interfaz Leanback / Modo Oscuro:** Construida con *Jetpack Compose*, la interfaz presenta un Grid (cuadrícula) en tono oscuro absoluto (`#0F172A`), ideal para visualización nocturna, reduciendo la fatiga visual. Las tarjetas de canal manejan sus estados de error inteligentemente mostrando fallbacks visuales automáticos cuando una imagen no está disponible.
*   **Controles Optimizados:** Interfaz 100% nativa para D-Pad. Interacción fluida mediante *Long Press* en la grilla y mapeo de botones de hardware (Retroceso, D-Pad Up) durante la reproducción para una experiencia limpia sin botones superpuestos.
*   **Motor de Video ExoPlayer:** Integra `Media3 ExoPlayer` de Google, el estándar de la industria para Android TV. Ofrece reproducción nativa HLS (`.m3u8`), gestión inteligente de errores de red (cierra el video y avisa con un mensaje 'Toast' si el canal cae en lugar de quedar en negro), permisos para aceptar HTTP sin cifrar (`usesCleartextTraffic`), certificados auto-firmados, prevención del apagado de pantalla (screensaver) automático, y pausa inteligente del audio al pasar a segundo plano gracias a un manejo robusto del `Lifecycle` de Android.

---

## 🛠️ Stack Tecnológico

*   **Lenguaje:** Kotlin 1.9+
*   **UI Toolkit:** Jetpack Compose
*   **Reproductor Multimedia:** AndroidX Media3 (ExoPlayer) con manejo de eventos de ciclo de vida (`LifecycleEventObserver`)
*   **Cliente HTTP:** OkHttp (con configuraciones personalizadas de timeout y bypass SSL)
*   **Asincronía:** Kotlin Coroutines (`async` / `awaitAll`) & Dispatchers
*   **Carga de Imágenes:** Coil Compose `SubcomposeAsyncImage` (renderizado asíncrono y control de estados de error)
*   **Persistencia:** Room Database (Caché de Canales y gestión de Favoritos)
*   **Build System:** Gradle (Kotlin DSL)

---

## 📂 Arquitectura del Proyecto

El flujo de trabajo es el siguiente:
1.  **Descarga Global:** Al iniciar `MainActivity.kt`, se descarga a la memoria caché la lista maestra global `index.m3u` (`https://iptv-org.github.io/iptv/index.m3u`).
2.  **Selección de País:** Cuando se selecciona una pestaña (ej. Perú), el motor interno procesa la lista en memoria y busca coincidencias con el código de país.
3.  **Verificación Concurrente:** Usando corrutinas, se lanzan procesos asíncronos paralelos para verificar el estado de los canales (conexión de 500 bytes). Solo los canales que responden un código de éxito 20x/30x (y no son páginas HTML) pasan el filtro.
4.  **Renderizado UI:** La lista limpia se devuelve al hilo principal (`Dispatchers.Main`) y Jetpack Compose renderiza de inmediato el `TvLazyVerticalGrid`. Si se selecciona la pestaña Favoritos, se unifican todos los canales validados y guardados previamente.
5.  **Reproducción:** Al hacer clic en un canal, la cuadrícula se suspende (guardando su estado de scroll) y se carga la vista `ExoPlayerView`.

---

## ⚙️ Cómo Compilar e Instalar

Puedes compilar la aplicación utilizando Android Studio o directamente desde la terminal de comandos de Windows (Powershell/CMD).

### Método 1: Desde la Terminal (Gradle CLI)
1. Abre tu terminal y dirígete a la carpeta del proyecto:
   ```powershell
   cd C:\xampp\htdocs\IPTVPRO_ESP\Chari-IPTV
   ```
2. (Si tienes errores con JAVA_HOME, define la ruta de tu JDK primero):
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   ```
3. Ejecuta el comando de compilación:
   ```powershell
   .\gradlew assembleDebug
   ```
4. El archivo instalable lo encontrarás en:
   `app\build\outputs\apk\debug\app-debug.apk`

### Método 2: Desde Android Studio
1. Abre **Android Studio** -> *Open Project* -> Selecciona la carpeta `Chari-IPTV`.
2. Espera a que termine la sincronización de Gradle (barra inferior).
3. Ve al menú superior: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Haz clic en el botón flotante emergente "Locate" para ver tu APK.

### Instalación en la TV
Una vez tengas el archivo **`app-debug.apk`**, puedes instalarlo en tu Android TV de dos formas:
*   Copiándolo a una **Memoria USB** (Pendrive) y conectándola a la TV (necesitas un explorador de archivos en la TV como *File Commander*).
*   Enviándolo por Wi-Fi usando la aplicación gratuita **"Send Files to TV"** (disponible en la PlayStore tanto de tu celular/PC como de tu Smart TV).
