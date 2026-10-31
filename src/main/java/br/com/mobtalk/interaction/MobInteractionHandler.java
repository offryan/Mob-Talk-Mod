package br.com.mobtalk.interaction;

import br.com.mobtalk.MobTalk;
import br.com.mobtalk.MobTalkTags;
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
         * Mobs fora do controle do MobTalk também não
         * acumulam memória nem relacionamentos.
         */
        if (MobTalkTags.isIgnored(mob)) {
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
         * O dano original indica se houve intenção de ferir:
         * sem ele, não há ataque a registrar.
         *
         * O dano efetivo (depois de armadura, resistência
         * e escudo) é o que realmente machucou o mob e é
         * usado como intensidade da interação.
         */
        float originalDamage =
                event.getOriginalDamage();

        if (originalDamage <= 0.0F) {
            return;
        }

        float effectiveDamage =
                Math.max(
                        0.0F,
                        event.getNewDamage()
                );

        /*
         * Registra a interação social.
         */
        MobInteractionManager.onDamagedBy(
                mob,
                attacker,
                originalDamage,
                effectiveDamage
        );
    }
}