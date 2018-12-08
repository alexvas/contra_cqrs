package contra.dal

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import contra.common.MAX_SEATS_COUNT
import contra.dal.ReadOnlyHikariConfig.config
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.apache.ibatis.type.TypeHandler
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*
import kotlin.experimental.and

private fun convert(input: ByteArray): Set<Int> = sequence {
    var num = 1
    do {
        val byteIdx = num / Byte.SIZE_BITS
        val bitIdx = num % Byte.SIZE_BITS
        val examine = (1 shl bitIdx).toByte()
        if (input[byteIdx] and examine == 0.toByte()) yield(num)
    } while (++num <= MAX_SEATS_COUNT)
}.toSortedSet()

@MappedTypes(Set::class)
@MappedJdbcTypes(JdbcType.BINARY)
class AvailableSeatsHandler : TypeHandler<Set<Int>> {

    override fun setParameter(ps: PreparedStatement, i: Int, parameter: Set<Int>, jdbcType: JdbcType) =
            throw UnsupportedOperationException("not used")

    override fun getResult(rs: ResultSet, columnName: String): Set<Int> =
            convert(rs.getBytes(columnName))

    override fun getResult(rs: ResultSet, columnIndex: Int): Set<Int> =
            convert(rs.getBytes(columnIndex))

    override fun getResult(cs: CallableStatement, columnIndex: Int): Set<Int> =
            convert(cs.getBytes(columnIndex))
}

private val log = LogManager.getLogger()!!

private fun loadHikariProperties(): Properties {
    val resourceName = "/hikari.properties"
    val props = Properties()
    try {
        Unit.javaClass.getResourceAsStream(resourceName).use { `is` -> props.load(`is`) }
    } catch (e: IOException) {
        log.error("failed to load properties from '{}'", resourceName, e)
        throw RuntimeException(e)
    }

    return props
}

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
    typeHandlerRegistry.register(Set::class.java, AvailableSeatsHandler::class.java)
    addMapper(CinemaMapper::class.java)
    addMapper(MovieMapper::class.java)
    addMapper(ShowMapper::class.java)
}

val sessionFactory by lazy { SqlSessionFactoryBuilder().build(configuration())!! }

inline fun <reified M> mapper(): M = sessionFactory.openSession().getMapper(M::class.java)