package com.logging.x1.loggingFilters;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import io.netty.util.AsciiString;
import org.slf4j.Marker;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LoggingNoiseFilter extends TurboFilter {
    private static final Set<String> whiteList = Set.of("READ:", "WRITE:");
    private static final Set<String> blackList = Set.of("WRITE: 0B", "/v1/users/foo", "/intserv/4.0/ping", "PooledSlicedByteBuf", "READ: 200 OK", "READ:  200 OK");
    private static final String ACTUATOR_HEALTH_PATH = "/actuator/health";
    static final AsciiString OK_RESPONSE_CODE = new AsciiString("200");

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (logger.getName().contains("WebClient") && format != null &&
                (whiteList.stream().noneMatch(format::contains) || blackList.stream().anyMatch(format::contains))) {
            return FilterReply.DENY;
        }
        if (logger.getName().contains("AccessLog") && params != null && params.length > 2
                && OK_RESPONSE_CODE.equals(params[0]) && ((String) params[2]).startsWith(ACTUATOR_HEALTH_PATH)) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }
}
