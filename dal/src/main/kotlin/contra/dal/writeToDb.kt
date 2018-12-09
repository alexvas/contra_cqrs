package contra.dal

import contra.common.Show
import java.sql.Types

private const val sql = "{call book(?, ?, ?)}"

fun book(show: Show, seats: Set<Int>): Boolean = writeOnlyDataSource.connection!!.use { conn ->
    conn.prepareCall(sql)!!.apply {
        setInt(1, show.id)
        setArray(2, conn.createArrayOf("INTEGER", seats.toTypedArray()))
        registerOutParameter(3, Types.BOOLEAN)
        execute()
    }.getBoolean(3)
}