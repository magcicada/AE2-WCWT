package com.lhy.wcwt.compat.jei;

import appeng.api.stacks.GenericStack;
import com.lhy.wcwt.WcwtMod;
import com.lhy.wcwt.helpers.WcwtWirelessFeatures;
import com.lhy.wcwt.network.ModNetworking;
import com.lhy.wcwt.network.WcwtJeiBookmarkOrderPacket;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import mezz.jei.common.input.interaction.InputAction;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.input.IngredientFocusSource;
import mezz.jei.gui.input.InputCommands;
import net.minecraft.client.Minecraft;

import java.util.Optional;

/** Full-JEI command integration for the WCWT ingredient crafting shortcut. */
public final class WcwtJeiBookmarkOrder {
    private static final boolean DEBUG = Boolean.getBoolean("wcwt.debug.jeiBookmark");

    private WcwtJeiBookmarkOrder() {
    }

    public static void registerInputCommands(InputCommands commands, IngredientFocusSource focusSource,
                                             IInternalKeyMappings keys) {
        commands.add("WCWT open ingredient crafting", input -> {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.options != null && input.is(minecraft.options.keyPickItem)
                    && !input.is(keys.getBookmark());
        }, (screen, input) -> prepareCraftAction(focusSource, input, keys));
    }

    private static Optional<IInputInteraction> prepareCraftAction(IngredientFocusSource focusSource,
                                                                  UserInput input,
                                                                  IInternalKeyMappings keys) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !WcwtWirelessFeatures.hasAnyTerminal(minecraft.player)) {
            return Optional.empty();
        }
        Optional<IClickableIngredientInternal<?>> clickedOptional =
                focusSource.getIngredientForInput(input, keys).findFirst();
        if (clickedOptional.isEmpty()) {
            return Optional.empty();
        }
        IClickableIngredientInternal<?> clicked = clickedOptional.get();
        GenericStack stack = WcwtRecipeTransferHandler.toGenericStackForBookmark(clicked.getTypedIngredient());
        if (stack == null || stack.what() == null) {
            return Optional.empty();
        }
        // Preparing the command has no side effects. JEI completes mouse actions
        // on release and checks that the same ingredient is still under the mouse.
        return Optional.of(InputAction.run(() -> {
            if (DEBUG) {
                WcwtMod.LOGGER.info("WCWT JEI crafting shortcut: {}", stack);
            }
            ModNetworking.sendToServer(new WcwtJeiBookmarkOrderPacket(
                    stack, WcwtJeiBookmarkOrderPacket.Action.OPEN_CRAFT));
        }).within(clicked::isMouseOver).when(() -> clicked.getElement().isVisible()
                && minecraft.player != null && WcwtWirelessFeatures.hasAnyTerminal(minecraft.player)));
    }
}
