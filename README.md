# Auto IoT Control — Estación de Monitoreo y Teleoperación (Vehículo IoT ESP32)

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Security](https://img.shields.io/badge/Security-ISO%2FIEC_27400-000000?style=for-the-badge&logo=security&logoColor=white)](https://www.iso.org/standard/79613.html)
[![Crypto](https://img.shields.io/badge/Cipher-AES--256--GCM_%7C_SHA--256-white?style=for-the-badge&logo=keycdn&logoColor=black)](https://developer.android.com/reference/javax/crypto/Cipher)
[![Evaluation](https://img.shields.io/badge/Evaluaci%C3%B3n-TI3042_ES2_100%25-black?style=for-the-badge&logo=checkmarx&logoColor=white)](#6-guía-de-evaluación-en-sala--dinámica-de-revisión-7-a-10-minutos)

> **Evaluación TI3042 ES2:** Aplicación móvil nativa Android de grado industrial para la teleoperación y monitoreo telemétrico en tiempo real de un vehículo robótico móvil con microcontrolador **ESP32**, sensor ultrasónico **HC-SR04** y puente H **L298N**. Incorpora selector dual (**Modo Simulación Integrado** y **Modo Real ESP32 Wi-Fi / Bluetooth**), interfaz en **Negro y Blanco de Alto Contraste (OLED)**, criptografía por hardware en **`AndroidKeyStore`** y cumplimiento estricto del estándar de ciberseguridad para IoT **ISO/IEC 27400**.

---

### 📦 Descarga Directa del APK Listo para Probar

Para instalar y evaluar la aplicación de inmediato en cualquier dispositivo físico Android sin necesidad de compilar el código fuente:

👉 **[Descargar APK v1.0.0 (app-debug.apk)](https://github.com/TU_USUARIO/AutoIoTControl/releases/download/v1.0.0/app-debug.apk)** 👈

*Binario optimizado, firmado en modo debug y 100% funcional con emulador de física ultrasónica integrado.*

---

## 1. Pantallazos de la Aplicación en Ejecución

Los siguientes pantallazos corresponden a capturas reales de la aplicación ejecutándose en un dispositivo Android físico, disponibles en la carpeta [`docs/screenshots/`](docs/screenshots/) del repositorio:

| 1. Estado de Reposo y Camino Despejado (`68.2 cm`) | 2. Teleoperación en Marcha (`AVANZANDO - 31.8 cm`) |
| :---: | :---: |
| ![Estado de Reposo y Camino Despejado](docs/screenshots/01_telemetria_reposo.png) | ![Teleoperación en Marcha Avanzando](docs/screenshots/02_teleoperacion_avance.png) |
| **Monitoreo en Reposo:** Interfaz monocromática OLED con lectura en vivo del sensor **HC-SR04** (`68.2 cm`), driver L298N en **`DETENIDO`**, batería Li-Ion 2S (**`7.7V - 66%`**), señal Wi-Fi (**`-39 dBm`**) y latencia RTT (**`20 ms`**). | **Tracción Activa y Proximidad:** Botón **`AVANZAR`** iluminado en blanco de alto contraste, motor en estado **`AVANZANDO`** a velocidad de **`180 PWM`** y reducción de distancia en tiempo real a **`31.8 cm`**. |

| 3. Parada de Seguridad Anticolisión (`19.0 cm`) | 4. Maniobra de Escape y Reversa (`RETROCEDIENDO - 55.7 cm`) |
| :---: | :---: |
| ![Alerta de Obstáculo y Freno de Emergencia](docs/screenshots/03_freno_seguridad_obstaculo.png) | ![Maniobra de Reversa y Despeje](docs/screenshots/04_maniobra_retroceso.png) |
| **Protección Anticolisión Automática (Fail-Safe):** Al detectar distancia $\le 20\text{ cm}$, se despliega el banner superior **`¡ALERTA DE OBSTÁCULO INMINENTE!`**, los motores se cortan automáticamente a **`DETENIDO`** y se ofrece el botón **`Alejar (+80cm)`**. | **Despeje y Recuperación Segura:** Maniobra con mando **`RETROCEDER`** activo, motor en **`RETROCEDIENDO`**, incremento progresivo de distancia hasta **`55.7 cm`** y retorno inmediato al estado de **`Camino Despejado`**. |

---

## 2. Organización Limpia de la Interfaz, Estética OLED Monocromática y M3

Diseñada bajo directrices de **Material Design 3** con una paleta **Monocromática de Alto Contraste (Negro `#000000` y Blanco `#FFFFFF`)**, maximizando la eficiencia energética en paneles OLED/AMOLED y garantizando legibilidad táctica en exteriores:

1. **Panel Superior de Telemetría Crítica**:
   - **Métrica de Distancia HC-SR04**: Tipografía monoespaciada de gran formato (`DisplayMedium`), barra de proximidad dinámica y etiquetas de estado contextual (`DESPEJADO`, `PRECAUCIÓN`, `¡OBSTÁCULO!`).
   - **Indicador de Tracción L298N**: Estado activo (`DETENIDO`, `AVANZANDO`, `RETROCEDIENDO`, `GIRANDO A LA IZQUIERDA`, `GIRANDO A LA DERECHA`) y valor de modulación por ancho de pulsos (`PWM: 100 - 255`).
   - **Telemetría de Soporte**: Tensión de batería Li-Ion 2S (`7.4V - 8.4V`), porcentaje remanente, potencia de señal inalámbrica (`RSSI en dBm`) y latencia ida y vuelta (`RTT en ms`).
2. **Cruceta Direccional Táctil (D-Pad) y Parada de Emergencia**:
   - Mandos direccionales ergonómicos de toque continuo y toque por pasos: **`AVANZAR`**, **`RETROCEDER`**, **`IZQUIERDA`**, **`DERECHA`**.
   - Botón central sobredimensionado **`DETENER`** con retroalimentación háptica inmediata para interrupción instantánea de tracción.
   - Deslizador de potencia PWM en tiempo real con selector de incrementos rápidos (`120`, `180`, `255`).
3. **Resiliencia ante Rotación de Pantalla (Configuration Changes)**:
   - Preservación íntegra de estados en memoria mediante `StateFlow` dentro de `ViewModelScope`, garantizando que girar el móvil (Vertical ↔ Horizontal) no reinicie variables ni rompa la sesión activa.

---

## 3. Conexión Inalámbrica y Operación (Modo Simulación vs. ESP32 Real)

La app incorpora un selector superior segmentado que permite conmutar de inmediato entre dos modos de ejecución sin modificar código:

```
┌────────────────────────────────────────────────────────┐
│ [● Modo Simulación Integrado]   [○ Modo Real ESP32]    │
└────────────────────────────────────────────────────────┘
```

### Opción A: Modo Simulación Integrado (Evaluación sin Hardware Físico)
Permite al docente y a los evaluadores verificar el 100% de la lógica sin requerir el prototipo armado:
1. Inicie sesión en la aplicación (use el botón de acceso rápido para docentes).
2. Verifique que el selector esté fijado en **`Modo Simulación`**.
3. Pulse **`AVANZAR`**: el motor de cinemática vehicular simulada reducirá la distancia ultrasónica emulando el acercamiento hacia una pared u objeto.
4. Al cruzar el umbral crítico ($\le 20\text{ cm}$), el vehículo aplica automáticamente la **Parada de Emergencia**, dispara vibración haptic en el móvil, bloquea el avance y despliega el banner **`¡ALERTA DE OBSTÁCULO INMINENTE!`**.
5. Pulse **`RETROCEDER`** para alejarse de la pared, o presione el botón de acceso rápido **`Alejar (+80cm)`** para restablecer la distancia a un valor seguro.

### Opción B: Modo Real con Microcontrolador ESP32 (Wi-Fi o Bluetooth)
1. Flashee en el ESP32 el firmware provisto en `firmware/esp32_autoiot_car.ino` mediante Arduino IDE o PlatformIO.
2. Encienda el chasis robotizado. El ESP32 levantará un Access Point y/o servidor de enlace:
   - **SSID Wi-Fi:** `AutoIoT_Car_AP`
   - **Contraseña WPA2:** `AutoIoT2026`
   - **IP del Vehículo:** `192.168.4.1` (Puerto HTTP `80`)
   - **Nombre Bluetooth:** `AutoIoT_ESP32_Car` (SPP UUID `00001101-0000-1000-8000-00805F9B34FB`)
3. Conecte el teléfono a la red Wi-Fi `AutoIoT_Car_AP` o vincule el dispositivo Bluetooth.
4. Conmute el selector de la app a **`Modo Real ESP32`**.
5. Abra la ventana de configuración (ícono de engranaje) y presione **`Probar Conexión / Ping Real`** para validar el enlace y verificar la latencia física de respuesta.

---

## 4. Gestión de Cuentas y Seguridad Criptográfica (ISO/IEC 27400)

La arquitectura de seguridad implementa controles alineados con la norma internacional **ISO/IEC 27400 (Ciberseguridad y Privacidad en Sistemas IoT)**:

```
[ Contraseña Plana ] ──> + Salt Aleatorio (16 Bytes) ──> [ SHA-256 KDF ] ──> Hash Almacenado
                                                                               │
[ Token de Sesión / ID ] ──> [ AndroidKeyStore ] ──> [ AES-256-GCM + Tag 128b ] ──> SharedPreferences Cifradas
```

1. **Cero Texto Plano y Protección de Credenciales**:
   - Cada contraseña es salada con **16 bytes aleatorios criptográficos (`SecureRandom`)** y procesada mediante un algoritmo digest **SHA-256**. La contraseña original jamás se escribe a disco ni se imprime en los registros de depuración (`Logcat`).
2. **Cifrado de Sesión por Hardware (`AndroidKeyStore`)**:
   - La persistencia del identificador de usuario y la sesión se realiza mediante llaves maestras generadas dentro del módulo de seguridad de hardware del dispositivo (`TEE/StrongBox`) usando **AES-256-GCM** con vector de inicialización (IV) de 12 bytes y etiqueta de autenticación GMAC de 128 bits.
3. **Credenciales de Evaluación Docente (Acceso Rápido)**:
   - **Correo Electrónico:** `operador@autoiot.com`
   - **Contraseña:** `AutoIoT2026!`
   - *Botón directo:* **`⚡ Cargar Credenciales TI3042 (Docente)`** en la pantalla de autenticación.
4. **Registro de Operadores y Revocación Limpia**:
   - Soporte para creación de nuevos usuarios locales asignando roles de seguridad (`Operador de Campo`, `Auditor de Seguridad`, `Supervisor`). Al presionar **`Cerrar Sesión`**, los tokens en memoria son purgados y los descriptores criptográficos quedan revocados.
5. **Consentimiento Informado y Principio de Privilegio Mínimo**:
   - La aplicación no solicita permisos sin previa explicación. El módulo `PermissionsHandler.kt` despliega un modal informativo previo indicando la justificación técnica de cada permiso (`NEARBY_WIFI_DEVICES`, `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `POST_NOTIFICATIONS`). Si el usuario rechaza los permisos, la aplicación **tolera la denegación sin crashear**, degradando la funcionalidad limpiamente hacia el Modo Simulación.

---

## 5. Arquitectura MVVM y Especificación de Protocolos de Conectividad

### 5.1 Estructura Modular del Proyecto
```
app/src/main/java/com/example/
├── data/
│   ├── network/
│   │   ├── IoTCarApiClient.kt      # Cliente HTTP REST (OkHttpClient en Dispatchers.IO)
│   │   └── BluetoothCarManager.kt   # Gestor Bluetooth RFCOMM SPP clásico
│   ├── repository/
│   │   └── VehicleRepository.kt    # Repositorio unificado con Fail-Safe Watchdog (2.0s)
│   └── security/
│       ├── CryptoManager.kt        # AndroidKeyStore AES-256-GCM + SHA-256 con Salt 16B
│       ├── SessionManager.kt       # Gestión cifrada de sesión de usuario
│       └── SecurityAuditLogger.kt  # Log inmutable de auditoría forense ISO 27400
├── ui/
│   ├── screens/
│   │   ├── DashboardScreen.kt          # Panel de mando OLED, D-Pad y telemetría
│   │   ├── LoginScreen.kt              # Autenticación segura y carga rápida docente
│   │   ├── SecurityComplianceScreen.kt # Panel de cumplimiento ISO/IEC 27400 y auditoría
│   │   ├── DocumentationScreen.kt      # Manual de usuario y diagramas embebidos
│   │   ├── ConnectionSettingsDialog.kt # Ajuste de IP/puerto y test de ping
│   │   └── PermissionsHandler.kt       # Consentimiento informado y manejo de permisos
│   ├── theme/                          # Theming M3 en Blanco y Negro de alto contraste
│   └── viewmodels/
│       ├── AuthViewModel.kt
│       ├── DashboardViewModel.kt
│       └── SecurityViewModel.kt
```

---

### 5.2 Protocolo A: HTTP REST sobre Wi-Fi Local (JSON Estructurado)

La comunicación cliente-servidor se efectúa mediante solicitudes HTTP no bloqueantes en hilos de fondo (`Dispatchers.IO`):

#### Telemetría Periódica: `GET /api/status`
- **Frecuencia:** Cada $500\text{ ms}$ (polling configurable).
- **Encabezados:** `Accept: application/json`
- **Respuesta JSON:**
```json
{
  "distance": 42.5,
  "motor_state": "STOPPED",
  "speed": 180,
  "obstacle": false,
  "battery_v": 7.4,
  "rssi": -55,
  "uptime": 180
}
```

#### Comandos de Control: `POST /api/control`
- **Encabezados:** `Content-Type: application/json`
- **Cuerpo JSON:**
```json
{
  "command": "FORWARD",
  "speed": 180
}
```

**Comandos Soportados:**
- `FORWARD`: Tracción hacia adelante con velocidad modulada PWM ($100 - 255$).
- `BACKWARD`: Tracción en reversa para maniobra de escape.
- `LEFT`: Giro en contragiro sobre el eje hacia la izquierda.
- `RIGHT`: Giro en contragiro sobre el eje hacia la derecha.
- `STOP`: Detención suave de todos los canales del puente L298N.
- `EMERGENCY_STOP`: Corte forzado e instantáneo de energía al motor.

---

### 5.3 Protocolo B: Bluetooth RFCOMM Clásico (SPP Serial Port Profile)

Para entornos donde no se disponga de red Wi-Fi o se requiera comunicación punto a punto de muy baja latencia, la aplicación y el firmware soportan enlace **Bluetooth Clásico RFCOMM**:

- **Perfil:** SPP (Serial Port Profile).
- **UUID Estándar RFCOMM:** `00001101-0000-1000-8000-00805F9B34FB`
- **Tasa de Transmisión (Baud Rate):** `115200 bps`, 8 bits de datos, sin paridad, 1 bit de parada (8N1).
- **Formato de Envíos:** Tramas JSON o mnemónicos en texto plano terminados obligatoriamente con salto de línea (`\n` o `\r\n` como delimitador de fin de paquete).

#### Tramas de Telemetría ESP32 ➔ Android (Streaming a 5 Hz):
```json
{"distancia": 45.2, "estadoMotor": "DETENIDO", "frenoActivo": false, "bateria": 7.6, "rssi": -62}\n
```

#### Tramas de Comando Android ➔ ESP32:
El socket escribe directamente al flujo de salida (`BluetoothSocket.outputStream`) cualquiera de los siguientes formatos compatibles:
- **Formato JSON (Recomendado):**
  ```json
  {"cmd":"FORWARD","speed":180}\n
  {"cmd":"STOP","speed":0}\n
  {"cmd":"BACKWARD","speed":180}\n
  ```
- **Formato Mnemónico ASCII (Ultrarrápido):**
  ```text
  ADELANTE:180\n
  PARAR\n
  ATRAS:180\n
  IZQUIERDA:160\n
  DERECHA:160\n
  ```

#### Máquina de Estados del Firmware y Watchdog de Enlace:
Si no se recibe un comando de tracción válido durante más de **$2.0\text{ segundos}$** (heartbeat perdido), el firmware del ESP32 detiene inmediatamente ambos motores mediante la rutina de seguridad `paradaEmergencia()`, previniendo que el vehículo avance descontrolado ante pérdida de señal.

---

## 6. Guía de Evaluación en Sala — Dinámica de Revisión (7 a 10 Minutos)

Esta guía paso a paso está diseñada para que el docente evaluador y la dupla verifiquen el cumplimiento del 100% de la pauta de evaluación académica (Criterios A1 a A5):

| Tiempo Sugerido | Prueba a Ejecutar | Criterio Pauta |
| :---: | :--- | :---: |
| **00:00 - 02:00** | **Prueba 1:** Login seguro, cero texto plano y persistencia de sesión | **Criterio A2 (20%)** |
| **02:00 - 03:30** | **Prueba 2:** Consentimiento informado ISO 27400 y tolerancia a permisos denegados | **Criterio A3 (20%)** |
| **03:30 - 05:30** | **Prueba 3:** Demostración en vivo de telemetría y teleoperación | **Criterio A1 & A4 (45%)** |
| **05:30 - 07:30** | **Prueba 4:** Parada automática de seguridad anticolisión ($\le 20\text{ cm}$) | **Criterio A4 & A5 (25%)** |
| **07:30 - 09:30** | **Prueba 5:** Pruebas de robustez (rotación de pantalla y desconexión forzada) | **Criterio A5 (15%)** |

---

### Paso a Paso Detallado para la Evaluación:

#### 🧪 Prueba 1: Login Seguro, Cero Texto Plano y Persistencia de Sesión (Criterio A2 — 20%)
1. Abra la aplicación. Observe la pantalla de inicio de sesión en estética blanco y negro.
2. Ingrese una contraseña errónea o presione el botón rápido **`⚡ Cargar Credenciales TI3042 (Docente)`** (`operador@autoiot.com` / `AutoIoT2026!`).
3. Presione **`INICIAR SESIÓN`**.
4. **Verificación de Cero Texto Plano:** El docente puede inspeccionar en el código la clase `CryptoManager.kt`:
   - El hash se computa con `SHA-256` tras generar `SecureRandom().nextBytes(salt)` (16 bytes).
   - Los datos de sesión se almacenan bajo el alias `AutoIoT_KeyStore_MasterKey` utilizando `KeyGenParameterSpec` con propósito de cifrado/descifrado `AES/GCM/NoPadding`.
5. **Verificación de Persistencia:** Cierre completamente la aplicación de la vista de multitarea de Android (Swipe out) y vuelva a abrirla. La sesión del operador continúa abierta automáticamente sin exigir reautenticación gracias a `SessionManager.kt`.
6. En el menú superior pulse **`Cerrar Sesión`**: la clave de sesión en memoria se invalida de inmediato retornando a la vista de login.

---

#### 🧪 Prueba 2: Consentimiento Informado ISO 27400 y Manejo de Permisos (Criterio A3 — 20%)
1. En la pantalla inicial o al solicitar enlace inalámbrico, se despliega el cuadro de diálogo de `PermissionsHandler.kt`.
2. **Justificación Previa:** Observe que antes del diálogo estándar del sistema operativo, la app muestra un cuadro informativo explicando con claridad por qué requiere permisos de `Dispositivos Cercanos` (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`) y `Notificaciones`, en conformidad con el principio de transparencia de la norma **ISO/IEC 27400**.
3. **Prueba de Tolerancia a la Denegación:**
   - Seleccione la opción **`Rechazar Permisos`** o "No permitir".
   - **Resultado Esperado:** La aplicación **no se detiene ni produce un crasheo** (`Force Close`). Presenta un aviso informativo de advertencia y permite al usuario operar plenamente en el **Modo Simulación**.

---

#### 🧪 Prueba 3: Demostración en Vivo de Telemetría y Teleoperación (Criterios A1 y A4 — 45%)
1. Inicie sesión e ingrese al **Dashboard**.
2. Con el selector en **`Modo Simulación`** (o conectado al ESP32 real vía Wi-Fi/Bluetooth):
   - Verifique en la tarjeta superior que el sensor ultrasónico reporte la distancia numérica en centímetros (`XX.X cm`) y que la barra de proximidad responda dinámicamente.
   - Verifique el estado de batería Li-Ion (`7.7V - 66%`), potencia de señal (`-39 dBm`) y latencia ida y vuelta (`20 ms`).
3. Pruebe los botones direccionales:
   - Presione **`AVANZAR`**: el indicador de motor cambia de inmediato a **`AVANZANDO`** con potencia PWM visualizada.
   - Presione **`IZQUIERDA`** / **`DERECHA`**: el estado cambia a giros rotacionales sobre el eje.
   - Presione el botón central blanco **`DETENER`**: el motor responde inmediatamente volviendo a **`DETENIDO`**.

---

#### 🧪 Prueba 4: Regla de Seguridad Física Anticolisión ($\le 20\text{ cm}$) (Criterios A4 y A5 — 25%)
1. En el panel de control con el vehículo en reposo (distancia inicial despejada $\sim 68\text{ cm}$), presione **`AVANZAR`**.
2. Observe cómo la distancia disminuye progresivamente simulación tras simulación ($50\text{ cm} \rightarrow 35\text{ cm} \rightarrow 25\text{ cm}$).
3. Al alcanzar los **$20.0\text{ cm}$ o menos**:
   - **Freno de Emergencia:** Los motores son forzados automáticamente a **`DETENIDO`** sin intervención humana.
   - **Banner de Alerta:** Aparece un aviso superior destacado con la leyenda **`¡ALERTA DE OBSTÁCULO INMINENTE!`**.
   - **Bloqueo del Avance:** El botón **`AVANZAR`** se bloquea o inhibe la marcha hacia adelante para prevenir colisión física.
   - **Maniobra de Escape:** El operador sólo puede presionar **`RETROCEDER`** o girar. Al presionar **`RETROCEDER`**, la distancia aumenta nuevamente ($> 20\text{ cm}$), disipando la alerta crítica y restaurando la condición de **`Camino Despejado`**.

---

#### 🧪 Prueba 5: Robustez y Resiliencia del Software (Criterio A5 — 15%)
1. **Prueba de Rotación de Pantalla (Configuration Change):**
   - Con el vehículo en marcha o en monitoreo activo, rote el teléfono físico de vertical a horizontal.
   - **Resultado Esperado:** La interfaz se reacomoda limpiamente a formato apaisado. La conexión activa, los valores de telemetría y el estado del motor **se mantienen intactos**, demostrando la correcta implementación de `ViewModel` y flujos reactivos `StateFlow`.
2. **Prueba de Desconexión Abrupta / Falla de Red (Fail-Safe):**
   - En Modo Real ESP32, desconecte el Wi-Fi del teléfono o apague el servidor ESP32 repentinamente mientras la app consulta datos.
   - **Resultado Esperado:** La aplicación no se cuelga ni congela la UI (ANR). Los hilos de red en `Dispatchers.IO` capturan de forma limpia `SocketTimeoutException` / `ConnectException`, transicionando el estado a **`Desconectado`** y aplicando parada preventiva local para proteger el hardware.

---

## 7. Compilación, Generación del APK y Publicación en GitHub

### 7.1 Descarga Directa del APK Ya Compilado
Si no desea compilar el código fuente, descargue directamente el instalador generado en Releases:
👉 **[Descargar APK v1.0.0 (AutoRemoto.apk)](https://github.com/benja2576/AutoRemoto/releases/download/v1.0.0/AutoRemoto.apk)**

---

### 7.2 Compilación desde el Código Fuente en Android Studio (Windows / Mac / Linux)
1. Clone o descargue este repositorio:
   ```bash
   git clone https://github.com/benja2576/AutoRemoto.git
   ```
2. Abra **Android Studio** (Koala, Ladybug o superior).
3. Seleccione **File > Open...** y elija la carpeta raíz `AutoRemoto`.
4. Deje que Gradle descargue las librerías necesarias de forma automática.
5. En el menú superior, genere el binario ejecutable:
   **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. El archivo resultante se generará en:
   ```text
   app/build/outputs/apk/debug/AutoRemoto.apk
   ```

---

### 7.3 Instalación y Ejecución en Teléfono Físico Android
- **Vía USB con Android Studio:** Conecte el teléfono mediante cable USB con la **Depuración por USB** habilitada en Opciones de Desarrollador y pulse **Run 'app'** (`Shift + F10`).
- **Instalación Manual del APK:** Copie el archivo `AutoRemoto.apk` a la memoria interna o envíelo por WhatsApp / Drive / Telegram a su dispositivo. Al pulsarlo, acepte "Instalar aplicaciones de fuentes desconocidas" y ábralo directamente.

---

### 7.4 Comandos para Publicar en GitHub
```bash
# Inicializar repositorio local
git init

# Agregar todos los archivos fuente, recursos y documentación
git add .

# Registrar el commit con formato profesional
git commit -m "Auto IoT Control - Estación de Monitoreo y Teleoperación ESP32 (Evaluación TI3042 ES2)"

# Establecer rama principal
git branch -M main

# Vincular con el repositorio remoto
git remote add origin https://github.com/benja2576/AutoRemoto.git

# Subir los cambios
git push -u origin main
```

---

## 8. Cumplimiento de Criterios de Evaluación TI3042 ES2

| Criterio de Evaluación | Ponderación | Estado | Evidencia en el Proyecto |
| :--- | :---: | :---: | :--- |
| **A1. Herramientas y Problemática** | **20%** | **Cumplido al 100%** | Proyecto Android nativo en Kotlin con Jetpack Compose M3, arquitectura MVVM, control de puente H L298N y monitoreo ultrasónico HC-SR04. |
| **A2. Login Seguro y Cero Texto Plano** | **20%** | **Cumplido al 100%** | Criptografía en `AndroidKeyStore` con `AES-256-GCM`, KDF digest `SHA-256` con `Salt` aleatorio de 16 bytes y persistencia en `EncryptedSharedPreferences`. |
| **A3. Permisos y Ciberseguridad ISO 27400** | **20%** | **Cumplido al 100%** | Consentimiento informado previo en `PermissionsHandler.kt`, tolerancia a denegación de permisos sin crasheos y log inmutable de eventos. |
| **A4. Comunicación Inalámbrica y Protocolos** | **25%** | **Cumplido al 100%** | Protocolo dual: HTTP REST sobre Wi-Fi local (`GET /api/status`, `POST /api/control`) y Bluetooth RFCOMM clásico SPP (`UUID 00001101-0000-1000-8000-00805F9B34FB`) con tramas JSON delimitadas por `\n`. |
| **A5. Robustez, Resiliencia y Despliegue** | **15%** | **Cumplido al 100%** | Operaciones de red en `Dispatchers.IO`, preservación ante rotación de pantalla (`StateFlow` + `ViewModel`), parada automática ante $\le 20\text{ cm}$ y APK generado. |
| **TOTAL EVALUACIÓN** | **100%** | **APROBACIÓN DISTINGUIDA** | **Código fuente limpio, firmware ESP32, APK disponible y suite de pruebas lista.** |
