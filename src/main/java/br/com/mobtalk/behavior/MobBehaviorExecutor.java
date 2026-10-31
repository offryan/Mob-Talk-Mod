package br.com.mobtalk.behavior;

import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * Executa fisicamente os comportamentos determinados
 * pelo sistema cognitivo do MobTalk.
 *
 * O executor não substitui a IA vanilla.
 * Ele apenas influencia a navegação, o olhar e o alvo
 * do mob de acordo com o comportamento escolhido.
 */
public final class MobBehaviorExecutor {

    private static final double APPROACH_STOP_DISTANCE = 2.5D;

    private static final double AVOID_DISTANCE = 8.0D;

    private static final double FLEE_DISTANCE = 16.0D;

    private static final double APPROACH_SPEED = 1.0D;

    private static final double AVOID_SPEED = 1.1D;

    private static final double FLEE_SPEED = 1.35D;

    private MobBehaviorExecutor() {
        // Classe utilitária.
    }

    /**
     * Executa o comportamento atual do mob.
     */
    public static void execute(
            Mob mob,
            MobBehavior behavior
    ) {

        if (mob == null || behavior == null) {
            return;
        }

        if (mob.level().isClientSide()) {
            return;
        }

        if (!mob.isAlive()) {
            return;
        }

        switch (behavior.type()) {

            case NONE -> {
                // Nenhuma ação específica.
            }

            case OBSERVE ->
                    executeObserve(
                            mob,
                            behavior
                    );

            case APPROACH ->
                    executeApproach(
                            mob,
                            behavior
                    );

            case ATTACK ->
                    executeAttack(
                            mob,
                            behavior
                    );

            case AVOID ->
                    executeAvoid(
                            mob,
                            behavior
                    );

            case FLEE ->
                    executeFlee(
                            mob,
                            behavior
                    );

            case PROTECT ->
                    executeProtect(
                            mob,
                            behavior
                    );
        }
    }

    /**
     * Faz o mob observar uma entidade.
     *
     * O mob olha para o alvo, mas não altera sua navegação.
     */
    private static void executeObserve(
            Mob mob,
            MobBehavior behavior
    ) {

        if (behavior.targetId() == null) {
            return;
        }

        Entity target =
                findTarget(
                        mob,
                        behavior.targetId()
                );

        if (target == null || !target.isAlive()) {
            return;
        }

        mob.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );
    }

    /**
     * Faz o mob se aproximar do alvo.
     */
    private static void executeApproach(
            Mob mob,
            MobBehavior behavior
    ) {

        if (behavior.targetId() == null) {
            return;
        }

        Entity target =
                findTarget(
                        mob,
                        behavior.targetId()
                );

        if (target == null || !target.isAlive()) {
            return;
        }

        double distance =
                mob.distanceTo(target);

        if (distance <= APPROACH_STOP_DISTANCE) {

            mob.getNavigation().stop();

            mob.getLookControl().setLookAt(
                    target,
                    30.0F,
                    30.0F
            );

            return;
        }

        mob.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

        mob.getNavigation().moveTo(
                target,
                APPROACH_SPEED
        );
    }

    /**
     * Faz o mob atacar o alvo.
     *
     * O alvo é entregue à IA vanilla através de setTarget().
     */
    private static void executeAttack(
            Mob mob,
            MobBehavior behavior
    ) {

        if (behavior.targetId() == null) {
            return;
        }

        Entity target =
                findTarget(
                        mob,
                        behavior.targetId()
                );

        if (target == null || !target.isAlive()) {
            return;
        }

        if (target == mob) {
            return;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        mob.setTarget(
                livingTarget
        );

        mob.getLookControl().setLookAt(
                livingTarget,
                30.0F,
                30.0F
        );

        double distance =
                mob.distanceTo(livingTarget);

        if (distance > APPROACH_STOP_DISTANCE) {

            mob.getNavigation().moveTo(
                    livingTarget,
                    APPROACH_SPEED
            );
        }
    }

    /**
     * Faz o mob evitar o alvo.
     *
     * AVOID representa uma resposta defensiva moderada.
     * O mob tenta aumentar a distância do alvo, mas não
     * utiliza a mesma intensidade de fuga de FLEE.
     */
    private static void executeAvoid(
            Mob mob,
            MobBehavior behavior
    ) {

        if (behavior.targetId() == null) {
            return;
        }

        Entity target =
                findTarget(
                        mob,
                        behavior.targetId()
                );

        if (target == null || !target.isAlive()) {
            return;
        }

        if (target == mob) {
            return;
        }

        /*
         * Remove um eventual alvo ofensivo da IA vanilla.
         *
         * Isso evita que um mob em estado defensivo tente
         * atacar simultaneamente a entidade que está evitando.
         */
        mob.setTarget(null);

        mob.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

        double distance =
                mob.distanceTo(target);

        /*
         * Se já estiver suficientemente distante,
         * não precisamos continuar navegando.
         */
        if (distance >= AVOID_DISTANCE) {

            mob.getNavigation().stop();

            return;
        }

        moveAwayFromTarget(
                mob,
                target,
                AVOID_DISTANCE,
                AVOID_SPEED
        );
    }

    /**
     * Faz o mob fugir do alvo.
     *
     * FLEE representa uma resposta forte à ameaça.
     * O mob tenta criar uma distância significativamente
     * maior entre ele e a entidade ameaçadora.
     */
    private static void executeFlee(
            Mob mob,
            MobBehavior behavior
    ) {

        if (behavior.targetId() == null) {
            return;
        }

        Entity target =
                findTarget(
                        mob,
                        behavior.targetId()
                );

        if (target == null || !target.isAlive()) {
            return;
        }

        if (target == mob) {
            return;
        }

        /*
         * Impede que a IA ofensiva vanilla entre em conflito
         * com a decisão cognitiva de fugir.
         */
        mob.setTarget(null);

        mob.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

        double distance =
                mob.distanceTo(target);

        /*
         * Se já conseguimos uma distância segura,
         * não precisamos continuar fugindo.
         */
        if (distance >= FLEE_DISTANCE) {

            mob.getNavigation().stop();

            return;
        }

        moveAwayFromTarget(
                mob,
                target,
                FLEE_DISTANCE,
                FLEE_SPEED
        );
    }

    /**
     * Executa o comportamento PROTECT.
     *
     * Por enquanto, proteção utiliza a aproximação
     * como comportamento físico básico.
     *
     * A proteção social será refinada posteriormente
     * quando tivermos grupos, aliados e relações sociais
     * mais completas.
     */
    private static void executeProtect(
            Mob mob,
            MobBehavior behavior
    ) {

        if (behavior.targetId() == null) {
            return;
        }

        Entity target =
                findTarget(
                        mob,
                        behavior.targetId()
                );

        if (target == null || !target.isAlive()) {
            return;
        }

        mob.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

        double distance =
                mob.distanceTo(target);

        /*
         * Mantém o mob próximo da entidade que está
         * tentando proteger.
         */
        if (distance > APPROACH_STOP_DISTANCE) {

            mob.getNavigation().moveTo(
                    target,
                    APPROACH_SPEED
            );
        }
    }

    /**
     * Calcula uma posição localizada na direção oposta
     * ao alvo e envia o mob para essa posição.
     *
     * Essa implementação utiliza apenas APIs disponíveis
     * para Mob e não exige PathfinderMob.
     */
    private static void moveAwayFromTarget(
            Mob mob,
            Entity target,
            double desiredDistance,
            double speed
    ) {

        Vec3 mobPosition =
                mob.position();

        Vec3 targetPosition =
                target.position();

        Vec3 direction =
                mobPosition.subtract(
                        targetPosition
                );

        /*
         * Evita normalização de um vetor praticamente nulo.
         */
        if (direction.lengthSqr() < 0.0001D) {

            direction = new Vec3(
                    1.0D,
                    0.0D,
                    0.0D
            );
        }

        Vec3 normalizedDirection =
                direction.normalize();

        /*
         * Calculamos uma posição à frente do mob,
         * seguindo a direção oposta ao alvo.
         */
        Vec3 destination =
                mobPosition.add(
                        normalizedDirection.scale(
                                desiredDistance
                        )
                );

        /*
         * Mantemos a altura atual.
         *
         * Isso evita introduzir movimento vertical artificial
         * em comportamentos terrestres simples.
         */
        destination =
                new Vec3(
                        destination.x,
                        mobPosition.y,
                        destination.z
                );

        mob.getNavigation().moveTo(
                destination.x,
                destination.y,
                destination.z,
                speed
        );
    }

    /**
     * Procura uma entidade pelo UUID no servidor.
     */
    private static Entity findTarget(
            Mob mob,
            UUID targetId
    ) {

        if (mob == null || targetId == null) {
            return null;
        }

        if (!(mob.level()
                instanceof ServerLevel serverLevel)) {
            return null;
        }

        return serverLevel.getEntity(
                targetId
        );
    }
}