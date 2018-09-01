package de.goetheschule_essen.technik.wifi_autoconnect

import java.io.*
import java.util.*

data class Configuration(
        val user: String,
        val password: String
) {
    fun saveTo(props: Properties) {
        props["user"] = user
        props["password"] = password
    }

    fun saveTo(writer: Writer) {
        val props = Properties()
        saveTo(props)
        props.store(writer, null)
    }

    fun saveTo(file: File) = saveTo(file.writer())
}

@Throws(IOException::class, FileNotFoundException::class)
fun parseConfigFile(file: File): Configuration? {
    if(!file.isFile) {
        throw FileNotFoundException(file.path)
    }

    val props = Properties()
    props.load(file.reader())

    val user = props["user"] as? String
    val password = props["password"] as? String

    return if(user != null && password != null) Configuration(user, password) else null
}

fun saveDefaultConfig(file: File) {
    Configuration("user", "password").saveTo(file)
}