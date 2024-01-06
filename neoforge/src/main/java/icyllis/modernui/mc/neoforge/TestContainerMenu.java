/*
 * Modern UI.
 * Copyright (C) 2019-2024 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.mc.neoforge;

import icyllis.modernui.mc.neoforge.MuiRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

/**
 * Example.
 */
@ApiStatus.Internal
public class TestContainerMenu extends AbstractContainerMenu {

    private boolean mDiamond;

    /**
     * This constructor should be only used on client.
     * Indicates that this menu is only used for view layout without any network communication.
     */
    public TestContainerMenu() {
        super(null, 0);
        assert (FMLEnvironment.dist.isClient());
    }

    public TestContainerMenu(int containerId, @Nonnull Inventory inventory, @Nonnull FriendlyByteBuf data) {
        super(MuiRegistries.TEST_MENU.get(), containerId);
        mDiamond = data.readBoolean();
    }

    public TestContainerMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
        super(MuiRegistries.TEST_MENU.get(), containerId);
    }

    /**
     * Called when the container menu is closed, on both side.
     *
     * @param player the player using this menu
     */
    @Override
    public void removed(@Nonnull Player player) {
        super.removed(player);
    }

    /**
     * This method will be called every tick to determine
     * whether the menu should be closed intrinsically. This is a server-side logic.
     * Unless you use {@link #TestContainerMenu()}, you should implement this method.
     * Otherwise, it only depends on certain behaviors of the client.
     *
     * @param player the player using this menu (should be server player)
     * @return {@code false} to close this menu on server, also send a packet to client
     */
    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }

    public boolean isDiamond() {
        return mDiamond;
    }

    @Deprecated
    @Nonnull
    @Override
    protected final DataSlot addDataSlot(@Nonnull DataSlot intValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    protected final void addDataSlots(@Nonnull ContainerData array) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Deprecated
    @Override
    public final void setData(int id, int data) {
        throw new UnsupportedOperationException();
    }
}
