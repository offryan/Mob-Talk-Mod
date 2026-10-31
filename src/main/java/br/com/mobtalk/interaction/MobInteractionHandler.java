package br.com.mobtalk.interaction;

import br.com.mobtalk.MobTalk;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Captura acontecimentos de combate do Minecraft
 * e os transforma em interações do MobTalk.
 *
 * Fluxo:
 *
 * Minecraft
 *     ↓
 * LivingDamageEvent.Post
 *     ↓
 * MobInteractionHandler
 *     ↓
 * MobInteractionManager
 *     ↓
 * Memória + Relacionamento
 */
@EventBusSubscriber(modid = MobTalk.MODID)
public final class MobInteractionHandler {

    private MobInteractionHandler() {
        // Classe utilitária.
    }

    /**
     * Detecta quando uma entidade viva sofre dano.
     *
     * O evento Post é utilizado porque o dano já foi
     * processado neste momento.
     */
    @SubscribeEvent
    public static void onLivingDamage(
            LivingDamageEvent.Post event
    ) {

        if (event.getEntity().level().isClientSide()) {
            return;
        }

        /*
         * Apenas nos interessa quando o alvo
         * do dano é um Mob.
         */
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        /*
         * Descobre qual entidade causou o dano.
         */
        Entity attacker =
                event.getSource().getEntity();

        if (attacker == null) {
            return;
        }

        /*
         * Evita registrar dano causado pelo próprio mob.
         */
        if (attacker == mob) {
            return;
        }

        /*
         * Nesta versão, utilizamos o valor original
         * fornecido pela DamageSource como intensidade
         * da interação.
         *
         * Posteriormente podemos utilizar o valor final
         * efetivamente aplicado, caso necessário.
         */
        float damage =
                event.getOriginalDamage();

        if (damage <= 0.0F) {
            return;
        }

        /*
         * Registra a interação social.
         */
        MobInteractionManager.onDamagedBy(
                mob,
                attacker,
                damage
        );
    }
}