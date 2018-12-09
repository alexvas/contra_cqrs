package contra.dal

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import contra.dal.ReadOnlyHikariConfig.config
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory
import java.util.*

object ReadOnlyHikariConfig {
    lateinit var config: HikariConfig
}

fun configuration(): Configuration = Configuration(
        Environment(
                "main",
                ManagedTransactionFactory().apply {
                    setProperties(Properties().apply { this["closeConnection"] = false })
                },
                HikariDataSource(
                        config.also {
                            HikariConfig().apply {
                                it.copyStateTo(this)
                                isReadOnly = true
                                isAutoCommit = false
                            }
                        }
                )
        )
).apply {
    addMapper(CinemaMapper::class.java)
    addMapper(MovieMapper::class.java)
    addMapper(ShowMapper::class.java)
}

val sessionFactory by lazy { SqlSessionFactoryBuilder().build(configuration())!! }

inline fun <reified M> mapper(): M = sessionFactory.openSession().getMapper(M::class.java)