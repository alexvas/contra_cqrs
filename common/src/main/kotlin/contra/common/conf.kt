package contra.common

import org.aeonbits.owner.Config
import org.aeonbits.owner.Reloadable

@Config.Sources("classpath:contra.properties")
interface Conf : Config, Reloadable {

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
