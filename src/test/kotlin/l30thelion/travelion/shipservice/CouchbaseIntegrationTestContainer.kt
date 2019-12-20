package l30thelion.travelion.shipservice

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.Cluster
import com.couchbase.client.java.cluster.ClusterInfo
import com.couchbase.client.java.cluster.DefaultBucketSettings
import com.couchbase.client.java.env.CouchbaseEnvironment
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.couchbase.config.AbstractReactiveCouchbaseConfiguration
import org.springframework.data.couchbase.config.BeanNames
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories
import org.testcontainers.couchbase.CouchbaseContainer

class CouchbaseIntegrationTestContainer {

    companion object {

        const val BUCKET_NAME = "ship_integration_test_bucket"

        private val couchbaseContainer = CouchbaseContainer().withNewBucket(
                DefaultBucketSettings.builder()
                        .name(BUCKET_NAME)
                        .password(BUCKET_NAME)
                        .build()
        )

        init {
            couchbaseContainer.start()
        }
    }

    @Configuration
    @EnableReactiveCouchbaseRepositories
    class ReactiveCouchbaseIntegrationTestConfiguration : AbstractReactiveCouchbaseConfiguration() {

        override fun getBucketName(): String = BUCKET_NAME

        override fun getBucketPassword(): String = BUCKET_NAME

        override fun getBootstrapHosts(): MutableList<String> = mutableListOf()

        @Bean(destroyMethod = "shutdown", name = [BeanNames.COUCHBASE_ENV])
        override fun couchbaseEnvironment(): CouchbaseEnvironment = couchbaseContainer.couchbaseEnvironment

        @Bean(destroyMethod = "disconnect", name = [BeanNames.COUCHBASE_CLUSTER])
        override fun couchbaseCluster(): Cluster = couchbaseContainer.couchbaseCluster

        @Bean(name = [BeanNames.COUCHBASE_CLUSTER_INFO])
        override fun couchbaseClusterInfo(): ClusterInfo = couchbaseCluster().clusterManager(BUCKET_NAME, BUCKET_NAME).info()

        @Bean(destroyMethod = "close", name = [BeanNames.COUCHBASE_BUCKET])
        override fun couchbaseClient(): Bucket = couchbaseCluster().openBucket(BUCKET_NAME)

    }
    
}