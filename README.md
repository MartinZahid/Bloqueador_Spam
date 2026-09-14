# LadaBlocker

App Android (Kotlin + Compose) que bloquea llamadas de cobranza/spam en México **sin que suenen**:
por **lada** (ej. `637`), por **número exacto**, y con **bases comunitarias** de números reportados
(cobranza, telemarketing, fraude).

## Requisitos
- Android 12 o superior (Samsung S25 Ultra probado).
- `minSdk 31` · `targetSdk 35` · Kotlin 2.0 · Compose/Material 3.

## Compilar
```bash
gradlew.bat :app:assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk

gradlew.bat :app:assembleRelease
# APK firmado en: app/build/outputs/apk/release/app-release.apk
```

### Firma del release
El release usa un keystore local (`keystore.jks`, alias `ladablocker`). Las credenciales
están en `keystore.properties` (ambos archivos van en `.gitignore`; **no los pierdas**, son
necesarios para actualizar la app sin desinstalar).

- `keystore.jks` + `keystore.properties` → contraseñas por defecto `ladablocker` (cámbialas si lo necesitas).
- Regenerar el keystore (solo la primera vez):
  ```bash
  keytool -genkeypair -v -keystore keystore.jks -alias ladablocker \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass ladablocker -keypass ladablocker \
    -dname "CN=LadaBlocker, OU=Personal, O=Personal, L=MX, ST=MX, C=MX"
  ```

## Instalar por USB
1. En el celular: *Ajustes > Acerca del teléfono > información de software* toca 7 veces “Número de compilación”.
2. *Ajustes > Opciones de desarrollador > Depuración por USB* → activar.
3. Conecta el cable y acepta la huella de depuración.
4. `adb install -r app\build\outputs\apk\debug\app-debug.apk`

## Activar (IMPORTANTE, una sola vez)
1. Abre **LadaBlocker** (o desde la app, pestaña *Ajustes*).
2. Toca **“Abrir 'ID de llamada y spam'”** y elige **LadaBlocker**. *Sin este paso Android no deja a la app revisar llamadas.*
3. Toca **“Batería: permitir uso sin restricción”** → *Permitir* (evita que Samsung la cierre).
4. *(Opcional)* Activa “Nunca bloquear a mis contactos” y acepta el permiso de contactos.

Después de esto, una llamada que coincida con alguna regla **no suena ni aparece como entrante**:
se rechaza al instante y se guarda en *Historial*.

## Cómo configura el bloqueo
- **Interruptor maestro** (Ajustes, arriba): pausa/reactiva todo el bloqueo de un toque (RF-10).
- **Ladas**: pestaña *Ladas*. La lada `637` (Sonora) llega pre-marcada. Tú marcas cuáles.
- **Números exactos y lista blanca**: pestaña *Números*. La lista blanca siempre respeta a tus contactos.
- **Bases comunitarias**: pestaña *Números* → *Archivo* o *Pegar lista* (o URL en *Ajustes*).
  Todo lo nuevo queda en la pestaña **Revisar** para que lo apruebes antes de bloquear.
  La app se actualiza **semanalmente y solo en Wi-Fi**, en modo sigilo (sin notificaciones).
- **Reportar desde el historial**: en cada llamada bloqueada hay dos acciones — *Bloquear siempre*
  (la agrega a números exactos) y *No bloquear este número* (la agrega a la lista blanca).

## Fuentes de listas comunitarias
- NoCall `nocall.io/mx/numeros-spam/cobros` (números reportados como cobros).
- SpamBlacklistMX (foros CallFilter): máscaras/prefijos de call centers de Coppel, HSBC, BBVA, etc.
- Tellows.mx y reportes de la comunidad mexicana.

Cualquier lista `.txt`/`.csv` (un número por línea, con o sin `+52`/`044`/`01`) se puede importar;
una línea terminada en `*` se trata como prefijo. Prefijos normales soportados:
`+52 1`, `521`, `52`, `044`, `045`, `01`, `1`.

## Notas sobre comportamiento
- Al bloquear, la llamada se rechaza en el acto (sin tono); es el máximo que permite Android a apps
  de terceros. Algunos Samsung pueden dejar un registro “rechazada” en el marcador del sistema.
- Bloquear una lada completa **también bloquea llamadas legítimas de esa zona**: úsalo con criterio
  y desactiva reglas con un switch.
- La app no usa servicios en segundo plano, ni notificaciones, ni restricciones de batería: solo se
  “despierta” al llegar una llamada (consumo ≈ 0).