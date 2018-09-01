package de.goetheschule_essen.technik.wifi_autoconnect

import de.goetheschule_essen.technik.wifi_autoconnect.networkmanager.NetworkManagerHelper
import org.freedesktop.dbus.DBusConnection
import org.freedesktop.dbus.exceptions.DBusExecutionException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

fun main(args: Array<String>) {
    val config = getConfig()
    if (config == null) {
        System.exit(1)
        return
    }

    val dbus = DBusConnection.getConnection(DBusConnection.SYSTEM)
    val networkManager = NetworkManagerHelper(dbus)

    if (!networkManager.ensureConnectivityCheck()) {
        System.err.println("Connectivity check unavailable")
        System.exit(1)
        return
    }

    val authenticationManager = AuthenticationManager(networkManager, config)
    val observer = ConnectivityObserver(networkManager, authenticationManager::authenticate)

    observer.observing = true
}

fun getConfig(): Configuration? {
    val configFile = File("/etc/wifi-autoconnect.properties")
    var config: Configuration? = null

    try {
        config = parseConfigFile(configFile)
    } catch (e: FileNotFoundException) {
        saveDefaultConfig(configFile)
        System.err.println("No configuration file found. Generated default ${configFile.path}")
    } catch (e: IOException) {
        e.printStackTrace()
    }

    if (config == null) {
        System.err.println("Couldn't read the configuration file ${configFile.path}")
    }

    return config
}

fun NetworkManagerHelper.ensureConnectivityCheck(): Boolean {
    try {
        if (!ConnectivityCheckAvailable)
            return false

        if (!ConnectivityCheckEnabled)
            ConnectivityCheckEnabled = true
    } catch (ex: DBusExecutionException) {
        return false
    }

    return true
}

