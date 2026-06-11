# G316 Memoriza la Palabra — App Android

## Requisitos
- Android Studio Hedgehog (2023.1.1) o más reciente
- JDK 17 (viene incluido con Android Studio)
- Dispositivo Android o emulador con API 24+

---

## Pasos para compilar y generar el APK

### 1. Clona o descarga este proyecto
Si lo subiste a GitHub:
```
git clone https://github.com/TU_USUARIO/G316Memoriza.git
```
O simplemente abre la carpeta descargada.

### 2. Abre en Android Studio
- Abre Android Studio
- Selecciona **File → Open**
- Navega hasta la carpeta `G316Memoriza` y ábrela
- Espera que Gradle sincronice (puede tomar unos minutos la primera vez)

### 3. Genera el APK de debug (para probar)
- Menú superior: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- Espera que compile
- Android Studio mostrará una notificación: **"Build successful"**
- Haz clic en **"locate"** para encontrar el APK
- Ruta: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Instala en tu celular
**Opción A — USB:**
- Conecta tu celular por USB
- Activa **Opciones de desarrollador** y **Depuración USB** en tu Android
- En Android Studio: **Run → Run 'app'**

**Opción B — Copiar APK:**
- Copia el archivo `app-debug.apk` a tu celular
- En el celular, abre el archivo y acepta instalar desde fuentes desconocidas

### 5. APK firmado para distribuir (opcional)
- Menú: **Build → Generate Signed Bundle / APK**
- Selecciona **APK**
- Crea un keystore nuevo (guárdalo bien, lo necesitas para actualizaciones)
- Compila en modo **release**

---

## Estructura del proyecto
```
G316Memoriza/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── index.html        ← Toda la app (HTML + CSS + JS)
│   │   ├── java/com/g316/memoriza/
│   │   │   └── MainActivity.kt   ← Activity principal (WebView)
│   │   ├── res/
│   │   │   ├── drawable/         ← Íconos de la app
│   │   │   ├── layout/           ← Layout XML
│   │   │   └── values/           ← Strings, themes
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## Agregar tu propio ícono
Reemplaza los archivos en `app/src/main/res/drawable/`:
- `ic_launcher.png` — 48x48px
- `ic_launcher_round.png` — 48x48px

O usa **Android Studio → File → New → Image Asset** para generarlos automáticamente.

## Permisos incluidos
- `RECORD_AUDIO` — para el grabador de voz
- `INTERNET` — para cargar los íconos de Tabler desde CDN

## Versión mínima de Android
Android 7.0 (API 24) — cubre el 95%+ de dispositivos activos.
