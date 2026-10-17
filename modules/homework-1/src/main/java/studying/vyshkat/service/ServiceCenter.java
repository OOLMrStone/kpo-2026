package studying.vyshkat.service;

import studying.vyshkat.model.Transport;

@FunctionalInterface
public interface ServiceCenter {
    /** Возвращает результат техосмотра. Сам сервис не добавляет транспорт в реестр. */
    boolean inspect(Transport transport);
}
