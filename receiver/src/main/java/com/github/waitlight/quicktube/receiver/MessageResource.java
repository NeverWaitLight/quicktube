package com.github.waitlight.quicktube.receiver;

import com.github.waitlight.quicktube.common.Msg;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/message")
public class MessageResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageResource.class);

    private final MessageProducerService producerService;

    @Inject
    public MessageResource(MessageProducerService producerService) {
        this.producerService = producerService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveMessage(Msg message) {
        try {
            LOGGER.info("接收到消息 adr={} content={}", message.adr().getAdr(), message.content());
            producerService.sendMessage(message);
            return Response.ok().entity("{\"status\":\"success\",\"message\":\"消息已成功接收并放入队列\"}").build();
        } catch (Exception e) {
            LOGGER.error("处理消息失败", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"处理消息失败\"}").build();
        }
    }
}