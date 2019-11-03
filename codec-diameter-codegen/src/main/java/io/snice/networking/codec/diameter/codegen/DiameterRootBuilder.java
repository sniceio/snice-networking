package io.snice.networking.codec.diameter.codegen;

import io.snice.networking.codec.diameter.codegen.builders.AttributeContext;
import io.snice.networking.codec.diameter.codegen.builders.DiameterSaxBuilder;
import io.snice.networking.codec.diameter.codegen.primitives.DiameterPrimitive;
import org.xml.sax.SAXException;

public class DiameterRootBuilder extends DiameterSaxBuilder.BaseBuilder<DiameterPrimitive> {

    public DiameterRootBuilder(final AttributeContext ctx) {
        super(ctx);
    }

    @Override
    public String getElementName() {
        return "root";
    }

    @Override
    public DiameterPrimitive build(final DiameterCollector ctx) {
        return null;
    }


    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        // ignore
    }
}

