package br.com.mobtalk.social;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

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
 */
public final class MobRelationshipManager {

    private static final Map<Mob, Map<UUID, MobRelationship>>
            RELATIONSHIPS = new WeakHashMap<>();

    private MobRelationshipManager() {
        // Classe utilitária.
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

        Map<UUID, MobRelationship> relationships =
                RELATIONSHIPS.computeIfAbsent(
                        mob,
                        ignored -> new HashMap<>()
                );

        return relationships.computeIfAbsent(
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

        Map<UUID, MobRelationship> relationships =
                RELATIONSHIPS.computeIfAbsent(
                        mob,
                        ignored -> new HashMap<>()
                );

        relationships.put(
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

        Map<UUID, MobRelationship> relationships =
                RELATIONSHIPS.get(mob);

        if (relationships == null) {
            return;
        }

        relationships.remove(entityId);
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

        RELATIONSHIPS.remove(mob);
    }

    /**
     * Remove todas as relações gerenciadas.
     */
    public static void clear() {
        RELATIONSHIPS.clear();
    }
}