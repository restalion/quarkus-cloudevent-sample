package com.accenture.sample.cloudevent.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.accenture.sample.cloudevent.event.SampleEvent;

import javax.validation.Validator;
import javax.validation.ConstraintViolation;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.Iterator;
import java.util.Set;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import io.cloudevents.extensions.DistributedTracingExtension;
import io.cloudevents.extensions.ExtensionFormat;
import io.cloudevents.format.Wire;
import io.cloudevents.v03.CloudEventBuilder;
import io.cloudevents.v03.CloudEventImpl;
import io.cloudevents.v03.http.Marshallers;

import org.apache.http.entity.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.jboss.logging.Logger;

@ApplicationScoped
public class EventService {

    /** Propiedad que nos indica si podemos enviar eventos */
    @ConfigProperty(name = "com.accenture.event.active", defaultValue = "false")
    Boolean eventActive;

    private static final Logger LOGGER =  Logger.getLogger(EventService.class);

    /** Propiedad con la url del canal del broker */
    @ConfigProperty(name="com.accenture.event.broker.url")
    String target;

    @ConfigProperty(name="com.accenture.event.type")
    String type;

    @Inject
    Validator validator;

    /**
     * Método que envía el mensaje al broker correspondiente.
     * @param payload Payload a incluir como cuerpo del mensaje
     */
    public void sendEvent(SampleEvent payload) {

        validatePayload(payload);

        // Create a tracing extension
        final DistributedTracingExtension dt = new DistributedTracingExtension();

        dt.setTraceparent("0");
        dt.setTracestate("congo=4");
        // Format it as extension format
        final ExtensionFormat tracing = new DistributedTracingExtension.Format(dt);
        String id = UUID.randomUUID().toString();
        LOGGER.debug("id: " + id);
        LOGGER.debug("type: " + type);

        CloudEventImpl<SampleEvent> ce =
            CloudEventBuilder.<SampleEvent>builder()
                .withType(type)
                .withId(id)
                .withTime(ZonedDateTime.now())
                .withDatacontenttype(ContentType.APPLICATION_JSON.toString())
                .withData(payload)
                .withExtension(tracing)
                .withSubject("Sample update")
                .withSource(URI.create("/sample-source"))
                .build();

        LOGGER.debug("type: " + ce.getAttributes().getType());
   
        Wire<String, String, String> wire =
            Marshallers.<SampleEvent>
            binary()
            .withEvent(() -> ce)
            .marshal();

        wire.getHeaders().entrySet().forEach(e -> LOGGER.debug("header " + e.getKey() + ":" + e.getValue()));
        LOGGER.debug("Payload: " + wire.getPayload());

        if (eventActive) fireEvent(wire);
    }

    /**
     * Disparador del envío de mensaje.
     * @param wire objeto con el evento a añadir al canal
     */
    private void fireEvent(Wire<String, String, String> wire) {

        Client httpClient = ResteasyClientBuilder.newBuilder().build();
        LOGGER.debug("CloudEvent to add: " + wire.getPayload());

        Entity<String> entity = Entity.entity(wire.getPayload().get(), MediaType.APPLICATION_JSON);
        Invocation.Builder invocation = httpClient.target(target).request();
        wire.getHeaders().entrySet().forEach(e -> invocation.header(e.getKey(), e.getValue()));
        Response response = invocation.post(entity);
        LOGGER.debug("Message sended and the response was: " + response.getStatus());

        httpClient.close();
    }

    private void validatePayload(SampleEvent payload) throws WebApplicationException {

        Set<ConstraintViolation<SampleEvent>> violations = validator.validate(payload);

        if (violations.size() > 0) {
            String violationMessage = "| ";
            Iterator<ConstraintViolation<SampleEvent>> it = violations.iterator();
            while (it.hasNext()) {
                violationMessage = violationMessage + it.next().getMessage() + " | ";
            }
            Response response = Response.status(Status.BAD_REQUEST).build();
            throw new WebApplicationException(violationMessage, response);
        }
    }
}
