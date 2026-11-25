# 📺 Guía de Ayuda y Manual de Usuario - Chari-IPTV (Smart TV)

Bienvenido a la **Guía Completa de Usuario** de **Chari-IPTV** (Versión 1.2 para televisores). Este manual detalla todas las características, navegación con el mando a distancia físico, zapping rápido, catálogo de cine, servidor local LAN para control remoto desde el móvil y solución de problemas técnicos para dispositivos **Android TV, Google TV y Fire TV**.

---

## 📑 Tabla de Contenidos

1. [Requisitos del Sistema y Métodos de Instalación](#1-requisitos-del-sistema-y-métodos-de-instalación)
2. [Navegación con el Mando a Distancia (Control Físico)](#2-navegación-con-el-mando-a-distancia-control-físico)
3. [Zapping Rápido sin Salir del Video (Quick Drawer)](#3-zapping-rápido-sin-salir-del-video-quick-drawer)
4. [Gestión de Canales Favoritos](#4-gestión-de-canales-favoritos)
5. [Colección Cinematográfica Studio Ghibli (25 Películas)](#5-colección-cinematográfica-studio-ghibli-25-películas)
6. [Servidor Micro-HTTP LAN y Conectividad con el Celular](#6-servidor-micro-http-lan-y-conectividad-con-el-celular)
7. [Integración con la Pantalla de Inicio de Android TV](#7-integración-con-la-pantalla-de-inicio-de-android-tv)
8. [Sección "Importante" y Donaciones](#8-sección-importante-y-donaciones)
9. [Preguntas Frecuentes y Solución de Problemas (Troubleshooting)](#9-preguntas-frecuentes-y-solución-de-problemas-troubleshooting)
10. [Aviso Legal y Exención de Responsabilidad](#10-aviso-legal-y-exención-de-responsabilidad)

---

## 1. Requisitos del Sistema y Métodos de Instalación

### Dispositivos Compatibles
* **Android TV:** Sony, TCL, Philips, Hisense, Xiaomi TV y decodificadores Android TV certificados (API 21 / Android 5.0 en adelante).
* **Google TV:** Chromecast con Google TV (HD / 4K), televisores Google TV actuales.
* **TV Boxes & Dongles:** Xiaomi Mi Box S / TV Stick, Mecool, Nvidia Shield TV.
* **Fire TV:** Amazon Fire TV Stick, Fire TV Cube y televisores con Fire OS.

### Métodos de Instalación

#### Método 1: Mediante Memoria USB (Recomendado para televisores con puerto USB)
1. Descarga el instalador oficial **`Chari-IPTV_1.2.apk`** en tu computadora.
2. Cópialo en la raíz de una memoria USB formateada en FAT32 o NTFS.
3. Conecta el USB a tu televisor o TV Box.
4. Abre una app exploradora de archivos en la TV (como *X-plore*, *File Commander* o *AnExplorer*, disponibles en Google Play Store).
5. Selecciona el archivo `Chari-IPTV_1.2.apk`, autoriza la instalación de orígenes desconocidos y pulsa **Instalar**.

#### Método 2: Mediante Wi-Fi con la app "Send Files to TV" (Sin cables)
1. Instala la app gratuita **"Send Files to TV"** tanto en tu teléfono móvil como en tu televisor desde Google Play Store.
2. Descarga el archivo `Chari-IPTV_1.2.apk` en tu celular.
3. Abre la app en ambos dispositivos; en el celular elige **Send** y selecciona el APK, y en el televisor elige **Receive**.
4. Una vez terminada la transferencia en la TV, abre el archivo con un instalador de paquetes o explorador de archivos e instálalo.

#### Método 3: Mediante ADB por Red (Para usuarios avanzados)
Si tu TV tiene activadas las *Opciones de Desarrollador* y la *Depuración por Red*:
```powershell
adb connect 192.168.1.XX:5555
adb install -r Chari-IPTV_1.2.apk
```

---

## 2. Navegación con el Mando a Distancia (Control Físico)

Chari-IPTV ha sido diseñada de acuerdo con los estándares de diseño ergonómico de **Android TV Leanback & Compose TV**, permitiendo controlar el 100% de la aplicación únicamente con la cruceta direccional (D-Pad) y los botones centrales del mando.

```
                  ┌───────────────┐
                  │    ▲ ARRIBA   │
                  │               │
     ◀ IZQUIERDA  │   ● OK/CENTRO │  DERECHA ▶
                  │               │
                  │    ▼ ABAJO    │
                  └───────────────┘
                     [ ↩ ATRÁS ]
```

### Tabla de Comandos del Mando Físico

| Botón | En la Cuadrícula Principal | En Reproducción de TV en Vivo | En Películas Studio Ghibli |
| :--- | :--- | :--- | :--- |
| **OK / Centro** | Abre el canal o película seleccionada. | Muestra u oculta la barra informativa inferior. | Pausa o reanuda la película. |
| **Izquierda (◀)** | Navega hacia la tarjeta izquierda. | **Despliega el menú de zapping lateral (Quick Drawer)**. | Retrocede **15 segundos**. |
| **Derecha (▶)** | Navega hacia la tarjeta derecha. | Cierra el menú lateral de zapping. | Adelanta **15 segundos**. |
| **Arriba (▲)** | Sube a la barra de pestañas y categorías. | **Añade o quita el canal actual de Favoritos (⭐)**. | Muestra la barra de progreso. |
| **Abajo (▼)** | Baja a la cuadrícula de canales. | Oculta la barra de información. | Abre el selector inferior de películas. |
| **Atrás (Back)** | Sube al menú de categorías; un segundo toque sale. | Cierra la reproducción y vuelve al menú. | Muestra el menú de confirmación de salida. |

---

## 3. Zapping Rápido sin Salir del Video (Quick Drawer)

Una de las funciones más destacadas de la Versión 1.2 es el **Quick Drawer de Zapping**:

* Mientras estás viendo un canal a pantalla completa, presiona la flecha **Izquierda (◀)** del control remoto.
* Aparecerá una barra lateral elegante y semitransparente sobre el borde izquierdo con la lista completa de canales de la categoría actual.
* Usa las flechas **Arriba (▲)** y **Abajo (▼)** para explorar otros canales.
* Presiona **OK** sobre cualquier canal: la transmisión cambiará de inmediato sin cortar la pantalla ni regresar al menú inicial.
* Para cerrar el menú sin cambiar de canal, presiona la flecha **Derecha (▶)** o el botón **Atrás**.

---

## 4. Gestión de Canales Favoritos

Guarda tus canales recurrentes para tenerlos siempre a mano en la primera pestaña:

* **Desde la Cuadrícula:** Mantén presionado el botón **OK** durante 2 segundos sobre cualquier tarjeta de canal.
* **Durante la Reproducción en Vivo:** Presiona la flecha **Arriba (▲)** del mando mientras miras el canal; aparecerá un aviso en pantalla confirmando *"Añadido a Favoritos"* o *"Eliminado de Favoritos"*.
* **Icono Distintivo:** Los canales favoritos muestran una estrella dorada (`⭐`) en su tarjeta y en la barra de información.

---

## 5. Colección Cinematográfica Studio Ghibli (25 Películas)

La sección **Studio Ghibli** te ofrece la filmografía completa del legendario estudio en orden cronológico (1984 a 2023):

* **Audio en Español:** Todas las obras cuentan con doblaje oficial en español de alta fidelidad.
* **Controles de Reproducción:** Usa **Izquierda** y **Derecha** para avanzar o retroceder en saltos de 15 segundos.
* **Reanudación Inteligente (Smart Resume):** Si debes apagar el televisor o interrumpir una película, la aplicación guarda automáticamente el punto exacto de visualización. Al volver a abrir la película, se presentará el cuadro de diálogo:
  * **"Continuar donde lo dejaste":** Reanuda en el minuto exacto.
  * **"Comenzar de nuevo":** Reproduce desde el inicio.

---

## 6. Servidor Micro-HTTP LAN y Conectividad con el Celular

Chari-IPTV integra en segundo plano un servidor micro-HTTP de alto rendimiento diseñado para comunicarse con la aplicación móvil **Chari-IPTV Cel**:

```
 ┌──────────────────────┐  mDNS (_chari-tv)  ┌──────────────────────┐
 │ Chari-IPTV Cel (Móvil)│ ────────────────▶  │ Chari-IPTV (Smart TV) │
 │                      │   REST / HTTP 8990 │                      │
 │ - Control D-Pad      │ ─────────────────▶ │ - Servidor TvLanServer│
 │ - Buscar en TV       │                    │ - EventBus de Teclas │
 │ - Sincronizar Favoritos│ ─────────────────▶ │ - Inyección a Room   │
 └──────────────────────┘                    └──────────────────────┘
```

### Características del Servidor Local:
1. **Puerto de Escucha:** `8990`.
2. **Descubrimiento Automático:** Se anuncia en la red mediante mDNS (ZeroConf / DNS-SD) bajo el servicio `_chari-tv._tcp.` con el nombre del televisor (por ejemplo, `Chari-TV-MIBOX4`).
3. **Seguridad Anti-SSRF:** Valida la dirección IP de origen de cada petición entrante. Únicamente se atienden dispositivos conectados a la misma subred privada (192.168.x.x, 10.x.x.x, 172.16-31.x.x), bloqueando cualquier intento de conexión externa.
4. **Operaciones Soportadas:**
   * `/api/command` con acción `play`: Sintoniza canales enviados desde el celular con cero retardo.
   * `/api/remote` con acciones de tecla: Simula las pulsaciones del control remoto físico (`up`, `down`, `left`, `right`, `enter`, `back`, `home`, `play_pause`, `volume_up`, `volume_down`, `mute`).
   * `/api/remote` con acción `text`: Introduce textos de búsqueda directamente desde el teclado táctil del móvil.
   * `/api/command` con acción `SYNC_FAVORITES`: Sincronización bidireccional que fusiona los favoritos de la TV y del celular, insertando los que faltan en ambos dispositivos y devolviendo la lista completa unificada.

---

## 7. Integración con la Pantalla de Inicio de Android TV

Chari-IPTV aprovecha la arquitectura de canales del launcher de Android TV (TvContract / Preview Channels):
* La app registra automáticamente una fila de **"Canales Destacados"** en la pantalla principal de tu televisor.
* Puedes abrir tus canales preferidos directamente al encender la TV sin necesidad de buscar la app en la lista general.

---

## 8. Sección "Importante" y Donaciones

Chari-IPTV es un proyecto independiente sin fines lucrativos ni muros de pago. Si disfrutas de la aplicación y deseas colaborar con el servidor de índices y el desarrollo continuo:
* En la barra superior de la app, selecciona la pestaña **"Importante"**.
* Podrás escanear el código QR de **Yape (Perú)** con tu teléfono o pulsar el enlace de **PayPal (Internacional)**.
* Visita el portal oficial en [www.qwertyaserty.com](https://www.qwertyaserty.com).

---

## 9. Preguntas Frecuentes y Solución de Problemas (Troubleshooting)

### P1: La aplicación del celular no encuentra mi televisor
* Asegúrate de que tanto el televisor como el celular estén conectados al **mismo router Wi-Fi**.
* Comprueba que la aplicación Chari-IPTV esté **abierta en pantalla** en el televisor (el servidor se apaga si sales completamente de la aplicación para ahorrar recursos del sistema).
* Verifica que en tu router no esté activada la opción *"Aislamiento de clientes"* o *"AP Isolation"*.

### P2: Una transmisión en vivo se congela o tarda en cargar
* Los canales provienen de señales libres internacionales cuyos servidores pueden experimentar congestión temporal. Presiona la flecha **Izquierda (◀)** para abrir el Quick Drawer y prueba otro canal o espera 5 segundos a que el reproductor intente la reconexión automática.

### P3: La imagen se ve bien pero no hay audio
* Si estás utilizando una barra de sonido o receptor AV por cable HDMI eARC / óptico, revisa la configuración de salida de audio de Android TV (*Ajustes > Pantalla y Sonido > Salida de Audio*) y selecciona **PCM** o **Passthrough Automático**.

### P4: El control remoto de la TV responde lento
* Si notas retardo en las animaciones, asegúrate de no tener aplicaciones pesadas ejecutándose en segundo plano en tu TV Box y reinicia el dispositivo si lleva semanas sin apagarse.

---

## 10. Aviso Legal y Exención de Responsabilidad

1. **Naturaleza del Software:** Chari-IPTV es un reproductor cliente de medios de libre acceso para la comunidad. No dispone de servidores de streaming propios ni comercializa servicios audiovisuales.
2. **Índices Públicos:** Todos los enlaces corresponden a transmisiones libres y abiertas alojadas en la red mundial por sus respectivas emisoras.
3. **Catálogo Studio Ghibli:** Las películas referenciadas provienen de copias de archivo cultural alojadas en plataformas públicas (*Internet Archive*).
4. **Marcas Registradas:** Las marcas, cadenas televisivas y nombres comerciales pertenecen a sus legítimos propietarios. Las producciones animadas son propiedad intelectual de *Studio Ghibli Inc.*
