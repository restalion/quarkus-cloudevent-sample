package com.accenture.sample.cloudevent;

import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.accenture.sample.cloudevent.dto.SampleDTO;
import com.accenture.sample.cloudevent.event.SampleEvent;
import com.accenture.sample.cloudevent.service.EventService;

import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/event-sample")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SampleResource {

    private static final Logger LOGGER =  Logger.getLogger(SampleResource.class);


    @Inject
    Validator validator;

    @Inject
    EventService service;    

    @POST
    public SampleEvent sendEvent(SampleDTO payload) throws WebApplicationException {

        LOGGER.debug("Call to sendEvent method with parameters: {code:" + payload.getCode() 
            + ", value:" + payload.getValue() + "}");
        validatePayload(payload);
        SampleEvent event = createEvent(payload);
        service.sendEvent(event);
        return event;
    }

    private SampleEvent createEvent(SampleDTO dto) {
        SampleEvent event = new SampleEvent();
        event.setCode(dto.getCode());
        event.setValue(dto.getValue());
        return event;
    }
    
    private void validatePayload(SampleDTO payload) throws WebApplicationException {

        Set<ConstraintViolation<SampleDTO>> violations = validator.validate(payload);

        if (violations.size() > 0) {
            String violationMessage = "| ";
            Iterator<ConstraintViolation<SampleDTO>> it = violations.iterator();
            while (it.hasNext()) {
                violationMessage = violationMessage + it.next().getMessage() + " | ";
            }
            Response response = Response.status(Status.BAD_REQUEST).build();
            throw new WebApplicationException(violationMessage, response);
        }
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Override
        public Response toResponse(Exception exception) {
            System.out.println("Manejando la excepción: " + exception.getMessage());
            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }
            return Response.status(code)
                    .entity(Json.createObjectBuilder().add("error", exception.getMessage()).add("code", code).build())
                    .build();
        }
    }
}
