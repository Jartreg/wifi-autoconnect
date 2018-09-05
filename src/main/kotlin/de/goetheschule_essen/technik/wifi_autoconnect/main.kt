package de.goetheschule_essen.technik.wifi_autoconnect

import de.goetheschule_essen.technik.wifi_autoconnect.networkmanager.NMConnectivityState
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

    // start observing the connectivity state
    val authenticationManager = AuthenticationManager(networkManager, config)
    val observer = ConnectivityObserver(networkManager, authenticationManager::authenticate)
    observer.observing = true

    // Check whether the user already needs to be authenticated
    if (networkManager.Connectivity == NMConnectivityState.PORTAL) {
        authenticationManager.authenticate()
    }
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

