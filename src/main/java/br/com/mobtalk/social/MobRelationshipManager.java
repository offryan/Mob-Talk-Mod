package br.com.mobtalk.social;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import br.com.mobtalk.MobTalkAttachments;

import net.minecraft.world.entity.Mob;

/**
 * Gerencia as relações sociais individuais dos mobs.
 *
 * Cada mob possui seu próprio conjunto de relações.
 *
 * Exemplo:
 *
 * Zumbi A
 * ├── Jogador X → hostilidade alta
 * ├── Zumbi B → confiança moderada
 * └── Creeper → familiaridade baixa
 *
 * Zumbi B possui outro conjunto completamente independente.
 *
 * As relações ficam em um attachment do NeoForge na própria
 * entidade e são gravadas no NBT do mob.
 */
public final class MobRelationshipManager {

    /**
     * Ticks sem nova agressão para a hostilidade
     * cair pela metade.
     *
     * 6000 ticks = aproximadamente 5 minutos.
     */
    public static final long HOSTILITY_HALF_LIFE_TICKS = 6000L;

    private MobRelationshipManager() {
        // Classe utilitária.
    }

    /**
     * Faz a hostilidade de todas as relações do mob
     * diminuir com o tempo decorrido.
     *
     * Novas agressões continuam aumentando a hostilidade
     * normalmente; o decaimento só a reduz aos poucos.
     *
     * @param elapsedTicks ticks desde o último decaimento
     */
    public static void decayHostility(
            Mob mob,
            long elapsedTicks
    ) {

        if (mob == null || elapsedTicks <= 0L) {
            return;
        }

        if (!mob.hasData(MobTalkAttachments.RELATIONSHIPS)) {
            return;
        }

        Map<UUID, MobRelationship> relationships =
                entries(mob);

        double factor =
                Math.pow(
                        0.5D,
                        (double) elapsedTicks
                                / HOSTILITY_HALF_LIFE_TICKS
                );

        relationships.replaceAll((entityId, relationship) ->
                relationship.hostility() > 0.0D
                        ? relationship.decayHostility(factor)
                        : relationship
        );
    }

    /**
     * Remove as relações que não devem mais ser mantidas.
     *
     * @param shouldForget critério de remoção
     * @return quantidade de relações removidas
     */
    public static int forgetRelationships(
            Mob mob,
            Predicate<MobRelationship> shouldForget
    ) {

        if (mob == null || shouldForget == null) {
            return 0;
        }

        if (!mob.hasData(MobTalkAttachments.RELATIONSHIPS)) {
            return 0;
        }

        Map<UUID, MobRelationship> relationships =
                entries(mob);

        int before =
                relationships.size();

        relationships.values().removeIf(
                shouldForget
        );

        int removed =
                before - relationships.size();

        if (relationships.isEmpty()) {
            mob.removeData(MobTalkAttachments.RELATIONSHIPS);
        }

        return removed;
    }

    /**
     * Obtém a relação de um mob com determinada entidade.
     *
     * Caso ainda não exista uma relação, uma relação inicial
     * será criada.
     */
    public static MobRelationship getRelationship(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return null;
        }

        return entries(mob).computeIfAbsent(
                entityId,
                MobRelationship::initial
        );
    }

    /**
     * Atualiza uma relação existente.
     */
    public static void setRelationship(
            Mob mob,
            MobRelationship relationship
    ) {

        if (mob == null || relationship == null) {
            return;
        }

        entries(mob).put(
                relationship.entityId(),
                relationship
        );
    }

    /**
     * Remove uma relação específica.
     */
    public static void removeRelationship(
            Mob mob,
            UUID entityId
    ) {

        if (mob == null || entityId == null) {
            return;
        }

        if (!mob.hasData(MobTalkAttachments.RELATIONSHIPS)) {
            return;
        }

        entries(mob).remove(entityId);
    }

    /**
     * Remove todas as relações de um mob.
     */
    public static void clearMob(
            Mob mob
    ) {

        if (mob == null) {
            return;
        }

        mob.removeData(MobTalkAttachments.RELATIONSHIPS);
    }

    /**
     * Mapa de relações do mob, criado quando necessário.
     */
    private static Map<UUID, MobRelationship> entries(
            Mob mob
    ) {

        return mob.getData(
                MobTalkAttachments.RELATIONSHIPS
        ).entries();
    }
}