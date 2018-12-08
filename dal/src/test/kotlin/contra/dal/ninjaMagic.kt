package contra.dal

import com.ninja_squad.dbsetup.DbSetup
import com.ninja_squad.dbsetup.DbSetupTracker
import com.ninja_squad.dbsetup.bind.Binder
import com.ninja_squad.dbsetup.bind.DefaultBinderConfiguration
import com.ninja_squad.dbsetup.destination.DataSourceDestination
import com.ninja_squad.dbsetup.operation.CompositeOperation
import com.ninja_squad.dbsetup.operation.Operation
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.util.PGobject
import java.sql.ParameterMetaData
import java.sql.PreparedStatement
import java.sql.Types.OTHER
import javax.sql.DataSource

object PgCustomBindConf : DefaultBinderConfiguration() {

    override fun getBinder(metadata: ParameterMetaData?, param: Int): Binder =
            BinderWrapper(super.getBinder(metadata, param))

    private class BinderWrapper internal constructor(private val delegate: Binder) : Binder {

        override fun bind(statement: PreparedStatement, paramIndex: Int, value: Any?) {
            if (value == null) {
                delegate.bind(statement, paramIndex, null)
                return
            }

            val chunks = value.toString().splitToSequence("::")
                    .map(String::trim)
                    .toList()

            if (chunks.size == 1) {
                delegate.bind(statement, paramIndex, value)
                return
            }

            val type = chunks[1]
            val v = chunks[0]
            when {
                "other" == type.toLowerCase() -> statement.setObject(paramIndex, v, OTHER)
                else -> statement.setObject(paramIndex, PGobject().apply {
                    this.type = type
                    this.value = v
                })
            }
        }
    }
}

class NinjaAdapter {
    private val dbSetupTracker = DbSetupTracker()
    private val ninjaDs: DataSource

    init {
        ReadOnlyHikariConfig.config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/contra_cqrs"
            username = "contra_cqrs"
            isReadOnly = true
            isAutoCommit = false
        }
        ninjaDs = HikariDataSource(
                HikariConfig().apply {
                    ReadOnlyHikariConfig.config.copyStateTo(this)
                    isReadOnly = false
                }
        )

    }

    // вызываем в @BeforeEach beforeEach() теста
    fun prepare(vararg initDb: Operation): NinjaAdapter {
        require(initDb.isNotEmpty()) { "no init operation!" }
        val dbSetup = DbSetup(DataSourceDestination(ninjaDs), adapt(initDb), PgCustomBindConf)
        dbSetupTracker.launchIfNecessary(dbSetup)
        return this
    }

    private fun adapt(initDb: Array<out Operation>) =
            if (initDb.size == 1) initDb[0] else CompositeOperation.sequenceOf(initDb.toList())

    fun replenish(vararg additional: Operation): NinjaAdapter {
        DbSetup(DataSourceDestination(ninjaDs), adapt(additional), PgCustomBindConf).launch()
        return this
    }

    fun skipNextLaunch(): NinjaAdapter {
        dbSetupTracker.skipNextLaunch()
        return this
    }
}
