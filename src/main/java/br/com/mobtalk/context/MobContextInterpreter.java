package br.com.mobtalk.context;

import java.util.UUID;

import net.minecraft.world.entity.Mob;

/**
 * Interpreta acontecimentos recentes do contexto
 * de um mob.
 *
 * O interpretador transforma eventos brutos em
 * informações cognitivas que podem ser utilizadas
 * pelo sistema de intenção e, futuramente,
 * pelo sistema de avaliação de ameaça.
 *
 * Nesta etapa, o interpretador ainda não toma
 * decisões. Ele apenas transforma acontecimentos
 * em informações que outros sistemas podem utilizar.
 */
public final class MobContextInterpreter {

    private MobContextInterpreter() {
        // Classe utilitária.
    }

    /**
     * Verifica se uma entidade atacou recentemente
     * o mob observado.
     */
    public static boolean wasRecentlyDamagedBy(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return false;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return false;
        }

        return context.hasRecentEvent(
                entityId,
                MobContextEvent.MobContextEventType.DAMAGED_BY
        );
    }

    /**
     * Retorna a quantidade total de eventos recentes
     * associados a uma determinada entidade.
     */
    public static int countRecentEvents(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return 0;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return 0;
        }

        return context.getEventsForEntity(
                entityId
        ).size();
    }

    /**
     * Retorna a quantidade de ataques recentes
     * realizados por uma entidade contra o mob.
     */
    public static int countRecentDamages(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return 0;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return 0;
        }

        return context.countEventsForEntity(
                entityId,
                MobContextEvent.MobContextEventType.DAMAGED_BY
        );
    }

    /**
     * Retorna o dano total recebido recentemente
     * de uma determinada entidade.
     */
    public static double getRecentDamage(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return 0.0D;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return 0.0D;
        }

        return context.getTotalEventIntensity(
                entityId,
                MobContextEvent.MobContextEventType.DAMAGED_BY
        );
    }

    /**
     * Retorna o maior dano recebido recentemente
     * de uma determinada entidade.
     */
    public static double getHighestRecentDamage(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return 0.0D;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return 0.0D;
        }

        return context.getMaximumEventIntensity(
                entityId,
                MobContextEvent.MobContextEventType.DAMAGED_BY
        );
    }

    /**
     * Retorna o dano médio recebido recentemente
     * de uma determinada entidade.
     */
    public static double getAverageRecentDamage(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return 0.0D;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return 0.0D;
        }

        return context.getAverageEventIntensity(
                entityId,
                MobContextEvent.MobContextEventType.DAMAGED_BY
        );
    }

    /**
     * Retorna a intensidade do acontecimento
     * mais recente envolvendo uma entidade.
     *
     * Caso não exista um evento, retorna zero.
     */
    public static double getMostRecentEventIntensity(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return 0.0D;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return 0.0D;
        }

        MobContextEvent mostRecent =
                context.getMostRecentEvent(
                        entityId
                );

        if (mostRecent == null) {
            return 0.0D;
        }

        return mostRecent.intensity();
    }

    /**
     * Retorna a idade, em ticks, do acontecimento
     * mais recente envolvendo uma entidade.
     *
     * Caso não exista um evento, retorna -1.
     */
    public static long getMostRecentEventAge(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return -1L;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return -1L;
        }

        MobContextEvent mostRecent =
                context.getMostRecentEvent(
                        entityId
                );

        if (mostRecent == null) {
            return -1L;
        }

        long currentGameTime =
                mob.level().getGameTime();

        return Math.max(
                0L,
                currentGameTime
                        - mostRecent.gameTime()
        );
    }

    /**
     * Retorna a idade, em ticks, do último ataque
     * realizado por determinada entidade.
     *
     * Caso não exista ataque recente, retorna -1.
     */
    public static long getLastDamageAge(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return -1L;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return -1L;
        }

        MobContextEvent mostRecentDamage =
                context.getMostRecentEvent(
                        entityId,
                        MobContextEvent.MobContextEventType.DAMAGED_BY
                );

        if (mostRecentDamage == null) {
            return -1L;
        }

        long currentGameTime =
                mob.level().getGameTime();

        return Math.max(
                0L,
                currentGameTime
                        - mostRecentDamage.gameTime()
        );
    }

    /**
     * Verifica se existe uma sequência de ataques
     * recentes suficientemente próxima no tempo.
     *
     * Nesta primeira versão, dois ou mais ataques
     * dentro dos últimos 60 ticks são considerados
     * uma repetição.
     */
    public static boolean hasRepeatedRecentDamage(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return false;
        }

        MobContext context =
                MobContextManager.getContext(mob);

        if (context == null) {
            return false;
        }

        long currentGameTime =
                mob.level().getGameTime();

        return context.getEventsForEntity(
                        entityId,
                        MobContextEvent.MobContextEventType.DAMAGED_BY
                )
                .stream()
                .filter(event ->
                        currentGameTime
                                - event.gameTime()
                                <= 60L
                )
                .count() >= 2L;
    }

    /**
     * Verifica se o dano recente pode ser considerado
     * significativo.
     *
     * Nesta primeira versão, dano igual ou superior
     * a 1.0 é considerado significativo.
     */
    public static boolean hasSignificantRecentEvent(
            Mob mob,
            UUID entityId
    ) {

        double intensity =
                getMostRecentEventIntensity(
                        mob,
                        entityId
                );

        return intensity >= 1.0D;
    }

    /**
     * Retorna um resumo simples do histórico recente
     * de dano causado por uma entidade.
     */
    public static DamageHistory getDamageHistory(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return DamageHistory.empty();
        }

        return new DamageHistory(
                countRecentDamages(
                        mob,
                        entityId
                ),
                getRecentDamage(
                        mob,
                        entityId
                ),
                getHighestRecentDamage(
                        mob,
                        entityId
                ),
                getAverageRecentDamage(
                        mob,
                        entityId
                ),
                getLastDamageAge(
                        mob,
                        entityId
                ),
                hasRepeatedRecentDamage(
                        mob,
                        entityId
                )
        );
    }

    /**
     * Representa o histórico de dano recente
     * de uma entidade contra um mob.
     */
    public record DamageHistory(
            int damageCount,
            double totalDamage,
            double highestDamage,
            double averageDamage,
            long lastDamageAge,
            boolean repeated
    ) {

        public static DamageHistory empty() {
            return new DamageHistory(
                    0,
                    0.0D,
                    0.0D,
                    0.0D,
                    -1L,
                    false
            );
        }
    }
}