package de.sldk.mc;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.common.TextFormat;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import net.md_5.bungee.BungeeCord;

public class MetricsController extends AbstractHandler {


    private Gauge players = Gauge.build().name("mc_players_total").help("Online players").labelNames("state").create().register();
    private Gauge memory = Gauge.build().name("mc_jvm_memory").help("JVM memory usage").labelNames("type").create().register();

    public MetricsController() {
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (!target.equals("/metrics")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        players.clear();

        players.labels("online").set(BungeeCord.getInstance().getOnlineCount());

        memory.labels("max").set(Runtime.getRuntime().maxMemory());
        memory.labels("free").set(Runtime.getRuntime().freeMemory());
        memory.labels("used").set(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(TextFormat.CONTENT_TYPE_004);

        TextFormat.write004(response.getWriter(), CollectorRegistry.defaultRegistry.metricFamilySamples());

        baseRequest.setHandled(true);
    }
}
