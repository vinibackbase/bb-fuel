package com.backbase.ct.bbfuel.client.notification;

import com.backbase.ct.bbfuel.client.common.RestClient;
import com.backbase.ct.bbfuel.config.BbFuelConfiguration;
import com.backbase.dbs.presentation.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationsPresentationRestClient extends RestClient {

    private final BbFuelConfiguration config;

    private static final String SERVICE_VERSION = "v2";
    private static final String PATH_EMPLOYEE = "/employee";
    private static final String ENDPOINT_NOTIFICATIONS = PATH_EMPLOYEE + "/notifications";
    private static final String NOTIFICATIONS_PRESENTATION_SERVICE = "notifications-presentation-service";

    /** Create notifications base path. */
    @PostConstruct
    public void init() {
        setBaseUri(config.getPlatform().getGateway());
        setVersion(SERVICE_VERSION);
        setInitialPath(NOTIFICATIONS_PRESENTATION_SERVICE);
    }

    /** Create notification. */
    public Response createNotification(NotificationsPostRequestBody body) {
        return requestSpec()
            .contentType(ContentType.JSON)
            .body(body)
            .post(getPath(ENDPOINT_NOTIFICATIONS));
    }

}
