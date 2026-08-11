# Comfy Cloud Launcher (Android)

App mínima para Android que actúa como "launcher" de tus apps de **App Mode** de Comfy Cloud: guardás los links (`https://cloud.comfy.org/?share=...`) que generás desde la web y los abrís con un toque, sin pasar por el navegador manualmente.

## Por qué no es un WebView

La primera idea natural es envolver `cloud.comfy.org` en un `WebView` nativo. No lo hice así a propósito: Comfy Cloud permite iniciar sesión con **Google**, y Google bloquea el login OAuth cuando se hace dentro de un WebView embebido (error `disallowed_useragent`), por política de seguridad propia, desde 2016. Si hubiera construido esto con WebView, cualquier usuario que use "Sign in with Google" se quedaría trabado en el login.

La solución fue usar **Chrome Custom Tabs** (`androidx.browser`) en vez de un WebView crudo:

- Google trata a Custom Tabs como un navegador real, así que el login con Google, GitHub o email funciona sin problemas.
- La sesión se comparte con el perfil de Chrome del dispositivo, así que una vez que iniciás sesión, se mantiene logueado entre aperturas de la app — igual que si lo abrieras en Chrome.
- Sigue sintiéndose como parte de la app: sin barra de navegación completa, con la barra de color de Comfy Cloud.

No verifiqué en detalle si `cloud.comfy.org` tiene alguna protección adicional contra ser embebido (frame-busting, CSP `frame-ancestors`, etc.) — si la tuviera, un WebView tampoco funcionaría igual. Con Custom Tabs ese problema no existe porque no es un iframe/WebView, es una pestaña de navegador real.

## Qué hace la app

- Pantalla principal con una lista de "apps" guardadas (nombre + link de App Mode).
- Botón "Agregar app" para pegar un nuevo link de App Mode que compartiste desde `cloud.comfy.org` (menú "Share" → App Mode en tu workflow).
- Tocar una app la abre en una Custom Tab.
- Mantener presionado el ícono de basura elimina una entrada guardada.
- Los links se guardan localmente en el dispositivo (SharedPreferences), no hay backend propio.

## Cómo compilar

1. Abrí la carpeta `ComfyCloudLauncher` en Android Studio (versión reciente, Giraffe o más nueva).
2. Dejá que Android Studio sincronice el proyecto — va a generar automáticamente el `gradlew`/`gradlew.bat` y descargar el wrapper de Gradle 8.7 (ya está declarado en `gradle/wrapper/gradle-wrapper.properties`, pero el `.jar` del wrapper no viene incluido en este export; Android Studio lo genera solo al sincronizar).
3. Conectá un dispositivo o usá un emulador y corré la app (Run ▶).
4. Para generar un APK instalable: `Build → Build Bundle(s) / APK(s) → Build APK(s)`.

Si preferís línea de comandos y tenés Gradle instalado globalmente, podés correr `gradle wrapper` una vez dentro de la carpeta para generar el wrapper, y después `./gradlew assembleDebug`.

## Compilar sin instalar nada (GitHub Actions)

Este proyecto incluye `.github/workflows/build.yml`, que compila el APK automáticamente en los servidores de GitHub cada vez que subís código. No hace falta instalar Android Studio, JDK, ni git en tu computadora.

1. Creá una cuenta gratuita en [github.com](https://github.com) si no tenés.
2. Creá un repositorio nuevo (público o privado, cualquiera sirve) — botón "New" en tu perfil.
3. Dentro del repo vacío, tocá "uploading an existing file" (o "Add file → Upload files").
4. Arrastrá **toda** la carpeta `ComfyCloudLauncher` (todo su contenido, incluida la carpeta oculta `.github`) a la zona de subida del navegador. Los navegadores modernos aceptan arrastrar carpetas completas ahí.
5. Confirmá el commit ("Commit changes") directo sobre la rama `main`.
6. Andá a la pestaña "Actions" del repositorio. Debería aparecer una ejecución en curso llamada "Build debug APK". Esperá a que termine (círculo verde ✓, unos minutos).
7. Entrá a esa ejecución terminada y bajá hasta "Artifacts". Vas a ver `ComfyCloudLauncher-debug-apk` — descargalo (es un .zip que contiene el `app-debug.apk`).
8. Pasá el `app-debug.apk` a tu celular (Drive, WhatsApp a vos mismo, cable, etc.), abrilo, aceptá instalar "apps de origen desconocido" la primera vez, e instalá.

Nota de honestidad: no pude probar este workflow en un run real de GitHub Actions porque este entorno no tiene salida de red hacia GitHub tampoco. Está escrito siguiendo el patrón estándar y bien documentado de `actions/setup-java` + `gradle/actions/setup-gradle` + `actions/upload-artifact`, pero si algún paso falla (por ejemplo si la ruta del Android SDK en el runner de GitHub cambió, `$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager`), el log de esa ejecución en la pestaña Actions va a mostrar exactamente en qué paso falló — pegame ese error y lo ajusto.

## Cosas para verificar antes de confiar en esto a largo plazo

- La API de `androidx.browser` (`CustomTabColorSchemeParams`, `CustomTabsIntent.Builder`) evolucionó en los últimos releases; si al compilar algún método aparece como no encontrado, conviene revisar la versión más reciente en la documentación de `androidx.browser` y ajustar la dependencia en `app/build.gradle.kts`.
- Las versiones de Android Gradle Plugin (8.5.2) y Kotlin (1.9.24) elegidas acá son razonablemente recientes a la fecha de este proyecto, pero convendría dejar que Android Studio sugiera actualizarlas si detecta versiones más nuevas al abrir el proyecto.
- No probé el login real con Google/GitHub dentro de Custom Tabs en este entorno (no tengo forma de correr un emulador Android acá); es el comportamiento documentado y estándar del patrón Custom Tabs + OAuth, pero vale la pena confirmarlo con tu cuenta real en el primer test.
