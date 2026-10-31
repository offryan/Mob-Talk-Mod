package br.com.mobtalk.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Mantém os acontecimentos recentes relevantes
 * para o processo cognitivo de um mob.
 *
 * O contexto representa acontecimentos de curto prazo.
 * Diferentemente da memória, os eventos do contexto
 * possuem duração limitada e são removidos com o tempo.
 */
public final class MobContext {

    public static final long DEFAULT_EVENT_DURATION_TICKS = 200L;

    /**
     * Quantidade máxima de eventos mantidos no contexto.
     *
     * Protege contra crescimento excessivo quando muitos
     * acontecimentos ocorrem dentro da janela de duração,
     * como vários atacantes ao mesmo tempo.
     */
    public static final int MAX_EVENTS = 64;

    private final List<MobContextEvent> events =
            new ArrayList<>();

    /**
     * Adiciona um novo acontecimento ao contexto.
     *
     * Os eventos são mantidos em ordem de chegada.
     * Quando o limite é atingido, o evento mais antigo
     * é descartado.
     */
    public void addEvent(
            MobContextEvent event
    ) {

        if (event == null) {
            return;
        }

        events.add(event);

        while (events.size() > MAX_EVENTS) {
            events.remove(0);
        }
    }

    /**
     * Retorna uma cópia imutável dos eventos atuais.
     */
    public Collection<MobContextEvent> getEvents() {
        return List.copyOf(events);
    }

    /**
     * Retorna a quantidade de eventos atualmente
     * armazenados no contexto.
     */
    public int size() {
        return events.size();
    }

    /**
     * Remove eventos que ultrapassaram o tempo
     * máximo de permanência no contexto.
     */
    public void removeExpiredEvents(
            long currentGameTime
    ) {

        Iterator<MobContextEvent> iterator =
                events.iterator();

        while (iterator.hasNext()) {

            MobContextEvent event =
                    iterator.next();

            long age =
                    currentGameTime - event.gameTime();

            if (age > DEFAULT_EVENT_DURATION_TICKS) {
                iterator.remove();
            }
        }
    }

    /**
     * Retorna todos os eventos recentes relacionados
     * a uma determinada entidade.
     */
    public List<MobContextEvent> getEventsForEntity(
            UUID entityId
    ) {

        if (entityId == null) {
            return List.of();
        }

        return events.stream()
                .filter(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                )
                .toList();
    }

    /**
     * Retorna todos os eventos recentes de um
     * determinado tipo relacionados a uma entidade.
     */
    public List<MobContextEvent> getEventsForEntity(
            UUID entityId,
            MobContextEvent.MobContextEventType type
    ) {

        if (entityId == null || type == null) {
            return List.of();
        }

        return events.stream()
                .filter(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                        && event.type() == type
                )
                .toList();
    }

    /**
     * Verifica se existe pelo menos um evento recente
     * de determinado tipo envolvendo uma entidade.
     */
    public boolean hasRecentEvent(
            UUID entityId,
            MobContextEvent.MobContextEventType type
    ) {

        if (entityId == null || type == null) {
            return false;
        }

        return events.stream()
                .anyMatch(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                        && event.type() == type
                );
    }

    /**
     * Conta quantos eventos de determinado tipo
     * aconteceram recentemente envolvendo uma entidade.
     */
    public int countEventsForEntity(
            UUID entityId,
            MobContextEvent.MobContextEventType type
    ) {

        if (entityId == null || type == null) {
            return 0;
        }

        return (int) events.stream()
                .filter(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                        && event.type() == type
                )
                .count();
    }

    /**
     * Soma a intensidade de todos os eventos recentes
     * de determinado tipo envolvendo uma entidade.
     *
     * No caso de DAMAGED_BY, a intensidade representa
     * o dano recebido.
     */
    public double getTotalEventIntensity(
            UUID entityId,
            MobContextEvent.MobContextEventType type
    ) {

        if (entityId == null || type == null) {
            return 0.0D;
        }

        return events.stream()
                .filter(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                        && event.type() == type
                )
                .mapToDouble(
                        MobContextEvent::intensity
                )
                .sum();
    }

    /**
     * Retorna a maior intensidade registrada recentemente
     * para determinado tipo de evento e entidade.
     */
    public double getMaximumEventIntensity(
            UUID entityId,
            MobContextEvent.MobContextEventType type
    ) {

        if (entityId == null || type == null) {
            return 0.0D;
        }

        return events.stream()
                .filter(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                        && event.type() == type
                )
                .mapToDouble(
                        MobContextEvent::intensity
                )
                .max()
                .orElse(0.0D);
    }

    /**
     * Retorna a intensidade média dos eventos recentes
     * de determinado tipo envolvendo uma entidade.
     */
    public double getAverageEventIntensity(
            UUID entityId,
            MobContextEvent.MobContextEventType type
    ) {

        if (entityId == null || type == null) {
            return 0.0D;
        }

        return events.stream()
                .filter(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                        && event.type() == type
                )
                .mapToDouble(
                        MobContextEvent::intensity
                )
                .average()
                .orElse(0.0D);
    }

    /**
     * Retorna o acontecimento mais recente envolvendo
     * uma determinada entidade.
     */
    public MobContextEvent getMostRecentEvent(
            UUID entityId
    ) {

        if (entityId == null) {
            return null;
        }

        return events.stream()
                .filter(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                )
                .max(
                        java.util.Comparator.comparingLong(
                                MobContextEvent::gameTime
                        )
                )
                .orElse(null);
    }

    /**
     * Retorna o acontecimento mais recente de um tipo
     * específico envolvendo uma entidade.
     */
    public MobContextEvent getMostRecentEvent(
            UUID entityId,
            MobContextEvent.MobContextEventType type
    ) {

        if (entityId == null || type == null) {
            return null;
        }

        return events.stream()
                .filter(event ->
                        entityId.equals(
                                event.otherEntityId()
                        )
                        && event.type() == type
                )
                .max(
                        java.util.Comparator.comparingLong(
                                MobContextEvent::gameTime
                        )
                )
                .orElse(null);
    }

    /**
     * Remove todos os eventos do contexto.
     */
    public void clear() {
        events.clear();
    }
}