package l30thelion.travelion.shipservice.model

import org.springframework.data.couchbase.repository.ReactiveCouchbaseRepository
import org.springframework.stereotype.Repository

@Repository
interface ShipRepository : ReactiveCouchbaseRepository<Ship, String>