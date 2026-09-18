# 📺 Interfaz de Usuario: Chari-IPTV

La interfaz de usuario de **Chari-IPTV** está diseñada específicamente para Android TV utilizando el moderno kit de herramientas **Jetpack Compose (androidx.tv.material3)**. A continuación, se detalla cada aspecto visual e interactivo de la aplicación.

## 1. Diseño General (Look & Feel)
- **Tema Oscuro (Dark Mode):** Toda la aplicación tiene un fondo oscuro profundo (Color `#0F172A`), ideal para entornos con poca luz como la sala de estar.
- **Navegación D-Pad Pura:** Todos los elementos interactivos están optimizados para el control remoto tradicional (botones direccionales: arriba, abajo, izquierda, derecha y OK).

## 2. Barra de Navegación de Países (Menú Superior)
- Se presenta como un carrusel horizontal (`LazyRow`) en la parte superior.
- **Pestaña "Favoritos":** Es el primer elemento de la lista. Agrupa exclusivamente todos los canales que el usuario ha marcado con un corazón, sin importar de qué país sean. Funciona con refresco en tiempo real.
- **Países Soportados:** A continuación de Favoritos, se listan los países de Hispanoamérica (Argentina, Bolivia, Chile, Colombia, etc.).
- **Feedback Visual:** El país seleccionado se resalta con un fondo azul brillante (`#3B82F6`) y texto en negrita. Los países inactivos tienen un fondo oscuro (`#1E293B`) y texto gris claro. El país enfocado con el control cambia de color para indicar la posición (`#60A5FA`).
- **Scroll Inteligente:** Al abrir la aplicación o cambiar de selección, la barra se desplaza automáticamente para mantener visible el país activo.
- **Escanear Canales Nuevos:** Si el usuario selecciona con un clic normal un país en el que *ya se encuentra posicionado*, aparecerá una ventana de diálogo (AlertDialog) para confirmar si desea volver a escanear los canales de ese país. Esto borrará la caché local y buscará transmisiones frescas desde el servidor.
- **Navegación Espacial (Focus Restoration):** La barra de países tiene memoria nativa (`focusRestorer()`). Cuando navegas por la grilla hacia abajo y regresas hacia arriba (botón Up), el foco vuelve como un imán exactamente a la última pestaña de país por la que pasaste, ignorando falsos positivos de la geometría espacial de Android TV y evitando saltos inesperados.

## 3. Grilla de Canales (Channel Grid)
- **Distribución:** Es una cuadrícula (`TvLazyVerticalGrid`) organizada en **4 columnas** fijas.
- **Tarjetas de Canal (Cards):** Cada canal está representado por una tarjeta rectangular (proporción 16:11).
  - *Fondo:* Gris oscuro (`#1E293B`).
  - *Logo:* Muestra el logotipo del canal centrado. Utiliza carga inteligente asíncrona (Coil `SubcomposeAsyncImage`). Si no hay logo o el enlace está roto/inaccesible (falla de red 404), el componente intercepta el error automáticamente y muestra un emoji (📺) como fallback para mantener la grilla uniforme.
  - *Nombre:* El nombre del canal aparece centrado abajo del logo, truncado a un máximo de 2 líneas.
- **Indicador de Favorito:** En la esquina superior derecha de cada tarjeta hay un botón circular traslúcido. Muestra un corazón rojo (❤️) si es favorito, o uno blanco (🤍) si no lo es. Es solo un indicador visual. Para marcar o desmarcar un canal como favorito, debes **mantener presionado el botón OK** (D-Pad Center) del control remoto sobre la tarjeta del canal (Long Press).
- **Efectos de Foco (Hover):** Cuando se navega con el control remoto sobre una tarjeta:
  - La tarjeta aumenta ligeramente su tamaño (`focusedScale = 1.15f`).
  - Se ilumina con un borde blanco de 4dp de grosor.
  - El fondo cambia a un gris más claro (`#334155`).
- **Orden de Canales:** Dentro de un país específico, los canales se muestran en un orden alfabético estricto, sin mezcla.
- **Preservación de Foco:** Al regresar a la grilla tras reproducir un canal, la vista recuerda su estado y restaura el foco en el último canal seleccionado.

## 4. Indicador de Carga (Loading State)
- Mientras se descargan y validan los canales de un país por primera vez, o durante un re-escaneo, la grilla es reemplazada por una pantalla de carga.
- La barra de navegación de países **nunca desaparece** durante la carga. El indicador de progreso se renderiza *debajo* de las pestañas, reemplazando únicamente a los canales. Esto permite al usuario abortar la espera y cambiar a otro país en cualquier momento, además de preservar intacto el motor de foco.
- Muestra un **Círculo de Progreso** (CircularProgressIndicator) de color blanco en el centro de la pantalla.
- Un texto descriptivo indica la acción: "Buscando canales de [Nombre del País]...".

## 5. Reproductor de Video (ExoPlayerView)
- **Pantalla Completa:** Al seleccionar un canal, el reproductor ocupa toda la pantalla con un fondo negro puro. La grilla queda "debajo" conservando su estado en memoria.
- **Manejo del Ciclo de Vida (Background Audio):** El reproductor está sincronizado con el `Lifecycle` de Android TV (`DisposableEffect`). Si el usuario presiona el botón "Home" o abre otra app, el reproductor captura el evento `ON_PAUSE/ON_STOP` y detiene el flujo multimedia inmediatamente (evitando audio fantasma en segundo plano). Al regresar a la app, se reanuda (`ON_RESUME`).
- **Controles Nativos e Interacción:** El reproductor no tiene botones dibujados superpuestos para evitar interferir con los controles nativos de ExoPlayer. Todo se maneja a través del control remoto físico:
  - **Salir del Video:** Se utiliza el botón de hardware **Retroceder (Back / Return)** del control remoto para volver a la grilla, lo cual dispara la destrucción (`ON_DESTROY`) y libera recursos de memoria.
  - **Alternar Favorito:** Mientras se está reproduciendo el video, puedes presionar la tecla direccional **Arriba (D-Pad Up)** en el control remoto para añadir o quitar el canal de favoritos de forma invisible. Un aviso breve (Toast) confirmará la acción.
- **Prevención de Apagado:** La interfaz envía una orden al sistema (`FLAG_KEEP_SCREEN_ON`) para evitar que el salvapantallas o el modo reposo del televisor se active mientras se reproduce contenido.
- **Gestión de Errores (Caídas):** Si un canal deja de emitir de repente o tiene un enlace inestable, en lugar de mostrar una pantalla negra infinita que confunda al usuario, ExoPlayer detecta el fallo (`Player.Listener`), cierra automáticamente el reproductor para devolver al usuario a la grilla y muestra un aviso breve en pantalla (Toast) indicando que la transmisión falló.
