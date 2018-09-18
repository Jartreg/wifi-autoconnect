package de.goetheschule_essen.technik.wifi_autoconnect.utils

import java.io.IOException
import java.net.URL
import java.net.URLEncoder

/**
 * A helper class to send web requests ignoring TLS verification
 */
class RequestManager {
    /**
     * Sends a POST request to the specified [url] with the given [data]
     *
     * @param url the URL to send the request to
     * @param data the data to send in the request body
     *
     * @return whether the request was successful
     */
    @Throws(IOException::class)
    fun post(url: URL, data: Map<String, String>): Boolean {
        val builder = ProcessBuilder(
                "/usr/bin/wget",
                "--output-document=-",
                "--no-check-certificate",
                "--post-data=${createQuery(data)}",
                url.toString())

        val process = builder
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start()

        val exitCode = process.waitFor()
        println("wget exited with $exitCode")

        return exitCode == 0
    }

    /**
     * Creates a query string from a map of values
     */
    private fun createQuery(data: Map<String, String>): String {
        val charset = "UTF-8"
        return data
                .map { "${URLEncoder.encode(it.key, charset)}=${URLEncoder.encode(it.value, charset)}" }
                .joinToString("&")
    }
}