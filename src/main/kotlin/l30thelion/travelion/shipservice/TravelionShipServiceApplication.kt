package l30thelion.travelion.shipservice

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import l30thelion.travelion.shipservice.SailingRepository.Sailing
import l30thelion.travelion.shipservice.model.Ship
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import reactor.core.publisher.Flux

@SpringBootApplication
class TravelionShipServiceApplication {
    @Bean
    fun webClient(@Value("\${SAILING_SERVICE_BASE_URL:http://travelion-sailing-service:8080}") sailingServiceBaseUrl: String) =
            WebClient.builder().baseUrl(sailingServiceBaseUrl).build()
}

fun main(args: Array<String>) {
    runApplication<TravelionShipServiceApplication>(*args)
}

@RestController
@RequestMapping("/ships")
class ShipController(private val webClient: WebClient) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun findAll(@RequestHeader headers: Map<String, String>): List<Ship> = ShipRepository.findAll().map { ship ->
        log.info("retrieving finAll sailings for ship {}", ship.code)
        val sailings = SailingRepository.findByShipCode(ship.code!!)
        log.info("retrieved sailings for ship {}, sailings {}", ship.code, sailings)
        ship.copy(sailings = sailings)
    }

    @GetMapping("/reactor")
    fun findAllReactor(@RequestHeader headers: Map<String, String>): Flux<Ship> {
        log.info("Received reactor request headers {}", headers)
        return ShipRepository.findAllReactive().flatMap { ship ->
            log.info("retrieving sailings for ship {}", ship.code)
            webClient
                    .get()
                    .uri("/sailings/{shipCode}", ship.code)
                    .retrieve()
                    .bodyToMono(object : ParameterizedTypeReference<List<Sailing>>() {})
                    .map { sailings ->
                        log.info("retrieved sailings for ship {}, sailings {}", ship.code, sailings)
                        ship.copy(sailings = sailings)
                    }
        }
    }

    @GetMapping("/coroutines")
    suspend fun findAllCoroutines(@RequestHeader headers: Map<String, String>): List<Ship> = coroutineScope {
        log.info("Received coroutines request headers {}", headers)
        ShipRepository.findAllCoroutines().mapAsyncAwaitAll { ship ->
            log.info("retrieving sailings for ship {}", ship.code)
            val sailings = webClient
                    .get()
                    .uri("/sailings/{shipCode}", ship.code)
                    .awaitExchange()
                    .awaitBody<List<Sailing>>()
            log.info("retrieved sailings for ship {}, sailings {}", ship.code, sailings)
            ship.copy(sailings = sailings)
        }
    }

    @GetMapping("/coroutines2")
    suspend fun findAllCoroutines2(@RequestHeader headers: Map<String, String>): List<Ship> = coroutineScope {
        log.info("Received coroutines 2 request headers {}", headers)
        ShipRepository.findAll().map { ship ->
            log.info("retrieving sailings for ship {}", ship.code)
            val sailings = webClient
                    .get()
                    .uri("/sailings/{shipCode}", ship.code)
                    .awaitExchange()
                    .awaitBody<List<Sailing>>()
            log.info("retrieved sailings for ship {}, sailings {}", ship.code, sailings)
            ship.copy(sailings = sailings)
        }
    }

    @GetMapping("/flow")
    suspend fun findAllCoroutinesFlow(@RequestHeader headers: Map<String, String>): Flow<Ship> {
        log.info("Received coroutines flow request headers {}", headers)
        return ShipRepository.findAllReactive().asFlow().map { ship ->
            log.info("retrieving sailings for ship {}", ship.code)
            val sailings = webClient
                    .get()
                    .uri("/sailings/{shipCode}", ship.code)
                    .awaitExchange()
                    .awaitBody<List<Sailing>>()
            log.info("retrieved sailings for ship {}, sailings {}", ship.code, sailings)
            ship.copy(sailings = sailings)
        }
    }
}

class ShipRepository {

    companion object {

        fun findAll() = listOf(
                Ship(code = "AF", name = "Arthur Foss", description = "A classic old wooden tugboat."),
                Ship(code = "AX", name = "Axe Fury", description = "A classic but furious ship."),
                Ship(code = "AQ", name = "American Queen", description = "A recently build Mississippi river steamboat."),
                Ship(code = "AL", name = "Allure of the Seas", description = "Alluring cruise ship."))

        fun findAllReactive() = Flux.fromIterable(findAll())

        suspend fun findAllCoroutines() = findAllReactive().asFlow().toList()
    }
}

class SailingRepository {

    data class Sailing(val shipCode: String, val sailDate: String)

    companion object {

        private val sailings = listOf(
                Sailing("AQ", "2019-12-15"),
                Sailing("AQ", "2019-12-20"),
                Sailing("AQ", "2019-12-25"),
                Sailing("AF", "2020-01-25"),
                Sailing("AF", "2020-01-31"),
                Sailing("AL", "2020-02-09")
        )

        fun findByShipCode(shipCode: String): List<Sailing> = sailings.filter { sailing -> sailing.shipCode == shipCode }
    }
}

suspend fun <T, R> Iterable<T>.mapAsyncAwaitAll(f: suspend (T) -> R): List<R> = coroutineScope { map { async { f(it) } }.awaitAll() }