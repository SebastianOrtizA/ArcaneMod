# Stage 6 — Endgame & Narrative Payoff
## Codex Arcanum · Developer Implementation Plan

**Depends on:** All previous stages (0–5). This is the capstone.
**Covers:** §1.4 (Warden of the Last Circle encounter and its two resolution paths)

---

## 6.1 The Warden of the Last Circle — Boss Entity

### Entity Registration
- [ ] Register entity: `warden_of_the_last_circle`
- [ ] Extends: custom boss mob (not a vanilla Warden reskin — this is a Loomkeeper)
- [ ] Appearance: tall humanoid (~2.5 blocks), wearing broken Loomkeeper Regalia, partially Unwoven
  - One arm is semi-transparent and flickering (the Unmaker's hold)
  - Eyes glow with alternating Wyrd-blue and Fray-purple
  - Faint Weave threads trail off the body, fraying visibly
- [ ] HP: 300 (150 hearts)
- [ ] Armor: 12 (diamond-level)
- [ ] Movement speed: moderate (0.3), increases during enrage phases
- [ ] Boss bar: custom boss bar displayed to all players in the arena

### Arena
- [ ] Located in The Last Circle's inner sanctum (built as part of Stage 5 structure)
- [ ] Arena design: circular room, ~20 block diameter
  - Central pedestal with a broken Anchor Brazier (lore: this was the head researcher's workspace)
  - 4 intact Weave Wellsprings at cardinal points (functional — player can draw Wyrd mid-fight)
  - Fray level in the arena: 80+ (cosmetic desaturation + Frayed mob ambient spawning during fight)
  - Sealed entrance: door locks when boss is engaged, reopens on defeat or player death

---

## 6.2 Boss Fight Mechanics

### Phase 1 — The Guardian (100%–60% HP)
- [ ] The Warden fights defensively, protecting the sanctum
- [ ] Attacks:
  - **Weave Slash**: melee swing (10 damage), medium range (3 blocks), telegraphed with a Weave-thread windup animation (1 second)
  - **Thread Barrage**: fires 5 rapid Wyrd bolts in a spread pattern (4 damage each), 20-block range, 8-second cooldown
  - **Anchored Ground**: every 30 seconds, stamps the ground — creates a 6-block radius slow zone for 5 seconds (slows players by 50%)
- [ ] Behavior: stays near the center pedestal, engages closest player, retreats to center if pulled too far

### Phase 2 — The Broken (60%–25% HP)
- [ ] The Warden becomes more aggressive as the Unmaker's influence surges
- [ ] Visual shift: more flickering, shadow particles increase, some threads snap visibly
- [ ] New attacks (adds to Phase 1):
  - **Unmaker's Pulse**: AoE dark nova (8 damage, 8-block radius), adds 3 Personal Fray to all hit players, 15-second cooldown
  - **Summon Unwoven**: calls 2–3 Unwoven Researchers as adds, 45-second cooldown, max 6 adds alive at once
  - **Fray Surge**: increases arena Regional Fray by 5, spawning more ambient Frayed mobs
- [ ] Behavior: more mobile, will chase players, occasionally teleports short distances (like an Enderman, but with a thread-snap visual)

### Phase 3 — The Last Thread (25%–0% HP)
- [ ] The Warden is barely holding on — visually almost entirely Unwoven
- [ ] Attacks intensify:
  - All previous attacks, with reduced cooldowns (75% of original)
  - **Desperate Grasp**: lunges at the nearest player (10-block range, 15 damage), if it hits it also drains 30 Wyrd from the player's wand
  - **Unraveling**: continuous AoE aura (2 damage/sec, 4-block radius) while in this phase — players must stay mobile
- [ ] At 5% HP: the Warden staggers, stops attacking for 3 seconds (the "choice window" — see 6.3)

---

## 6.3 Two Resolution Paths

The kill resolves differently based on the player's Personal Fray level at the moment of the final blow.

### Path A — Redemption (Personal Fray ≤ 40)
A player who avoided Forbidden Knowledge or kept their Fray low.

- [ ] When HP reaches 0:
  1. The Warden freezes in place, flickering wildly
  2. The Unmaker's shadow visibly peels away from the Warden (dark particles streaming upward and dissolving)
  3. The Warden's appearance briefly becomes clear — a Loomkeeper in intact Regalia, recognizable as whole for one moment
  4. Single line of dialogue (rendered as a title text overlay):
     > *"Maren... you kept the threads. Thank you."*
  5. The Warden dissolves into Wyrd particles (blue/gold), floating gently upward
  6. A warm chime plays, light fills the arena briefly
  7. The broken Anchor Brazier at center repairs itself and fills with Wyrd (permanent functional block)
  8. Drops: Loomkeeper's Regalia pieces, Warden's Thread (unique trinket — see 6.4), clean loot

- [ ] Codex entry: a final page writes itself — Thale's handwriting, acknowledging the player found a way to free her circle-mate without the dark arts. Tone: relieved, grateful, quietly proud.

### Path B — Conquest (Personal Fray > 40)
A player who embraced Forbidden Knowledge.

- [ ] When HP reaches 0:
  1. The Warden staggers but doesn't dissolve — instead, dark tendrils from the player reach out and bind it
  2. The Warden's remaining Weave threads snap one by one (visible, audible)
  3. The Warden tries to speak but can only produce static/whisper:
     > *"...not... like this..."*
  4. The player's shadow visibly grows larger for a moment (cosmetic)
  5. The Warden collapses into a pile of dark crystal shards and shadow particles
  6. An ominous low tone plays, the arena dims temporarily
  7. The broken Anchor Brazier at center cracks further and emits dark Essence permanently (functional: produces Dark Essence passively)
  8. Drops: Duskbound equipment pieces, Hollow Crown (if not already found), Warden's Burden (unique trinket — see 6.4), forbidden loot

- [ ] Codex entry: a final page writes itself — but this time in the Forbidden author's handwriting, not Thale's. Tone: triumphant, possessive, suggesting the player has become what the Loomkeepers feared. A final margin note from Thale, barely legible: *"I'm sorry."*

### Implementation Notes
- [ ] Path detection: at the moment `warden_of_the_last_circle` HP reaches 0, read the killing player's Personal Fray
  - Fray ≤ 40 → Path A
  - Fray > 40 → Path B
  - If multiple players, use the one who dealt the final hit
- [ ] Both paths are equally valid — neither is "wrong." Do NOT gate content behind one path or penalize either choice
- [ ] The cutscene is a scripted sequence (entity animations + particle effects + title text + sound), not a video. Implement as a `BossDeathSequence` class that ticks through steps

---

## 6.4 Unique Rewards

### Warden's Thread (Redemption reward)
- [ ] Register item: `wardens_thread`
- [ ] Slot: charm (Curios)
- [ ] Effect:
  - Passive Personal Fray decay tripled (3× the normal rate)
  - All Wyrd costs reduced by 10%
  - Weave Wellsprings within 16 blocks of the player regenerate 25% faster
- [ ] Visual: a faintly glowing golden thread, coiled around the player's wrist (Curios render)
- [ ] Flavor text: *"A single thread, still holding. That's all it took."*

### Warden's Burden (Conquest reward)
- [ ] Register item: `wardens_burden`
- [ ] Slot: charm (Curios)
- [ ] Effect:
  - +25% all spell damage
  - +15% Wyrd efficiency
  - Personal Fray decay halved (slower cleansing)
  - Forbidden spells cost 30% less Wyrd
- [ ] Visual: a dark crystal shard on a chain, pulsing with shadow
- [ ] Flavor text: *"The weight of what you did. It suits you."*

### Both rewards are roughly equal in power — Thread favors sustainability, Burden favors raw power with a Fray tradeoff.

---

## 6.5 Post-Boss World Effects

### Path A effects
- [ ] The Last Circle's Regional Fray drops to 0 (the sanctum is cleansed)
- [ ] Unwoven mobs stop spawning in The Last Circle permanently
- [ ] The repaired Anchor Brazier becomes the most powerful Wyrd source in the world (2000 capacity, 5× regen rate)
- [ ] A new Weave Wellspring appears at the exact center of the sanctum (ancient-tier, attuned — best possible stats)

### Path B effects
- [ ] The Last Circle's Regional Fray rises to 100 and is locked (cannot be cleansed)
- [ ] The cracked Brazier becomes a Dark Essence source (produces 10 Dark Essence/min passively)
- [ ] Frayed mobs in the structure become stronger (+50% stats) but drop better loot
- [ ] A unique worldgen event: for 3 real-time days after the boss kill, Frayed mobs spawn slightly more frequently world-wide (1.2× rate) — the Unmaker "noticed" what happened. Decays back to normal afterward.

---

## 6.6 Codex Final Pages

### Narrative Wrap-up
- [ ] Both paths unlock a final Codex section (after Section 7): "Epilogue"
- [ ] Path A Epilogue (2 pages):
  - Page 1: Thale's handwriting. Reflects on the journey — "You found what we lost. Not the power — the restraint." References the Weave healing, the threads holding.
  - Page 2: A final sketch — the Loomkeeper's sigil, intact. Below it: "If you're reading this, the Weave still has a chance. Keep it whole."
- [ ] Path B Epilogue (2 pages):
  - Page 1: Forbidden author's handwriting. Exultant — "They said it was forbidden because they were afraid. You weren't." References the player's power, the Unmaker's approval.
  - Page 2: Thale's handwriting, barely legible, smudged. Just one line: *"I should have burned this book."*
  - Below Thale's line, in the Forbidden author's hand: *"Too late."*

---

## 6.7 Advancement / Achievement Triggers

- [ ] `codexarcanum:the_first_thread` — Complete the Awakening research (Section 1)
- [ ] `codexarcanum:weaver` — Complete all Section 1–2 research
- [ ] `codexarcanum:sorcerer` — Learn all clean spells (Section 3)
- [ ] `codexarcanum:ritualist` — Perform your first ritual (Section 4)
- [ ] `codexarcanum:alchemist` — Craft your first potion in the Cauldron (Section 5)
- [ ] `codexarcanum:fully_equipped` — Wear a complete Regalia or Duskbound set
- [ ] `codexarcanum:a_warning_ignored` — Read your first Sable Page
- [ ] `codexarcanum:the_last_circle` — Enter The Last Circle structure
- [ ] `codexarcanum:redemption` — Defeat the Warden via Path A
- [ ] `codexarcanum:conquest` — Defeat the Warden via Path B
- [ ] `codexarcanum:master_weaver` — Complete every research node in the Codex (hidden advancement)

---

## 6.8 Data Generation

- [ ] Entity model/texture/animations: Warden of the Last Circle (3 phase appearances, death sequences for both paths)
- [ ] Custom animations: Weave Slash windup, Thread Barrage, Anchored Ground stamp, Unmaker's Pulse, Desperate Grasp, teleport, death sequences
- [ ] Unique items: Warden's Thread, Warden's Burden (models + Curios renderers)
- [ ] Sound events: boss music (ambient loop per phase), Weave Slash, Thread Barrage, Unmaker's Pulse, Redemption chime, Conquest tone, dialogue whispers
- [ ] Particle types: Unmaker shadow peel, golden dissolution, thread-snap, shadow bind tendrils, Fray surge
- [ ] Codex page content: Epilogue pages for both paths
- [ ] Advancement JSON definitions for all milestones
- [ ] Language file entries
- [ ] Boss bar texture/color configuration

---

## 6.9 Testing Checklist

- [ ] Warden spawns in The Last Circle sanctum and engages correctly
- [ ] Phase transitions happen at correct HP thresholds with visual changes
- [ ] All attacks work: Weave Slash, Thread Barrage, Anchored Ground, Unmaker's Pulse, Summon Unwoven, Fray Surge, Desperate Grasp, Unraveling
- [ ] Summoned Unwoven adds spawn and cap at 6
- [ ] Arena seals on engagement and reopens on resolution
- [ ] Path A triggers correctly when killer's Fray ≤ 40
- [ ] Path A death sequence plays: shadow peels, dialogue, golden dissolution, Brazier repair
- [ ] Path A drops correct loot including Warden's Thread
- [ ] Path B triggers correctly when killer's Fray > 40
- [ ] Path B death sequence plays: dark binding, dialogue, collapse, Brazier cracks
- [ ] Path B drops correct loot including Warden's Burden
- [ ] Post-boss world effects apply correctly for each path
- [ ] Codex Epilogue pages appear with correct text and style per path
- [ ] Advancements trigger at the right moments
- [ ] Boss is challenging but fair for both clean and Forbidden loadouts
- [ ] Multiplayer: boss scales or maintains challenge with multiple players
- [ ] Boss bar renders correctly for all players in the arena
