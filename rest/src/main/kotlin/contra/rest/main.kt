@file:JvmName("Main")
package contra.rest

import contra.common.Conf
import contra.dal.configurePools
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.aeonbits.owner.ConfigFactory

internal fun startServer(conf: Conf, wait: Boolean) {
    configurePools(conf.jdbcUrl(), conf.dbUser(), conf.dbPassword().ifEmpty { null })
    embeddedServer(Netty, conf.port(), conf.host(), module = Application::module)
            .start(wait)
}

fun main(args: Array<String>) {
    startServer(ConfigFactory.create(Conf::class.java)!!, true)
}
