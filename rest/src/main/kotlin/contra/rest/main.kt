package contra.rest

import contra.common.Conf
import contra.dal.configurePools
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.aeonbits.owner.ConfigFactory


object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = ConfigFactory.create(Conf::class.java)!!
        configurePools(conf.jdbcUrl(), conf.dbUser(), conf.dbPassword().ifEmpty { null })

        embeddedServer(Netty, conf.port(), conf.host(), module = Application::module)
                .start(wait = true)
    }
}
