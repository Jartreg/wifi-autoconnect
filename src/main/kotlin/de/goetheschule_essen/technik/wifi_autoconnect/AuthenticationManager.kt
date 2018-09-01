package de.goetheschule_essen.technik.wifi_autoconnect

import de.goetheschule_essen.technik.wifi_autoconnect.networkmanager.NMConnectivityState
import de.goetheschule_essen.technik.wifi_autoconnect.networkmanager.NetworkManagerHelper
import java.util.concurrent.*

class AuthenticationManager(private val networkManager: NetworkManagerHelper, private val configuration: Configuration) {
    private var authenticating = false
    private val executor = Executors.newSingleThreadScheduledExecutor()

    /**
     * Authenticates the user.
     * This happens on a separate thread and only once at a time.
     */
    fun authenticate() {
        synchronized(this) {
            if (authenticating) return
            // Set authenticating to true to block concurrent authentications
            authenticating = true
        }

        scheduleAuthentication()
    }

    private fun scheduleAuthentication(delay: Long = 0) {
        if (delay == 0L)
            executor.execute(::runAuthentication)
        else
            executor.schedule(::runAuthentication, delay, TimeUnit.SECONDS)
    }

    private fun runAuthentication() {
        // Submit credentials
        submitAuthentication()

        // Check for connectivity
        if (networkManager.CheckConnectivity() == NMConnectivityState.PORTAL) {
            scheduleAuthentication(5) // Retry after 5 seconds
        } else {
            // Authenticated successfully
            // Reset authenticating to false to allow later authentications
            synchronized(this) {
                authenticating = false
            }
        }
    }

    private fun submitAuthentication() {
        TODO()
    }
}