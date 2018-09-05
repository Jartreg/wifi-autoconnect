package de.goetheschule_essen.technik.wifi_autoconnect.utils

import org.freedesktop.DBus
import org.freedesktop.NetworkManager
import org.freedesktop.dbus.DBusConnection
import org.freedesktop.dbus.UInt32

const val networkManagerBus = "org.freedesktop.NetworkManager"
const val networkManagerInterfaceName = "org.freedesktop.NetworkManager"
const val networkManagerPath = "/org/freedesktop/NetworkManager"

const val ConnectivityProperty = "Connectivity"
const val ConnectivityCheckAvailableProperty = "ConnectivityCheckAvailable"
const val ConnectivityCheckEnabledProperty = "ConnectivityCheckEnabled"

/**
 * A helper class for easier access to properties on the NetworkManager interface
 * Also provides automatic conversion of [NMConnectivityState] values
 */
class NetworkManagerHelper(private val dbus: DBusConnection) {
    /**
     * The remote org.freedesktop.NetworkManager interface on the NetworkManager object
     * used to invoke methods on the NetworkManager interface
     */
    private val networkManager: NetworkManager = dbus.getRemoteObject(networkManagerBus, networkManagerPath, NetworkManager::class.java)

    /**
     * The remote org.freedesktop.DBus.Properties interface on the NetworkManager object
     * used to get and modify properties
     */
    private val networkManagerProperties: DBus.Properties = dbus.getRemoteObject(networkManagerBus, networkManagerPath, DBus.Properties::class.java)

    /**
     * Re-check the network connectivity state
     */
    @Suppress("FunctionName") // the name is the same as on the interface
    fun CheckConnectivity(): NMConnectivityState {
        val result = networkManager.CheckConnectivity()
        return NMConnectivityState.fromUInt32(result)
    }

    /**
     * The result of the last connectivity check
     */
    @Suppress("PropertyName") // the name is the same as on the interface
    val Connectivity: NMConnectivityState
        get() {
            val value = networkManagerProperties.Get<UInt32>(networkManagerInterfaceName, ConnectivityProperty)
            return NMConnectivityState.fromUInt32(value)
        }

    /**
     * Indicates whether connectivity checking service has been configured
     */
    @Suppress("PropertyName") // the name is the same as on the interface
    val ConnectivityCheckAvailable: Boolean
        get() = networkManagerProperties.Get(networkManagerInterfaceName, ConnectivityCheckAvailableProperty)

    /**
     * Indicates whether connectivity checking is enabled
     */
    @Suppress("PropertyName") // the name is the same as on the interface
    var ConnectivityCheckEnabled: Boolean
        get() = networkManagerProperties.Get(networkManagerInterfaceName, ConnectivityCheckEnabledProperty)
        set(value) = networkManagerProperties.Set(networkManagerInterfaceName, ConnectivityCheckEnabledProperty, value)

    /**
     * Adds a handler for property changes
     */
    fun addPropertyChangedHandler(handler: (DBus.Properties.PropertiesChanged) -> Unit) {
        dbus.addSigHandler(DBus.Properties.PropertiesChanged::class.java, networkManager, handler)
    }

    /**
     * Removes a handler for property changes
     */
    fun removePropertyChangedHandler(handler: (DBus.Properties.PropertiesChanged) -> Unit) {
        dbus.removeSigHandler(DBus.Properties.PropertiesChanged::class.java, networkManager, handler)
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
        /**
         * Converts a [UInt32] value received from a DBus remote call to an instance of [NMConnectivityState]
         *
         * @throws IndexOutOfBoundsException if [value] is not one of the expected values
         */
        @JvmStatic
        fun fromUInt32(value: UInt32) = NMConnectivityState.values()[value.toInt()]
    }
}