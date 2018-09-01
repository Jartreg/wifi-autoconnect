package de.goetheschule_essen.technik.wifi_autoconnect

import de.goetheschule_essen.technik.wifi_autoconnect.networkmanager.NetworkManagerHelper
import org.freedesktop.dbus.DBusConnection
import org.freedesktop.dbus.exceptions.DBusExecutionException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

fun main(args: Array<String>) {
    val config = getConfig()
    if (config == null) { // exit if the configuration could not be read
        System.exit(1)
        return
    }

    // get access to the system bus and the NetworkManager object
    val dbus = DBusConnection.getConnection(DBusConnection.SYSTEM)
    val networkManager = NetworkManagerHelper(dbus)

    // ensure that connectivity checking is available
    // exit otherwise
    if (!networkManager.ensureConnectivityCheck()) {
        System.err.println("Connectivity check unavailable")
        System.exit(1)
        return
    }

    // start observing the connectivity state
    val authenticationManager = AuthenticationManager(networkManager, config)
    val observer = ConnectivityObserver(networkManager, authenticationManager::authenticate)
    observer.observing = true
}

/**
 * Reads the configuration or generates a default one
 *
 * @return the [Configuration] object or `null` if the file could not be read
 */
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

/**
 * Returns whether connectivity checking is available and tries to enable it if possible
 */
fun NetworkManagerHelper.ensureConnectivityCheck(): Boolean {
    try {
        // check whether it is available
        if (!ConnectivityCheckAvailable)
            return false

        // ensure that it is enabled
        if (!ConnectivityCheckEnabled)
            ConnectivityCheckEnabled = true
    } catch (ex: DBusExecutionException) {
        return false
    }

    // connectivity checking is available and enabled
    return true
}

