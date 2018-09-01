package de.goetheschule_essen.technik.wifi_autoconnect

import java.io.*
import java.util.*

/**
 * The configuration needed to authenticate the user
 */
data class Configuration(
        val user: String,
        val password: String
) {
    fun saveTo(props: Properties) {
        props["user"] = user
        props["password"] = password
    }

    @Throws(IOException::class)
    fun saveTo(writer: Writer) {
        val props = Properties()
        saveTo(props)
        props.store(writer, null)
    }

    @Throws(IOException::class)
    fun saveTo(file: File) = saveTo(file.writer())
}

/**
 * Parses the specified file into a [Configuration] object.
 *
 * @throws FileNotFoundException if the file is not found or a directory
 * @throws IOException when an IO error occurs
 */
@Throws(IOException::class, FileNotFoundException::class)
fun parseConfigFile(file: File): Configuration? {
    if(!file.isFile) { // throw if the file is not found
        throw FileNotFoundException(file.path)
    }

    // parse the properties file
    val props = Properties()
    props.load(file.reader())

    // read the values
    val user = props["user"] as? String
    val password = props["password"] as? String

    return if(user != null && password != null) Configuration(user, password) else null
}

/**
 * Saves the default configuration to the specified file.
 * The default configuration contains the placeholders 'user' for the user name and 'password' for the password.
 */
fun saveDefaultConfig(file: File) {
    Configuration("user", "password").saveTo(file)
}