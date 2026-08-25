# Blockbench sources

The editable Java block models are under `reference_pack/assets/adaptive_functional_storage/models/block`.

The controller, drawer, grid, crafting-grid, and armory models use separate front textures so edits to one block do not overwrite another. `drawer_side_online.png` is the shared casing texture. Files ending in `_static_source.png` preserve the single-frame artwork used to make animated texture sheets.

Online controller and grid textures use four frames at 16 ticks per frame. Drawer screens use the same timing. Offline drawer screens are solid black.

The `palette_*.png` files are color references. Runtime textures live under `src/main/resources/assets/adaptive_functional_storage/textures/block`; copy an edited source there only after checking its model and animation metadata.

Some reference geometry and textures are derived from Functional Storage under the MIT License. See `THIRD_PARTY_NOTICES.md` before redistributing modified source assets.
