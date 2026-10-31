package br.com.mobtalk.intent;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.Mob;

/**
 * Gerencia a intenção atual de cada mob.
 *
 * Cada mob possui sua própria intenção.
 *
 * A intenção é recalculada periodicamente pelo sistema
 * de inteligência do MobTalk.
 *
 * Esta classe ainda não executa nenhum comportamento.
 *
 * Sua responsabilidade é somente armazenar o estado
 * cognitivo atual do mob.
 */
public final class MobIntentManager {

    private static final Map<Mob, MobIntent> INTENTS =
            new WeakHashMap<>();

    private MobIntentManager() {
        // Classe utilitária.
    }

    /**
     * Obtém a intenção atual de um mob.
     *
     * Caso ainda não exista uma intenção registrada,
     * retorna uma intenção neutra.
     */
    public static MobIntent getIntent(
            Mob mob
    ) {

        if (mob == null) {
            return MobIntent.none();
        }

        return INTENTS.getOrDefault(
                mob,
                MobIntent.none()
        );
    }

    /**
     * Define a intenção atual de um mob.
     */
    public static void setIntent(
            Mob mob,
            MobIntent intent
    ) {

        if (mob == null || intent == null) {
            return;
        }

        INTENTS.put(
                mob,
                intent
        );
    }

    /**
     * Remove a intenção de um mob.
     */
    public static void removeIntent(
            Mob mob
    ) {

        if (mob == null) {
            return;
        }

        INTENTS.remove(mob);
    }

    /**
     * Limpa todas as intenções armazenadas.
     */
    public static void clear() {
        INTENTS.clear();
    }
}