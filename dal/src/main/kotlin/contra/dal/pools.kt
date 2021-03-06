package contra.dal

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun configurePools(jdbcUrl: String, user: String, password: String? = null) {
    val base = HikariConfig().also {
        it.jdbcUrl = jdbcUrl
        it.username = user
        password?.apply { it.password = this }
    }

    readOnlyDataSource = HikariDataSource(
            base.let {
                HikariConfig().apply {
                    it.copyStateTo(this)
                    isReadOnly = true
                    isAutoCommit = false
                    poolName = "read-only"
                }
            }
    )

    writeOnlyDataSource = HikariDataSource(
            base.let {
                HikariConfig().apply {
                    it.copyStateTo(this)
                    isReadOnly = false
                    isAutoCommit = true
                    poolName = "write-only"
                }
            }
    )
    configureSessionFactory()
}

fun closePools() {
    readOnlyDataSource.close()
    writeOnlyDataSource.close()
}

internal lateinit var readOnlyDataSource: HikariDataSource
internal lateinit var writeOnlyDataSource: HikariDataSource
