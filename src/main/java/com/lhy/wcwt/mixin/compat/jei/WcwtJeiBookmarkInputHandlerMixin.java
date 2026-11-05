package com.lhy.wcwt.mixin.compat.jei;

import com.lhy.wcwt.compat.jei.WcwtJeiBookmarkOrder;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IngredientFocusSource;
import mezz.jei.gui.input.InputCommands;
import mezz.jei.gui.input.handlers.IngredientCommands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Keep the existing mixin name/config entry; JEI 15.74 uses commands instead
// of the removed BookmarkInputHandler and IUserInputHandler interfaces.
@Mixin(value = IngredientCommands.class, remap = false)
public abstract class WcwtJeiBookmarkInputHandlerMixin {
    @Shadow @Final private IngredientFocusSource focusSource;
    @Shadow @Final private IInternalKeyMappings keys;

    @Inject(method = "registerInputCommands(Lmezz/jei/gui/input/InputCommands;)V", at = @At("HEAD"), require = 1)
    private void wcwt$registerCraftCommand(InputCommands commands, CallbackInfo ci) {
        WcwtJeiBookmarkOrder.registerInputCommands(commands, focusSource, keys);
    }
}
