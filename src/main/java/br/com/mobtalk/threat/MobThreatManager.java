package br.com.mobtalk.threat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.world.entity.Mob;

/**
 * Mantém as avaliações de ameaça associadas
 * a cada mob.
 */
public final class MobThreatManager {

    private static final Map<
            Mob,
            Map<UUID, MobThreatAssessment>
            > THREATS = new WeakHashMap<>();

    private MobThreatManager() {
        // Classe utilitária.
    }

    /**
     * Retorna a avaliação de ameaça de uma entidade
     * em relação ao mob.
     */
    public static MobThreatAssessment getThreat(
            Mob mob,
            UUID targetId
    ) {

        if (mob == null || targetId == null) {
            return MobThreatAssessment.none(
                    targetId
            );
        }

        Map<UUID, MobThreatAssessment> threats =
                THREATS.get(mob);

        if (threats == null) {
            return MobThreatAssessment.none(
                    targetId
            );
        }

        return threats.getOrDefault(
                targetId,
                MobThreatAssessment.none(
                        targetId
                )
        );
    }

    /**
     * Define uma avaliação de ameaça.
     */
    public static void setThreat(
            Mob mob,
            MobThreatAssessment assessment
    ) {

        if (mob == null || assessment == null) {
            return;
        }

        if (assessment.targetId() == null) {
            return;
        }

        Map<UUID, MobThreatAssessment> threats =
                THREATS.computeIfAbsent(
                        mob,
                        ignored -> new HashMap<>()
                );

        threats.put(
                assessment.targetId(),
                assessment
        );
    }

    /**
     * Remove a avaliação de ameaça de uma entidade.
     */
    public static void removeThreat(
            Mob mob,
            UUID targetId
    ) {

        if (mob == null || targetId == null) {
            return;
        }

        Map<UUID, MobThreatAssessment> threats =
                THREATS.get(mob);

        if (threats == null) {
            return;
        }

        threats.remove(targetId);

        if (threats.isEmpty()) {
            THREATS.remove(mob);
        }
    }

    /**
     * Remove todas as ameaças associadas ao mob.
     */
    public static void clearMob(
            Mob mob
    ) {

        if (mob == null) {
            return;
        }

        THREATS.remove(mob);
    }

    /**
     * Remove todas as avaliações de ameaça.
     */
    public static void clear() {
        THREATS.clear();
    }
}