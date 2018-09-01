package de.goetheschule_essen.technik.wifi_autoconnect.networkmanager

import org.freedesktop.DBus
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusConnection
import org.freedesktop.dbus.DBusSigHandler
import org.freedesktop.dbus.UInt32

const val networkManagerBus = "org.freedesktop.NetworkManager"
const val networkManagerInterfaceName = "org.freedesktop.NetworkManager"
const val networkManagerPath = "/org/freedesktop/NetworkManager"

const val ConnectivityProperty = "Connectivity"
const val ConnectivityCheckAvailableProperty = "ConnectivityCheckAvailable"
const val ConnectivityCheckEnabledProperty = "ConnectivityCheckEnabled"

class NetworkManagerHelper(private val dbus: DBusConnection) {
    val networkManager = dbus.getRemoteObject(networkManagerBus, networkManagerPath, NetworkManager::class.java)
    val networkManagerProperties: DBus.Properties = dbus.getRemoteObject(networkManagerBus, networkManagerPath, DBus.Properties::class.java)

    /**
     * Re-check the network connectivity state
     */
    fun CheckConnectivity(): NMConnectivityState {
        val result = networkManager.CheckConnectivity()
        return NMConnectivityState.fromUInt32(result)
    }

    /**
     * The result of the last connectivity check
     */
    val Connectivity: NMConnectivityState
        get() {
            val value = networkManagerProperties.Get<UInt32>(networkManagerInterfaceName, ConnectivityProperty)
            return NMConnectivityState.fromUInt32(value)
        }

    /**
     * Indicates whether connectivity checking service has been configured
     */
    val ConnectivityCheckAvailable: Boolean
        get() = networkManagerProperties.Get(networkManagerInterfaceName, ConnectivityCheckAvailableProperty)

    /**
     * Indicates whether connectivity checking is enabled
     */
    var ConnectivityCheckEnabled: Boolean
        get() = networkManagerProperties.Get(networkManagerInterfaceName, ConnectivityCheckEnabledProperty)
        set(value) = networkManagerProperties.Set(networkManagerInterfaceName, ConnectivityCheckEnabledProperty, value)

    /**
     * Adds a handler for property changes
     */
    fun addPropertyChangedHandler(handler: (DBus.Properties.PropertiesChanged) -> Unit) {
        dbus.addSigHandler(DBus.Properties.PropertiesChanged::class.java, networkManagerBus, networkManager, handler)
    }

    /**
     * Removes a handler for property changes
     */
    fun removePropertyChangedHandler(handler: (DBus.Properties.PropertiesChanged) -> Unit) {
        dbus.removeSigHandler(DBus.Properties.PropertiesChanged::class.java, networkManagerBus, networkManager, handler)
    }
}

/**
 * Describes the state of network connectivity
 */
enum class NMConnectivityState {
    UNKNOWN,
    NONE,
    PORTAL,
    LIMITED,
    FULL;

    companion object {
        @JvmStatic
        fun fromUInt32(value: UInt32) = NMConnectivityState.values()[value.toInt()]
    }
}