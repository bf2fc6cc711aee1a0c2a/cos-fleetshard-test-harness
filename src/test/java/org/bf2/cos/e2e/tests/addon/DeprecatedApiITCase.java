package org.bf2.cos.e2e.tests.addon;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.api.model.miscellaneous.apiserver.v1.APIRequestCount;
import io.fabric8.openshift.api.model.miscellaneous.apiserver.v1.APIRequestCountStatus;
import io.fabric8.openshift.api.model.miscellaneous.apiserver.v1.PerUserAPIRequestCount;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftConfig;

@Order(3)
@Tag("deprecated-api")
public class DeprecatedApiITCase {

    private static NamespacedOpenShiftClient client = new DefaultOpenShiftClient(new OpenShiftConfig(Config.autoConfigure(null)))
        .inNamespace("redhat-openshift-connectors");

    public static boolean checkForApis() {
        return client.getApiGroup("apiserver.openshift.io") != null;
    }

    @DisplayName("deprecated api should not be used:")
    @ParameterizedTest(name = "{0}")
    @MethodSource("apirequestProvider")
    @EnabledIf(value = "checkForApis", disabledReason = "apirequestcount is openshift specific API")
    public void deprecatedApiIsNotRequested(APIRequestCount requestCount) {
        final APIRequestCountStatus requestCountStatus = requestCount.getStatus();
        final String removedInRelease = requestCountStatus.getRemovedInRelease();

        final Set<String> rhocServiceAccounts =
            client.serviceAccounts().list().getItems().stream().map(sa -> sa.getMetadata().getName()).collect(Collectors.toSet());

        if (requestCountStatus.getCurrentHour().getRequestCount() > 0) {
            final Set<String> usersRequestingApi =
                requestCountStatus.getLast24h().stream().flatMap(it -> it.getByNode().stream()).flatMap(it -> it.getByUser().stream()).filter(
                        Objects::nonNull)
                    .map(
                        PerUserAPIRequestCount::getUsername).collect(Collectors.toSet());

            final Set<String> rhocUsersRequestingApi =
                usersRequestingApi.stream().filter(user -> rhocServiceAccounts.stream().anyMatch(user::endsWith)).collect(Collectors.toSet());

            Assertions.assertTrue(rhocUsersRequestingApi.isEmpty(),
                usersRequestingApi + " is requesting removed API " + requestCount.getMetadata().getName() + " from version " + removedInRelease);
        }
    }

    public static Stream<APIRequestCount> apirequestProvider() {
        return client.apiRequestCounts().list().getItems().stream().filter(reqCount -> reqCount.getStatus().getRemovedInRelease() != null);
    }
}
