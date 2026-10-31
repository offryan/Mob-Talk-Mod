package br.com.mobtalk.perception;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Sistema responsável pela percepção dos mobs.
 *
 * A percepção identifica entidades vivas próximas
 * ao observador e transforma essas informações em
 * dados que podem ser utilizados pela memória.
 */
public final class MobPerception {

    public static final double DEFAULT_PERCEPTION_RADIUS = 16.0D;

    private MobPerception() {
        // Classe utilitária.
    }

    /**
     * Procura entidades vivas próximas ao observador.
     *
     * @param observer entidade que está percebendo
     * @param radius raio máximo da percepção
     * @return lista de entidades percebidas
     */
    public static List<PerceivedEntity> perceive(
            Entity observer,
            double radius
    ) {

        if (observer == null) {
            return List.of();
        }

        if (radius <= 0.0D) {
            return List.of();
        }

        Level level = observer.level();

        return level.getEntitiesOfClass(
                LivingEntity.class,
                observer.getBoundingBox().inflate(radius),
                entity -> isPerceivable(
                        observer,
                        entity
                )
        ).stream()
                .map(entity ->
                        PerceivedEntity.from(
                                observer,
                                entity
                        )
                )
                .toList();
    }

    /**
     * Determina se uma entidade pode ser percebida.
     */
    private static boolean isPerceivable(
            Entity observer,
            LivingEntity target
    ) {

        if (target == observer) {
            return false;
        }

        return target.isAlive();
    }
}