package io.snice.networking.codec.diameter.codegen;

import io.snice.networking.codec.diameter.codegen.config.CodeConfig;
import io.snice.networking.codec.diameter.codegen.primitives.AvpPrimitive;
import io.snice.networking.codec.diameter.codegen.templates.AvpTemplate;

import java.util.Optional;

public class Renderer {

    private final DiameterCollector collector;
    private final CodeConfig config;

    public Renderer(final CodeConfig config, final DiameterCollector collector) {
        this.collector = collector;
        this.config = config;
    }

    public String renderAvp(final String name) {
        final Optional<AvpPrimitive> avp = collector.getAvps()
                .stream()
                .filter(a -> a.getName().equals(name))
                .findFirst();

        if (!avp.isPresent()) {
            throw new RuntimeException("AVP not found");
        }

        try {
            return renderAvp(avp.get());
        } catch (final Exception e) {
            // TODO
            e.printStackTrace();
            return null;
        }
    }

    public String renderAvp(final AvpPrimitive avp) throws Exception {
        final AvpTemplate template = AvpTemplate.load(avp);
        return template.render(config);
    }

}
