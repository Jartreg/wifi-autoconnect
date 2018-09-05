package de.goetheschule_essen.technik.wifi_autoconnect.utils

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * A helper class to send web requests ignoring TLS verification
 */
class RequestManager {
    private val sslSocketFactory: SSLSocketFactory
    private val hostnameVerifier: HostnameVerifier

    init {
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) { }
            override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) { }
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        }
        val context = SSLContext.getInstance("TLS")
        context.init(null, arrayOf(trustManager), SecureRandom())
        sslSocketFactory = context.socketFactory

        hostnameVerifier = HostnameVerifier { _, _ -> true }
    }

    /**
     * Sends a POST request to the specified [url] with the given [data]
     *
     * @param url the URL to send the request to
     * @param data the data to send in the request body
     */
    @Throws(IOException::class)
    fun post(url: URL, data: Map<String, String>): String {
        val connection = url.openConnection()

        if (connection is HttpsURLConnection) { // Necessary to allow insecure requests
            connection.sslSocketFactory = sslSocketFactory
            connection.hostnameVerifier = hostnameVerifier
        }

        if (connection is HttpURLConnection) {
            connection.requestMethod = "POST"
        }
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        connection.doOutput = true
        connection.connect()

        connection.getOutputStream().use {
            it.write(createQuery(data).toByteArray())
        }

        return connection.getInputStream().use {
            it.reader().readText()
        }
    }

    /**
     * Creates a query string from a map of values
     */
    private fun createQuery(data: Map<String, String>): String {
        val charset = Charsets.UTF_8
        return data
                .map { "${URLEncoder.encode(it.key, charset)}=${URLEncoder.encode(it.key, charset)}" }
                .joinToString("&")
    }
}