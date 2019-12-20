package l30thelion.travelion.shipservice.model

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import l30thelion.travelion.shipservice.CouchbaseIntegrationTestContainer.ReactiveCouchbaseIntegrationTestConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@SpringJUnitConfig(classes = [ReactiveCouchbaseIntegrationTestConfiguration::class])
class ShipRepositoryIntegrationTest {

    @Autowired
    private lateinit var shipRepository: ShipRepository

    @Test
    fun `should save`() = runBlocking {

        shipRepository
                .save(Ship(code = "AL", name = "Allure", description = "Allure Ship"))
                .awaitSingle()

        val ship = shipRepository
                .findById("SHIP::AL")
                .awaitSingle()

        with(ship) {
            assertEquals("AL", code)
            assertEquals("Allure", name)
            assertEquals("Allure Ship", description)
        }
    }

}