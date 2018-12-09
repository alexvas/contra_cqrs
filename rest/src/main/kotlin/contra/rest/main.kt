package contra.rest

import contra.dal.configurePools
import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.aeonbits.owner.Config
import org.aeonbits.owner.ConfigFactory
import org.aeonbits.owner.Reloadable


@Config.Sources("classpath:contra.properties")
interface Co : Config, Reloadable {

    @Config.Key("server.host")
    @Config.DefaultValue("localhost")
    fun host(): String

    @Config.Key("server.port")
    @Config.DefaultValue("8080")
    fun port(): Int

    @Config.Key("db.jdbc.url")
    @Config.DefaultValue("jdbc:postgresql://localhost:5432/contra_cqrs")
    fun jdbcUrl(): String

    @Config.Key("db.user")
    @Config.DefaultValue("contra_cqrs")
    fun dbUser(): String

    @Config.Key("db.password")
    @Config.DefaultValue("")
    fun dbPassword(): String
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val conf = ConfigFactory.create(Co::class.java)!!
        configurePools(conf.jdbcUrl(), conf.dbUser(), conf.dbPassword().ifEmpty { null })

        embeddedServer(Netty, conf.port(), conf.host(), module = Application::module)
                .start(wait = true)
    }
}
