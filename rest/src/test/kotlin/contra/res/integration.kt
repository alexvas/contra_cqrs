package contra.res

import contra.common.Conf
import contra.dal.closePools
import contra.dal.configurePools
import contra.dal.initDbWithTestData
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.aeonbits.owner.ConfigFactory
import org.apache.http.impl.nio.reactor.IOReactorConfig
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.util.concurrent.ThreadFactory

var clientThreadNum = 0
val clientThreadFactory = ThreadFactory { Thread(it, "Ktor-client-${++clientThreadNum}").apply { isDaemon = true } }

internal val httpClient = HttpClient(Apache) {
    install(JsonFeature)

    expectSuccess = false
    engine {
        followRedirects = false
        connectTimeout = 1_000
        socketTimeout = 1_000
        connectionRequestTimeout = 2_000
        customizeClient {
            setDefaultIOReactorConfig(
                    IOReactorConfig.custom().apply {
                        setMaxConnPerRoute(1)
                        setMaxConnTotal(1)
                        setIoThreadCount(1)
                    }.build()
            )
            setThreadFactory(clientThreadFactory)
        }
    }
}

val conf = ConfigFactory.create(Conf::class.java)!!

internal suspend inline fun <reified T> call(requestConfig: (HttpRequestBuilder) -> Unit): T {
    val builder = HttpRequestBuilder().apply {
        url.host = conf.host()
        url.port = conf.port()
        method = HttpMethod.Get // default value
        requestConfig.invoke(this)
    }

    return httpClient.request(builder)
}

class CsobIntegrationTests : CoroutineScope {

    override val coroutineContext = Job()

    @BeforeAll
    fun beforeAll() {
        configurePools(conf.jdbcUrl(), conf.dbUser(), conf.dbPassword().ifEmpty { null })
        initDbWithTestData()
    }

    @AfterAll
    fun afterAll() {
        closePools()
    }
}