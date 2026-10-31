# 🐄 Mob Talk Mod

Mod para Minecraft 1.21.1 (NeoForge) que dá aos mobs vanilla percepção, memória, relacionamentos e decisões próprias. A ideia é que cada mob guarde um histórico das suas experiências e reaja ao mundo de forma coerente com ele, sem substituir a IA original do jogo.

> 🚧 **Em desenvolvimento.** Ainda não há versão publicada. A primeira fase trabalha só com mobs vanilla, e toda a lógica roda no servidor (pronta para servidor dedicado e multiplayer).

## ✨ Funcionalidades

- **Percepção:** cada mob identifica as entidades vivas num raio de 16 blocos.
- **Memória individual:** o que o mob viu (tipo, distância, última vez visto) e um histórico de combate por entidade (quantidade de ataques, dano efetivo total, maior dano, último ataque). Quem atacou o mob continua lembrado por cerca de 10 minutos, mesmo fora de vista.
- **Contexto recente:** acontecimentos dos últimos 10 segundos, como "acabei de levar dano daquele jogador", separados da memória de longo prazo.
- **Relacionamentos:** familiaridade, hostilidade e confiança de cada mob com cada entidade. A hostilidade cai pela metade a cada 5 minutos sem nova agressão.
- **Avaliação de ameaça:** pontuação de 0 a 1 (`NONE` a `CRITICAL`) que junta o ataque recente, a lembrança dos ataques antigos (que vai se apagando) e a hostilidade. Fugir exige ataque recente; a lembrança sozinha só deixa o mob cauteloso.
- **Perfis de comportamento:** passivo, neutro, agressivo e defensivo respondem de jeitos diferentes à mesma ameaça.
- **Intenção com estabilidade:** observar, aproximar, evitar, fugir, atacar ou proteger, sem trocar de ideia a cada oscilação da ameaça.
- **Execução física:** o mob olha, se aproxima, se afasta ou foge usando a navegação, o olhar e o alvo do próprio Minecraft, só para alvos a até 16 blocos. Fora disso, e sem ameaça, a IA vanilla segue no controle.
- **Persistência:** memória e relacionamentos ficam gravados no próprio mob (attachments do NeoForge) e sobrevivem ao descarregamento do chunk, à troca de dimensão e ao reinício do mundo.
- **Mobs ignorados:** a tag `mobtalk:ignored` tira chefes e mobs especiais do controle do mod (Ender Dragon, Wither, Warden e Elder Guardian por padrão); modpacks podem estendê-la.
- **Comunicação interna:** mensagens entre entidades publicadas como eventos (base para o diálogo futuro).

Já testado no jogo: uma vaca atacada foge, passa a evitar o jogador depois que a fuga termina, se acalma conforme a lembrança decai e continua lembrando dos ataques depois de salvar e reabrir o mundo.

## 🧠 Como funciona

Um ciclo cognitivo roda a cada 10 ticks para cada mob, no servidor:

```mermaid
flowchart LR
  M[Mundo] --> P[Percepção]
  D[Dano recebido] --> I[Interação]
  P --> Me[Memória]
  I --> Me
  I --> C[Contexto]
  I --> R[Relacionamento]
  Me --> A[Ameaça]
  C --> A
  R --> A
  A --> Pf[Perfil]
  Pf --> In[Intenção]
  In --> De[Decisão]
  De --> Co[Comportamento]
  Co --> E[Executor: navegação, olhar e alvo vanilla]
```

| Pacote | Responsabilidade |
|---|---|
| `perception` | Encontrar entidades próximas |
| `memory` | Conhecimento acumulado por entidade |
| `context` | Acontecimentos recentes e temporários |
| `interaction` | Transformar eventos do jogo (dano) em interações |
| `social` | Familiaridade, hostilidade e confiança |
| `threat` | Calcular o nível de ameaça |
| `profile` | Como cada tipo de mob reage à ameaça |
| `intent` / `decision` | O que o mob pretende fazer e a decisão operacional |
| `behavior` | Traduzir a decisão em ação física |
| `cognition` | Coordenar o ciclo completo |
| `communication` | Mensagens entre entidades |

## 🗺️ Próximos passos

- [x] Validar no jogo a memória acumulada de combate
- [x] Fazer o contexto recente expirar e calcular a ameaça uma vez por ciclo
- [x] Integrar a memória histórica ao cálculo de ameaça, com esquecimento e decaimento da hostilidade
- [x] Deixar o executor menos invasivo com a IA vanilla e filtrar chefes
- [x] Salvar memórias e relacionamentos no mundo
- [ ] Observar mobs guiados por Brain (villager, piglin) e ajustar se o executor atrapalhar
- [ ] Novas interações: ajuda, proteção, alimentação, troca, morte de aliados
- [ ] Comunicação de verdade com jogadores e entre mobs

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Jogo | Minecraft 1.21.1 |
| Loader | NeoForge 21.1.250 |
| Linguagem | Java 21 |
| Build | Gradle 9.2.1 + ModDevGradle 2.0.147 |
| Mappings | Parchment 2024.11.17 |

## ▶️ Rodando localmente

Pré-requisitos: JDK 21.

```powershell
git clone https://github.com/offryan/Mob-Talk-Mod.git
cd Mob-Talk-Mod
.\gradlew build       # gera build/libs/mobtalk-1.0.0.jar
.\gradlew runClient   # abre o Minecraft de desenvolvimento com o mod
```

Os logs de diagnóstico (`MobTalk AMEAÇA`, `MobTalk INTENÇÃO`, `MobTalk MEMÓRIA DE COMBATE`...) aparecem no console e em `run/logs/debug.log`.

## 📄 Licença

© 2026 Ryan G. Santos. Todos os direitos reservados. O código está público apenas para portfólio e avaliação: não é permitido copiar, modificar, distribuir ou usar comercialmente sem autorização por escrito. Veja o arquivo [LICENSE](LICENSE).
