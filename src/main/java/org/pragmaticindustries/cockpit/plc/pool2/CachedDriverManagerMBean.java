package org.pragmaticindustries.cockpit.plc.pool2;

public interface CachedDriverManagerMBean {

    String getStateString();

    int getNumberOfConnects();

    int getNumberOfBorrows();

    int getNumberOfWachtdogs();

    int getNumberOfRejections();

    void triggerReconnect();

    int getQueueSize();

}
