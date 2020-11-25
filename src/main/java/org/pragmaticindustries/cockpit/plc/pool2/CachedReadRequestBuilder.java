package org.pragmaticindustries.cockpit.plc.pool2;

import org.apache.plc4x.java.api.messages.PlcReadRequest;

/**
 * TODO write comment
 *
 * @author julian
 * Created by julian on 24.02.20
 */
public class CachedReadRequestBuilder implements PlcReadRequest.Builder {

    private final CachedPlcConnection parent;
    private final PlcReadRequest.Builder builder;

    public CachedReadRequestBuilder(CachedPlcConnection parent, PlcReadRequest.Builder builder) {
        this.parent = parent;
        this.builder = builder;
    }

    @Override
    public PlcReadRequest.Builder addItem(String s, String s1) {
        builder.addItem(s, s1);
        return this;
    }

    @Override
    public PlcReadRequest build() {
        return new CachedReadRequest(parent, builder.build());
    }

}
