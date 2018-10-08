# WiFi Autoconnect

[![Build Status](https://travis-ci.com/Technik-AG/wifi-autoconnect.svg?branch=master)](https://travis-ci.com/Technik-AG/wifi-autoconnect)

Ein Service, um sich automatisch im Schul-WLAN anzumelden

## Funktionsweise

Der Service nutzt NetworkManager, um zu erkennen, ob das Gerät bereits angemeldet ist oder nicht.
Er sendet dann die Anmeldedaten automatisch ab, sobald eine Anmeldung erforderlich ist.

## Vorraussetzungen

Der Service benötigt NetworkManager und DBus und funktioniert somit nur auf Linux-basierten Betriebssystemen.

## Konfiguration

In der Datei `/etc/wifi-autoconnect.properties` muss zuvor der Benutzername und das Passwort des Benutzerkontos,
mit dem sich im Netzwerk angemeldet werden soll, eingerichtet werden (das geht nur mit `root`-Rechten).