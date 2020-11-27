package org.pragmaticindustries.cockpit.plc.pool2;

import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;

/**
 * Simple Factory Method which creates a new PlcConnection
 *
 * @author julian
 * Created by julian on 27.11.20
 */
@FunctionalInterface
public interface PlcConnectionFactory {

    PlcConnection create() throws PlcConnectionException;

}
