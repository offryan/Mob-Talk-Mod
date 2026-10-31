package br.com.mobtalk.profile;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.Mob;

/**
 * Gerencia os perfis cognitivos dos mobs.
 *
 * Os perfis são mantidos separadamente da entidade
 * para que o sistema possa evoluir sem modificar
 * diretamente a implementação das entidades Vanilla.
 */
public final class MobProfileManager {

    private static final Map<Mob, MobProfile> PROFILES =
            new WeakHashMap<>();

    private MobProfileManager() {
        // Classe utilitária.
    }

    /**
     * Obtém o perfil de um mob.
     *
     * Caso o mob ainda não possua um perfil,
     * um perfil neutro será criado automaticamente.
     */
    public static MobProfile getProfile(
            Mob mob
    ) {

        if (mob == null) {
            return MobProfile.neutral();
        }

        return PROFILES.computeIfAbsent(
                mob,
                MobProfileManager::createDefaultProfile
        );
    }

    /**
     * Define manualmente o perfil de um mob.
     */
    public static void setProfile(
            Mob mob,
            MobProfile profile
    ) {

        if (mob == null || profile == null) {
            return;
        }

        PROFILES.put(
                mob,
                profile
        );
    }

    /**
     * Remove o perfil de um mob.
     */
    public static void removeProfile(
            Mob mob
    ) {

        if (mob == null) {
            return;
        }

        PROFILES.remove(mob);
    }

    /**
     * Remove todos os perfis armazenados.
     */
    public static void clear() {
        PROFILES.clear();
    }

    /**
     * Cria o perfil inicial de uma entidade.
     *
     * Nesta primeira etapa utilizamos o comportamento
     * Vanilla como referência básica.
     *
     * Perfis específicos por espécie serão adicionados
     * posteriormente.
     */
    private static MobProfile createDefaultProfile(
            Mob mob
    ) {

        if (mob == null) {
            return MobProfile.neutral();
        }

        if (mob.getType().getCategory().isFriendly()) {
            return MobProfile.passive();
        }

        return MobProfile.neutral();
    }
}