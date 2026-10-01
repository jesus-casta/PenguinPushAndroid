# PenguinPush · Android

Proyecto Android Studio (Java 17, Android 7/API 24+ con System WebView actualizado). Incluye **50 mapas clásicos de Sokoban**, **12 tutoriales**, los pingüinos azul y rosa sin libro, cajas de pescado, animaciones y efectos sonoros originales.

## Abrir y ejecutar

1. Clona este repositorio y abre su carpeta raíz en Android Studio.
2. Usa JDK 17 para Gradle e instala Android SDK 36.
3. Deja que Gradle sincronice y pulsa Run con un emulador o dispositivo.

```sh
chmod +x gradlew
./gradlew assembleDebug
```

El APK de prueba queda en `app/build/outputs/apk/debug/app-debug.apk`. La aplicación usa `es.castanon.penguinpush` como identificador inicial. La compilación de distribución necesita la firma que configures tú.

Gradle Wrapper 8.13 y Android Gradle Plugin 8.13.2 están fijados en el proyecto. No hay bibliotecas de juego ni servicios externos.

## Arquitectura

`MainActivity` es un contenedor nativo Java con **Android WebView**. El tablero, la lógica y las animaciones usan el motor HTML/Canvas compartido; no se reimplementan en vistas Android. El juego está incluido en `app/src/main/assets/game`.

La WebView sirve esos archivos desde un origen HTTPS local interceptado, con una lista cerrada de recursos. No solicita permiso de Internet, no accede a archivos externos ni permite navegar a webs remotas. El audio comienza con una interacción, se puede silenciar y se pausa al pasar a segundo plano. La rotación conserva el contenedor; la interfaz respeta las barras del sistema.

El progreso guarda niveles completados, mejores contadores y preferencias. Una partida sin terminar se reinicia al cerrar el proceso. Desinstalar la app elimina ese progreso.

## Validación

```sh
node scripts/check-bundle.cjs
./gradlew assembleDebug lintDebug
```

La automatización de GitHub compila el APK de depuración y ejecuta Android Lint. No publica en Google Play. Prueba los gestos, el almacenamiento y el sonido en un dispositivo con WebView actualizado antes de distribuirla.

El código compartido se edita en [PenguinPushHTML](https://github.com/jesus-casta/PenguinPushHTML) y se sincroniza con su script `scripts/sync-native.py`. `bundle-manifest.json` permite comprobar la copia incluida.

La procedencia de los mapas está en [ORIGINAL_LEVELS.md](ORIGINAL_LEVELS.md). Los clásicos son de Thinking Rabbit; su distribución permanece intacta.

El Gradle Wrapper procede de [gradle/gradle, v8.13.0](https://github.com/gradle/gradle/tree/v8.13.0), bajo la licencia Apache-2.0 del proyecto Gradle.
