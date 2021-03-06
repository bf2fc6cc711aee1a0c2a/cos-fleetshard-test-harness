package org.bf2.cos.e2e.tests.addon;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Order(0)
@Tags(value = {
        @Tag("install"),
        @Tag("upgrade-phase-1"),
})
public class AddonCrdITCase {

    private static DefaultOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)));

    @DisplayName("crd should have been created:")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "managedconnectorclusters.cos.bf2.org",
            "managedconnectoroperators.cos.bf2.org",
            "managedconnectors.cos.bf2.org",
            "builds.camel.apache.org",
            "camelcatalogs.camel.apache.org",
            "integrationkits.camel.apache.org",
            "integrationplatforms.camel.apache.org",
            "integrations.camel.apache.org",
            "kameletbindings.camel.apache.org",
            "kamelets.camel.apache.org",
            "kafkabridges.kafka.strimzi.io",
            "kafkaconnectors.kafka.strimzi.io",
            "kafkaconnects.kafka.strimzi.io",
            "kafkamirrormaker2s.kafka.strimzi.io",
            "kafkamirrormakers.kafka.strimzi.io",
            "kafkarebalances.kafka.strimzi.io",
            "kafkas.kafka.strimzi.io",
            "kafkatopics.kafka.strimzi.io",
            "kafkausers.kafka.strimzi.io",
            "strimzipodsets.core.strimzi.io",
    })
    public void crdShouldHaveBeenCreated(String crdName) {
        Awaitility.await()
                .atMost(Duration.ofMinutes(3))
                .untilAsserted(() -> {
                            CustomResourceDefinition crd = client.apiextensions().v1().customResourceDefinitions().withName(crdName).get();
                            Assertions.assertNotNull(crd, () -> crdName + " CRD not created");
                        }
                );
    }

}
