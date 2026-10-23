/*
  =============================================================================
  Auto IoT Control - Firmware Oficial ESP32
  Evaluación: TI3042 ES2 - Sistema de Control y Telemetría Vehicular IoT
  =============================================================================
  Hardware soportado:
  - Microcontrolador: ESP32 Dev Module / ESP-WROOM-32 / NodeMCU-32S
  - Driver de Motores: Puente H L298N (Control de 2 Motores DC 6V-12V)
  - Sensor de Distancia: Sensor Ultrasónico HC-SR04 (Trig / Echo)
  - Alimentación: Batería Li-Ion 2S (7.4V nominal)
  
  Medidas de Seguridad (ISO/IEC 27400):
  1. Watchdog Hardware de Parada Segura: Si no se reciben paquetes en 2 segundos,
     el ESP32 frena los motores automáticamente para evitar colisiones.
  2. Parada Preventiva por Sensor: Si la distancia al frente es <= 15 cm, el ESP32
     rechaza la orden de avance y detiene los motores de inmediato.
  3. Autenticación de Comandos por Token: Valida el header o campo "auth_token".
  =============================================================================
*/

#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>

// ==========================================
// 1. CONFIGURACIÓN DE RED WI-FI
// ==========================================
// MODO AP (Punto de Acceso Autónomo sin router externo):
#define WIFI_AP_MODE 1

#if WIFI_AP_MODE
  const char* AP_SSID = "AutoIoT_Car_AP";
  const char* AP_PASS = "AutoIoT2026"; // Mínimo 8 caracteres WPA2-PSK
  IPAddress local_IP(192, 168, 4, 1);
  IPAddress gateway(192, 168, 4, 1);
  IPAddress subnet(255, 255, 255, 0);
#else
  // MODO ESTACIÓN (Conectado a tu red Wi-Fi de hogar/laboratorio):
  const char* STA_SSID = "TU_WIFI_SSID";
  const char* STA_PASS = "TU_WIFI_PASSWORD";
#endif

// Token de autenticación de seguridad (debe coincidir con la app Android)
const char* AUTH_TOKEN_EXPECTED = "AutoIoT-SecToken-2026";

// ==========================================
// 2. DEFINICIÓN DE PINES (GPIO ESP32)
// ==========================================
// Driver Puente H L298N
#define PIN_ENA 25  // PWM Velocidad Motor Izquierdo
#define PIN_IN1 26  // Dirección Izquierda +
#define PIN_IN2 27  // Dirección Izquierda -
#define PIN_IN3 14  // Dirección Derecha +
#define PIN_IN4 12  // Dirección Derecha -
#define PIN_ENB 13  // PWM Velocidad Motor Derecho

// Sensor Ultrasónico HC-SR04
#define PIN_TRIG 5   // Trigger (Salida)
#define PIN_ECHO 18  // Echo (Entrada con divisor de tensión 5V -> 3.3V)

// LED indicador de estado
#define PIN_LED 2

// Canales PWM en ESP32
#define PWM_FREQ 1000
#define PWM_RES 8 // Resolución de 8 bits (0 - 255)
#define PWM_CH_LEFT 0
#define PWM_CH_RIGHT 1

// Parámetros de Seguridad y Watchdog
#define WATCHDOG_TIMEOUT_MS 2000L   // 2 segundos sin comando = Parada segura
#define CRITICAL_OBSTACLE_CM 15.0f  // Distancia crítica de parada preventiva

// ==========================================
// 3. VARIABLES GLOBALES
// ==========================================
WebServer server(80);

String currentMotorState = "STOPPED";
int currentSpeed = 180; // PWM 0 - 255
float currentDistanceCm = 50.0f;
bool isObstacleCritical = false;
unsigned long lastCommandTime = 0;
unsigned long lastDistanceReadTime = 0;

// ==========================================
// 4. FUNCIONES DE LECTURA DE SENSORES
// ==========================================
float readUltrasonicDistance() {
  digitalWrite(PIN_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(PIN_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(PIN_TRIG, LOW);

  // Leer tiempo de pulso en microsegundos (timeout de 25ms = ~4 metros)
  long duration = pulseIn(PIN_ECHO, HIGH, 25000);
  if (duration == 0) {
    return 200.0f; // Fuera de rango o sin eco
  }

  // Velocidad del sonido = 343 m/s = 0.0343 cm/us -> (duration * 0.0343) / 2
  float distance = (duration * 0.0343f) / 2.0f;
  return distance;
}

// ==========================================
// 5. CONTROL DE MOTORES L298N
// ==========================================
void setMotorOutputs(int in1, int in2, int in3, int in4, int speed) {
  digitalWrite(PIN_IN1, in1);
  digitalWrite(PIN_IN2, in2);
  digitalWrite(PIN_IN3, in3);
  digitalWrite(PIN_IN4, in4);

  ledcWrite(PWM_CH_LEFT, speed);
  ledcWrite(PWM_CH_RIGHT, speed);
}

void executeMotorCommand(String command, int speed) {
  speed = constrain(speed, 0, 255);
  currentSpeed = speed;

  // Medida de seguridad preventiva por proximidad
  if (command == "FORWARD" && currentDistanceCm <= CRITICAL_OBSTACLE_CM) {
    safeStop();
    Serial.println("[SEGURIDAD] Orden de AVANCE rechazada por obstáculo.");
    return;
  }

  if (command == "FORWARD") {
    // Motores hacia adelante
    setMotorOutputs(HIGH, LOW, HIGH, LOW, speed);
    currentMotorState = "FORWARD";
  } else if (command == "BACKWARD") {
    // Motores hacia atrás
    setMotorOutputs(LOW, HIGH, LOW, HIGH, speed);
    currentMotorState = "BACKWARD";
  } else if (command == "LEFT") {
    // Giro sobre su eje a la izquierda
    setMotorOutputs(LOW, HIGH, HIGH, LOW, speed);
    currentMotorState = "LEFT";
  } else if (command == "RIGHT") {
    // Giro sobre su eje a la derecha
    setMotorOutputs(HIGH, LOW, LOW, HIGH, speed);
    currentMotorState = "RIGHT";
  } else {
    safeStop();
  }

  lastCommandTime = millis();
}

void safeStop() {
  setMotorOutputs(LOW, LOW, LOW, LOW, 0);
  currentMotorState = "STOPPED";
}

// ==========================================
// 6. CONTROLADORES HTTP REST (API)
// ==========================================

void setCorsHeaders() {
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type, X-IoT-Auth-Token");
}

void handleOptions() {
  setCorsHeaders();
  server.send(204);
}

void handlePing() {
  setCorsHeaders();
  StaticJsonDocument<128> doc;
  doc["pong"] = true;
  doc["timestamp"] = millis();
  doc["device"] = "ESP32_AutoIoT";

  String json;
  serializeJson(doc, json);
  server.send(200, "application/json", json);
}

void handleStatus() {
  setCorsHeaders();
  StaticJsonDocument<256> doc;
  doc["distance"] = currentDistanceCm;
  doc["motor_state"] = currentMotorState;
  doc["speed"] = currentSpeed;
  doc["obstacle"] = isObstacleCritical;
  doc["battery_v"] = 7.4f; // O lectura analógica si se conecta divisor de batería a GPIO 34
  doc["rssi"] = WiFi.RSSI();
  doc["uptime"] = millis() / 1000;

  String json;
  serializeJson(doc, json);
  server.send(200, "application/json", json);
}

void handleControl() {
  setCorsHeaders();
  if (server.method() != HTTP_POST) {
    server.send(405, "application/json", "{\"error\":\"Método no permitido\"}");
    return;
  }

  String body = server.arg("plain");
  StaticJsonDocument<256> doc;
  DeserializationError err = deserializeJson(doc, body);

  if (err) {
    server.send(400, "application/json", "{\"error\":\"JSON malformado\"}");
    return;
  }

  // Validación de Token de Seguridad (ISO/IEC 27400)
  const char* token = doc["auth_token"] | "";
  String headerToken = server.header("X-IoT-Auth-Token");
  if (String(token) != AUTH_TOKEN_EXPECTED && headerToken != AUTH_TOKEN_EXPECTED) {
    server.send(401, "application/json", "{\"error\":\"Token de autenticación inválido\"}");
    return;
  }

  String cmd = doc["command"] | "STOP";
  int spd = doc["speed"] | 180;

  executeMotorCommand(cmd, spd);

  StaticJsonDocument<128> resp;
  resp["success"] = true;
  resp["state"] = currentMotorState;
  resp["speed"] = currentSpeed;

  String respStr;
  serializeJson(resp, respStr);
  server.send(200, "application/json", respStr);
}

// ==========================================
// 7. SETUP Y BUCLE PRINCIPAL
// ==========================================
void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\n[INICIANDO] Auto IoT Control ESP32...");

  // Configurar Pines
  pinMode(PIN_IN1, OUTPUT);
  pinMode(PIN_IN2, OUTPUT);
  pinMode(PIN_IN3, OUTPUT);
  pinMode(PIN_IN4, OUTPUT);
  pinMode(PIN_ENA, OUTPUT);
  pinMode(PIN_ENB, OUTPUT);

  pinMode(PIN_TRIG, OUTPUT);
  pinMode(PIN_ECHO, INPUT);
  pinMode(PIN_LED, OUTPUT);

  // Configurar PWM con ledc en ESP32
  ledcSetup(PWM_CH_LEFT, PWM_FREQ, PWM_RES);
  ledcSetup(PWM_CH_RIGHT, PWM_FREQ, PWM_RES);
  ledcAttachPin(PIN_ENA, PWM_CH_LEFT);
  ledcAttachPin(PIN_ENB, PWM_CH_RIGHT);

  safeStop();

  // Iniciar Wi-Fi
#if WIFI_AP_MODE
  WiFi.mode(WIFI_AP);
  WiFi.softAPConfig(local_IP, gateway, subnet);
  WiFi.softAP(AP_SSID, AP_PASS);
  Serial.print("[WIFI AP] Creado: ");
  Serial.println(AP_SSID);
  Serial.print("[IP]: ");
  Serial.println(WiFi.softAPIP());
#else
  WiFi.mode(WIFI_STA);
  WiFi.begin(STA_SSID, STA_PASS);
  Serial.print("[WIFI STA] Conectando");
  while (WiFi.status() != WL_CONNECTED) {
    delay(400);
    Serial.print(".");
  }
  Serial.println("\n[CONECTADO] IP: " + WiFi.localIP().toString());
#endif

  // Configurar Endpoints del Servidor Web
  server.on("/api/ping", HTTP_OPTIONS, handleOptions);
  server.on("/api/ping", HTTP_GET, handlePing);

  server.on("/api/status", HTTP_OPTIONS, handleOptions);
  server.on("/api/status", HTTP_GET, handleStatus);

  server.on("/api/control", HTTP_OPTIONS, handleOptions);
  server.on("/api/control", HTTP_POST, handleControl);

  // Recolectar headers de autenticación
  const char* headerKeys[] = {"X-IoT-Auth-Token"};
  server.collectHeaders(headerKeys, 1);

  server.begin();
  Serial.println("[HTTP] Servidor REST activo en puerto 80.");

  lastCommandTime = millis();
}

void loop() {
  server.handleClient();

  // Lectura periódica de distancia cada 100 ms
  if (millis() - lastDistanceReadTime >= 100) {
    lastDistanceReadTime = millis();
    currentDistanceCm = readUltrasonicDistance();
    isObstacleCritical = (currentDistanceCm <= CRITICAL_OBSTACLE_CM);

    // Parada preventiva inmediata si se detecta obstáculo al avanzar
    if (isObstacleCritical && currentMotorState == "FORWARD") {
      safeStop();
      digitalWrite(PIN_LED, HIGH); // LED de alerta encendido
      Serial.println("[PARADA SEGURA] Obstáculo detectado. Motores detenidos.");
    } else {
      digitalWrite(PIN_LED, LOW);
    }
  }

  // WATCHDOG DE FAIL-SAFE (ISO/IEC 27400):
  // Si transcurre más del tiempo límite sin recibir comandos y el motor no está detenido, parar.
  if (currentMotorState != "STOPPED" && (millis() - lastCommandTime > WATCHDOG_TIMEOUT_MS)) {
    safeStop();
    Serial.println("[WATCHDOG] Timeout de comunicación. Parada de emergencia ejecutada.");
  }
}
