# Adaptive Functional Storage 0.2.0

This update rebuilds Adaptive Functional Storage as a closer extension of Functional Storage rather than a separate drawer network.

## Adaptive Matrix integration

- Replaced the standalone Adaptive Controller identity with the Adaptive Matrix.
- The Adaptive Matrix now joins a Functional Storage Controller network as a linked storage endpoint.
- Adaptive Drawers connect to their Matrix automatically; they do not need to be linked to the Functional Storage Controller one at a time.
- Functional Storage drawers and Adaptive Drawers are available together through the Storage Grid, Crafting Grid, Deposit Port, and Functional Storage Controller.
- The Matrix consumes `20 FE/t + 5 FE/t` for each linked, loaded Adaptive Drawer.
- Empty-hand use on the Matrix reports its current FE/t consumption and stored energy.
- When power is unavailable, adaptive storage remains intact but its network access and displays turn off until power returns.

## Adaptive Drawers

- Added Adaptive Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Mangrove, Cherry, Crimson, and Warped Drawers.
- Adaptive Drawers retain Functional Storage's native wood casing and physical divider geometry while replacing the drawer faces with animated digital screens.
- Added the Adaptive Upgrade. Craft it with a clean matching Functional Storage drawer to create its adaptive variant.
- Conversion recipes accept Functional Storage 1x1, 1x2, and 2x2 drawers, but reject drawers that already contain items, upgrades, or saved configuration.
- Corrected 2x2 face selection so withdrawing an item uses the section actually targeted.
- Corrected adaptive rebalancing so changing one face does not hide an item or leave the array with insufficient visible cells.
- Updated powered screens with scan-line animation, matching section edges, emissive treatment, and solid-black offline states.

## Storage and crafting access

- Renamed Adaptive Grid and Adaptive Crafting Grid to Storage Grid and Crafting Grid.
- Both grids now read the complete linked Functional Storage network, including ordinary FS drawers and the Adaptive Matrix.
- Corrected item identity and quantity mapping for Functional Storage drawer contents.
- Corrected clicks that displayed one item but withdrew another.
- Crafting Grid recipes now refill their crafting cells from network storage after crafting.
- Corrected EMI recipe transfer and craft-max accounting to prevent duplicated ingredients or one extra craft.
- Network and crafting counts now refresh automatically after EMI transfer and crafting.
- Updated both grid blocks with new Functional Storage-inspired models, animated powered screens, offline states, and proper 3D inventory icons.

## Deposit Port and cleanup

- Renamed Adaptive Deposit to Deposit Port.
- Deposit Port insertion now uses the complete Functional Storage network rather than only the Adaptive Matrix inventory.
- Added new Deposit Port and Adaptive Upgrade artwork.
- Removed the Extender, Armory Drawer, and Fluid Drawer from obtainable items, recipes, and the creative tab; Functional Storage's equivalent blocks remain the intended options.
- Updated remaining block inventory icons to use their 3D models.

## Requirements and compatibility

- Minecraft 1.21.1
- NeoForge 21.1 or newer
- Functional Storage 1.5.5 or newer (required)

Back up important worlds before updating from 0.1.0. This release changes the network architecture and removes several prototype items from normal availability.
