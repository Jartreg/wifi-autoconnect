package de.goetheschule_essen.technik.wifi_autoconnect

import de.goetheschule_essen.technik.wifi_autoconnect.utils.NMConnectivityState
import de.goetheschule_essen.technik.wifi_autoconnect.utils.NetworkManagerHelper
import de.goetheschule_essen.technik.wifi_autoconnect.utils.RequestManager
import java.net.URL
import java.util.concurrent.*

class AuthenticationManager(private val networkManager: NetworkManagerHelper, configuration: Configuration) {
    private var authenticating = false
    private val executor = Executors.newSingleThreadScheduledExecutor()

    private val requestManager = RequestManager()
    private val requestUrl = URL("https://web.auto.configed.certificate/aaa/wba_login.html")
    private val requestData = mapOf(
            "fname" to "wba_login",
            "username" to configuration.user,
            "key" to configuration.password
    )

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
        var success = false

        try {
            // Submit credentials
            requestManager.post(requestUrl, requestData)

            // Check for connectivity
            success = networkManager.CheckConnectivity() == NMConnectivityState.PORTAL
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (success) { // On failure: retry after 5 seconds
            scheduleAuthentication(5)
        } else {
            // Authenticated successfully
            // Reset authenticating to false to allow later authentications
            synchronized(this) {
                authenticating = false
            }
        }
    }
}