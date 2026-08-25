# Functional coverage

Updated for 0.1.0 on 2026-08-24.

## Network structure

An Adaptive Controller covers a 24-block Chebyshev radius. Addon-owned blocks join automatically when they are placed within one valid network. Functional Storage's Linking Tool handles blocks that need an explicit controller assignment. Extenders add another bounded 24-block coverage area without force-loading chunks.

The Controller owns the item ledger. Drawer faces are views of that ledger, not separate inventories. This keeps automatic layout changes from moving physical stacks between block entities.

## Functional Storage features

| Functional Storage feature | Adaptive Functional Storage behavior |
|---|---|
| Wood drawers | Accepted as recipe inputs for the Adaptive Drawer. |
| Storage Controller | Accepted as a recipe input for the Adaptive Controller. The two controller types do not merge networks. |
| Linking Tool | Selects an Adaptive Controller and assigns supported addon blocks to it. |
| Configuration Tool | Used to craft the purple Adaptive Configuration Tool. |
| Storage upgrades | Not used. Adaptive storage has one capacity tier. |
| Compacting drawers | Left to Functional Storage. Automatic item conversion is outside this mod's scope. |
| Fluid drawer | Used as the recipe input and model basis for the Adaptive Fluid Drawer. |
| Armory Cabinet | Used as the recipe input for the Adaptive Armory. |

## Included in 0.1.0

- Balanced, Proportional, and Manual drawer layouts.
- Sorting by Count, Mod, Item ID, Tag, or Category.
- Six directions for Count priority.
- Persistent drawer-size pins, item locks, offhand assignments, and empty-cell reservations.
- Storage and Crafting Grids.
- Deposit, Extender, Armory, and Fluid Drawer blocks.
- FE-powered online and offline states.
- Safe controller handoff and a 64-stack limit for world drops during drawer removal.
- Item and fluid capabilities for automation.

## Follow-up work

- Show controller energy and network diagnostics in a dedicated screen.
- Bound and cache extender-chain bookkeeping for larger installations.
- Add fluid entries to the storage grids.
- Add automated persistence, topology, transfer-simulation, and removal-safety tests.
- Define recovery for forced or administrative block removal when normal break events are bypassed.
- Add framed variants only if they can share the existing ownership and safety rules.

Wireless storage, compacting drawers, and automatic crafting are not part of the 0.1 design.
