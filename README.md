# Winterfell Companion (Android)

Android-WebView-App für die vorhandene Website **http://winterfell-mc.com/**.
Alle Serverfunktionen (Login über /wstats link, Statistiken, persönliche Profile,
Farmverzeichnis und BlueMap) bleiben auf dem bestehenden WinterfellStats-Server.

Die App fordert nur **android.permission.INTERNET** an.
Für Winterfell wird HTTP ausdrücklich zugelassen; die damit verbundenen
Risiken beim Übertragen von Login-Codes und Cookies bleiben bestehen.
Zertifikatsfehler werden nicht ignoriert.

## APK herunterladen

Unter **Actions > Android APK bauen > neuester erfolgreicher Workflow-Lauf >
Artifacts > Winterfell-Android-APK** herunterladen. Das ZIP enthält
**app-debug.apk**, signiert mit einem Android-Debugschlüssel.

Die Anwendung ist ein Testbuild und kein Play-Store-Release.
