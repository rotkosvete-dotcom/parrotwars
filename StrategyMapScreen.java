package com.parrotwars.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.parrotwars.ParrotWars;
import com.parrotwars.entity.ArmyUnit;
import com.parrotwars.entity.EnemyUnit;
import com.parrotwars.entity.WarVehicleEntity;
import com.parrotwars.network.CountryActionPacket;
import com.parrotwars.network.CountrySyncPacket;
import com.parrotwars.network.ModNetwork;
import com.parrotwars.network.OrderPacket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.Heightmap;

/**
 * Стратегическая карта.
 * Режим "Командование": ЛКМ - выбрать (клик или рамка), ПКМ - приказ (идти / атаковать врага).
 * Режим "Территория": ЛКМ - занять чанк рядом с границей, ПКМ - отпустить чанк.
 */
public class StrategyMapScreen extends Screen {
    private static final int N = 192;
    private static final ResourceLocation TEX_ID = new ResourceLocation(ParrotWars.MODID, "dynamic/strategy_map");

    private DynamicTexture texture;
    private int minX;
    private int minZ;
    private int mapX;
    private int mapY;
    private int size;
    private double scale;

    private final Set<Integer> selected = new HashSet<>();
    private boolean territoryMode = false;
    private boolean dragging = false;
    private double dragX0;
    private double dragY0;
    private double dragX1;
    private double dragY1;

    private TextFieldWidget nameField;
    private Button modeButton;

    public StrategyMapScreen() {
        super(new TranslationTextComponent("screen.parrotwars.map"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------- Построение ----------

    @Override
    protected void init() {
        size = Math.max(128, Math.min(this.height - 40, this.width - 190));
        mapX = 10;
        mapY = 20;
        scale = size / (double) N;

        if (texture == null) {
            buildTerrain();
        }

        int bx = mapX + size + 12;
        int bw = Math.min(160, this.width - bx - 6);
        int by = 20;
        this.addButton(new Button(bx, by, bw, 20, new TranslationTextComponent("screen.parrotwars.select_all"),
                b -> selectAll()));
        by += 24;
        this.addButton(new Button(bx, by, bw, 20, new TranslationTextComponent("screen.parrotwars.follow"),
                b -> sendSimple(OrderPacket.FOLLOW)));
        by += 24;
        this.addButton(new Button(bx, by, bw, 20, new TranslationTextComponent("screen.parrotwars.hold"),
                b -> sendSimple(OrderPacket.HOLD)));
        by += 30;
        modeButton = this.addButton(new Button(bx, by, bw, 20, modeLabel(), b -> {
            territoryMode = !territoryMode;
            b.setMessage(modeLabel());
        }));
        by += 36;
        nameField = new TextFieldWidget(this.font, bx, by, bw, 18, new StringTextComponent(""));
        nameField.setMaxLength(24);
        if (nameField.getValue().isEmpty()) {
            nameField.setValue("Parrotland");
        }
        this.addButton(nameField);
        by += 22;
        this.addButton(new Button(bx, by, bw, 20, new TranslationTextComponent("screen.parrotwars.found"),
                b -> sendCountry(CountryActionPacket.FOUND, 0, 0)));
        by += 30;
        this.addButton(new Button(bx, by, bw, 20, new TranslationTextComponent("screen.parrotwars.close"),
                b -> this.onClose()));
    }

    private TranslationTextComponent modeLabel() {
        return new TranslationTextComponent(territoryMode ? "screen.parrotwars.mode_territory" : "screen.parrotwars.mode_command");
    }

    private void buildTerrain() {
        Minecraft mc = Minecraft.getInstance();
        ClientWorld world = mc.level;
        PlayerEntity player = mc.player;
        if (world == null || player == null) {
            return;
        }
        minX = MathHelper.floor(player.getX()) - N / 2;
        minZ = MathHelper.floor(player.getZ()) - N / 2;

        int[] heights = new int[N * N];
        int[] colors = new int[N * N];
        for (int z = 0; z < N; z++) {
            for (int x = 0; x < N; x++) {
                int wx = minX + x;
                int wz = minZ + z;
                BlockPos probe = new BlockPos(wx, 64, wz);
                if (!world.hasChunkAt(probe)) {
                    heights[z * N + x] = -1;
                    continue;
                }
                int h = world.getHeight(Heightmap.Type.WORLD_SURFACE, wx, wz);
                BlockPos top = new BlockPos(wx, Math.max(h - 1, 0), wz);
                BlockState state = world.getBlockState(top);
                heights[z * N + x] = h;
                colors[z * N + x] = state.getMapColor(world, top).col;
            }
        }

        NativeImage img = new NativeImage(N, N, false);
        for (int z = 0; z < N; z++) {
            for (int x = 0; x < N; x++) {
                int h = heights[z * N + x];
                if (h < 0) {
                    img.setPixelRGBA(x, z, 0xFF202020);
                    continue;
                }
                int rgb = colors[z * N + x];
                if (rgb == 0) {
                    rgb = 0x303030;
                }
                float shade = 1.0F;
                if (z > 0 && heights[(z - 1) * N + x] >= 0) {
                    int diff = MathHelper.clamp(h - heights[(z - 1) * N + x], -3, 3);
                    shade = 1.0F + diff * 0.10F;
                }
                int r = MathHelper.clamp((int) (((rgb >> 16) & 255) * shade), 0, 255);
                int g = MathHelper.clamp((int) (((rgb >> 8) & 255) * shade), 0, 255);
                int b = MathHelper.clamp((int) ((rgb & 255) * shade), 0, 255);
                img.setPixelRGBA(x, z, 0xFF000000 | (b << 16) | (g << 8) | r);
            }
        }
        texture = new DynamicTexture(img);
        mc.getTextureManager().register(TEX_ID, texture);
    }

    @Override
    public void removed() {
        super.removed();
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(TEX_ID);
            texture = null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (nameField != null) {
            nameField.tick();
        }
    }

    // ---------- Координаты ----------

    private int toScreenX(double wx) {
        return (int) (mapX + (wx - minX) * scale);
    }

    private int toScreenY(double wz) {
        return (int) (mapY + (wz - minZ) * scale);
    }

    private double toWorldX(double sx) {
        return minX + (sx - mapX) / scale;
    }

    private double toWorldZ(double sy) {
        return minZ + (sy - mapY) / scale;
    }

    private boolean inMap(double mx, double my) {
        return mx >= mapX && my >= mapY && mx < mapX + size && my < mapY + size;
    }

    // ---------- Юниты ----------

    private List<LivingEntity> unitsOnMap(final boolean mineOnly) {
        Minecraft mc = Minecraft.getInstance();
        final PlayerEntity me = mc.player;
        List<LivingEntity> result = new ArrayList<>();
        if (mc.level == null || me == null) {
            return result;
        }
        AxisAlignedBB box = new AxisAlignedBB(minX, 0, minZ, minX + N, 256, minZ + N);
        for (LivingEntity e : mc.level.getEntitiesOfClass(LivingEntity.class, box,
                x -> x instanceof ArmyUnit || x instanceof EnemyUnit)) {
            boolean mine = e instanceof ArmyUnit && ((ArmyUnit) e).isOwnedByPlayer(me);
            if (mineOnly ? mine : (mine || e instanceof EnemyUnit)) {
                result.add(e);
            }
        }
        return result;
    }

    private boolean isMine(LivingEntity e) {
        PlayerEntity me = Minecraft.getInstance().player;
        return e instanceof ArmyUnit && me != null && ((ArmyUnit) e).isOwnedByPlayer(me);
    }

    private void selectAll() {
        selected.clear();
        for (LivingEntity e : unitsOnMap(true)) {
            selected.add(e.getId());
        }
    }

    // ---------- Отправка ----------

    private int[] selectedArray() {
        int[] arr = new int[selected.size()];
        int i = 0;
        for (int id : selected) {
            arr[i++] = id;
        }
        return arr;
    }

    private void sendSimple(int mode) {
        if (selected.isEmpty()) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(new OrderPacket(mode, selectedArray(), 0, 0, 0));
    }

    private void sendCountry(int action, int cx, int cz) {
        String name = nameField == null ? "" : nameField.getValue();
        ModNetwork.CHANNEL.sendToServer(new CountryActionPacket(action, name, cx, cz));
    }

    // ---------- Мышь ----------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) {
            return true;
        }
        if (!inMap(mx, my)) {
            return false;
        }
        if (territoryMode) {
            int cx = MathHelper.floor(toWorldX(mx)) >> 4;
            int cz = MathHelper.floor(toWorldZ(my)) >> 4;
            if (button == 0) {
                sendCountry(CountryActionPacket.CLAIM, cx, cz);
            } else if (button == 1) {
                sendCountry(CountryActionPacket.UNCLAIM, cx, cz);
            }
            return true;
        }
        if (button == 0) {
            dragging = true;
            dragX0 = mx;
            dragY0 = my;
            dragX1 = mx;
            dragY1 = my;
            return true;
        }
        if (button == 1) {
            rightClick(mx, my);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging && button == 0) {
            dragX1 = mx;
            dragY1 = my;
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging && button == 0) {
            dragging = false;
            dragX1 = mx;
            dragY1 = my;
            finishSelection();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    private void finishSelection() {
        boolean add = hasShiftDown();
        double w = Math.abs(dragX1 - dragX0);
        double h = Math.abs(dragY1 - dragY0);
        if (w < 5 && h < 5) {
            LivingEntity best = null;
            double bestD = 8.0 * 8.0;
            for (LivingEntity e : unitsOnMap(true)) {
                double dx = toScreenX(e.getX()) - dragX1;
                double dy = toScreenY(e.getZ()) - dragY1;
                double d = dx * dx + dy * dy;
                if (d < bestD) {
                    bestD = d;
                    best = e;
                }
            }
            if (best == null) {
                if (!add) {
                    selected.clear();
                }
            } else if (add) {
                if (!selected.remove(best.getId())) {
                    selected.add(best.getId());
                }
            } else {
                selected.clear();
                selected.add(best.getId());
            }
        } else {
            double x0 = Math.min(dragX0, dragX1);
            double x1 = Math.max(dragX0, dragX1);
            double y0 = Math.min(dragY0, dragY1);
            double y1 = Math.max(dragY0, dragY1);
            if (!add) {
                selected.clear();
            }
            for (LivingEntity e : unitsOnMap(true)) {
                int sx = toScreenX(e.getX());
                int sy = toScreenY(e.getZ());
                if (sx >= x0 && sx <= x1 && sy >= y0 && sy <= y1) {
                    selected.add(e.getId());
                }
            }
        }
    }

    private void rightClick(double mx, double my) {
        if (selected.isEmpty()) {
            return;
        }
        LivingEntity enemy = null;
        double bestD = 9.0 * 9.0;
        for (LivingEntity e : unitsOnMap(false)) {
            if (isMine(e)) {
                continue;
            }
            double dx = toScreenX(e.getX()) - mx;
            double dy = toScreenY(e.getZ()) - my;
            double d = dx * dx + dy * dy;
            if (d < bestD) {
                bestD = d;
                enemy = e;
            }
        }
        if (enemy != null) {
            ModNetwork.CHANNEL.sendToServer(new OrderPacket(OrderPacket.ATTACK, selectedArray(), 0, 0, enemy.getId()));
        } else {
            int wx = MathHelper.floor(toWorldX(mx));
            int wz = MathHelper.floor(toWorldZ(my));
            ModNetwork.CHANNEL.sendToServer(new OrderPacket(OrderPacket.MOVE, selectedArray(), wx, wz, 0));
        }
    }

    // ---------- Отрисовка ----------

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        fill(ms, mapX - 2, mapY - 2, mapX + size + 2, mapY + size + 2, 0xFF000000);
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().bind(TEX_ID);
            blit(ms, mapX, mapY, size, size, 0.0F, 0.0F, N, N, N, N);
        }

        drawTerritory(ms);
        drawUnits(ms);
        drawPlayer(ms);

        if (dragging) {
            int x0 = (int) Math.min(dragX0, dragX1);
            int x1 = (int) Math.max(dragX0, dragX1);
            int y0 = (int) Math.min(dragY0, dragY1);
            int y1 = (int) Math.max(dragY0, dragY1);
            fill(ms, x0, y0, x1, y0 + 1, 0xFF00FF00);
            fill(ms, x0, y1, x1, y1 + 1, 0xFF00FF00);
            fill(ms, x0, y0, x0 + 1, y1, 0xFF00FF00);
            fill(ms, x1, y0, x1 + 1, y1, 0xFF00FF00);
        }

        drawInfo(ms, mouseX, mouseY);
        super.render(ms, mouseX, mouseY, partialTicks);
    }

    private void drawTerritory(MatrixStack ms) {
        for (CountrySyncPacket.View v : ClientHooks.views) {
            int fillColor = (0x66 << 24) | (v.color & 0xFFFFFF);
            int lineColor = (0xFF << 24) | (v.color & 0xFFFFFF);
            for (long c : v.chunks) {
                ChunkPos cp = new ChunkPos(c);
                int x0 = toScreenX(cp.x * 16.0);
                int y0 = toScreenY(cp.z * 16.0);
                int x1 = toScreenX(cp.x * 16.0 + 16.0);
                int y1 = toScreenY(cp.z * 16.0 + 16.0);
                int cx0 = Math.max(x0, mapX);
                int cy0 = Math.max(y0, mapY);
                int cx1 = Math.min(x1, mapX + size);
                int cy1 = Math.min(y1, mapY + size);
                if (cx0 >= cx1 || cy0 >= cy1) {
                    continue;
                }
                fill(ms, cx0, cy0, cx1, cy1, fillColor);
                fill(ms, cx0, cy0, cx1, cy0 + 1, lineColor);
                fill(ms, cx0, cy1 - 1, cx1, cy1, lineColor);
                fill(ms, cx0, cy0, cx0 + 1, cy1, lineColor);
                fill(ms, cx1 - 1, cy0, cx1, cy1, lineColor);
            }
        }
        if (territoryMode) {
            int startChunkX = Math.floorDiv(minX, 16);
            int startChunkZ = Math.floorDiv(minZ, 16);
            for (int i = 0; i <= N / 16 + 1; i++) {
                int sx = toScreenX((startChunkX + i) * 16.0);
                int sy = toScreenY((startChunkZ + i) * 16.0);
                if (sx >= mapX && sx < mapX + size) {
                    fill(ms, sx, mapY, sx + 1, mapY + size, 0x33FFFFFF);
                }
                if (sy >= mapY && sy < mapY + size) {
                    fill(ms, mapX, sy, mapX + size, sy + 1, 0x33FFFFFF);
                }
            }
        }
    }

    private void drawUnits(MatrixStack ms) {
        for (LivingEntity e : unitsOnMap(false)) {
            boolean mine = isMine(e);
            int sx = toScreenX(e.getX());
            int sy = toScreenY(e.getZ());
            int r = (e instanceof WarVehicleEntity) ? 4 : 2;
            if (mine && selected.contains(e.getId())) {
                fill(ms, sx - r - 2, sy - r - 2, sx + r + 2, sy + r + 2, 0xFFFFFFFF);
            }
            fill(ms, sx - r, sy - r, sx + r, sy + r, mine ? 0xFFFFD700 : 0xFFE53935);
        }
    }

    private void drawPlayer(MatrixStack ms) {
        PlayerEntity me = Minecraft.getInstance().player;
        if (me == null) {
            return;
        }
        int sx = toScreenX(me.getX());
        int sy = toScreenY(me.getZ());
        fill(ms, sx - 3, sy - 3, sx + 3, sy + 3, 0xFF000000);
        fill(ms, sx - 2, sy - 2, sx + 2, sy + 2, 0xFF00E676);
    }

    private void drawInfo(MatrixStack ms, int mouseX, int mouseY) {
        int bx = mapX + size + 12;
        int y = 22 + 24 * 3 + 6 + 30 + 36 - 12;
        drawString(ms, this.font, new TranslationTextComponent("screen.parrotwars.selected", selected.size()), bx, 4, 0xFFFFFF);
        drawString(ms, this.font, new TranslationTextComponent("screen.parrotwars.country_name"), bx, y, 0xAAAAAA);

        String myCountry = "-";
        int chunks = 0;
        for (CountrySyncPacket.View v : ClientHooks.views) {
            if (v.mine) {
                myCountry = v.name;
                chunks = v.chunks.length;
            }
        }
        int infoY = this.height - 40;
        drawString(ms, this.font, new TranslationTextComponent("screen.parrotwars.my_country", myCountry, chunks),
                bx, infoY, 0xFFD700);
        if (inMap(mouseX, mouseY)) {
            int wx = MathHelper.floor(toWorldX(mouseX));
            int wz = MathHelper.floor(toWorldZ(mouseY));
            drawString(ms, this.font, "X: " + wx + "  Z: " + wz, bx, infoY + 14, 0xCCCCCC);
        }
        String hint = territoryMode
                ? new TranslationTextComponent("screen.parrotwars.hint_territory").getString()
                : new TranslationTextComponent("screen.parrotwars.hint_command").getString();
        drawString(ms, this.font, hint, mapX, 6, 0xDDDDDD);
    }
}
