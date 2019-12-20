package l30thelion.travelion.shipservice.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.couchbase.core.mapping.Document
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue
import org.springframework.data.couchbase.core.mapping.id.IdAttribute
import org.springframework.data.couchbase.core.mapping.id.IdPrefix
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

const val ID_DELIMITER = "::"

@Document
data class Ship(

        @JsonIgnore
        @IdPrefix
        val idPrefix: String = ID_PREFIX,

        @JsonIgnore
        @Id
        @GeneratedValue(delimiter = ID_DELIMITER)
        val id: String? = null,

        @IdAttribute
        @field:NotNull(message = "code is required")
        @field:Pattern(regexp = "^[A-Z]{2}$", message = "code must be 2 uppercase letters")
        val code: String?,

        @field:NotNull(message = "name is required")
        val name: String?,

        val description: String?

) {
    companion object {
        const val ID_PREFIX = "SHIP"
    }
}