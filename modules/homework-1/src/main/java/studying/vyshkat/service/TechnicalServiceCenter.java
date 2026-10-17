package studying.vyshkat.service;

import studying.vyshkat.model.Transport;

public final class TechnicalServiceCenter implements ServiceCenter {
    @Override
    public boolean inspect(Transport transport) {
        return transport.operational();
    }
}
