/**
 * 
 */
package io.snice.networking.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.snice.buffer.Buffer;
import io.snice.buffer.Buffers;
import io.snice.networking.common.Transport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public class NetworkInterfaceDeserializer extends JsonDeserializer<NetworkInterfaceConfiguration> {

    /**
     * {@inheritDoc}
     */
    @Override
    public NetworkInterfaceConfiguration deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final JsonNode nameNode = node.get("name");
        final JsonNode listenNode = node.get("listen");
        final JsonNode vipNode = node.get("vipAddress");
        final JsonNode transportNode = node.get("transport");

        if (nameNode == null) {
            throw new IllegalArgumentException("You must specify the name of the Network Interface");
        }

        if (listenNode == null) {
            throw new IllegalArgumentException("You must specify a listen address of the Network Interface");
        }


            final URI listenAddress = parseURI("listen", listenNode.asText());

            URI vipAddress = null;
            if (vipNode != null) {
                vipAddress = parseURI("vipAddress", vipNode.asText());
            }

            final List<Transport> transports = new ArrayList<>();
            if (transportNode != null) {
                if (transportNode instanceof TextNode) {
                    transports.add(Transport.valueOf(transportNode.asText()));
                } else if (transportNode instanceof ArrayNode) {
                    final ArrayNode transportNodes = (ArrayNode) transportNode;
                    final Iterator<JsonNode> nodes = transportNodes.iterator();
                    while (nodes.hasNext()) {
                        transports.add(Transport.valueOf(nodes.next().asText()));
                    }
                }
            }

            return new NetworkInterfaceConfiguration(nameNode.asText(), listenAddress, vipAddress,
                    Collections.unmodifiableList(transports));
    }

    private static URI parseURI(final String paramName, final String uri) {
        try {
            final URI tmp = URI.create(uri);
            return new URI(null, null, tmp.getHost(), tmp.getPort(), null, null, null);
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Unable to parse the parameter " + paramName + " as a valid URI", e);
        }

    }


}
