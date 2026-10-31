package night.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import night.Night;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {
   @Shadow
   @Nullable
   private ParseResults<ClientSuggestionProvider> currentParse;
   @Shadow
   @Nullable
   private CompletableFuture<Suggestions> pendingSuggestions;
   @Shadow
   private SuggestionsList/* @Nullable */ suggestions;
   @Shadow
   private boolean keepSuggestions;
   @Shadow
   @Final
   private EditBox input;

   @Shadow
   private void updateUsageInfo(ParseResults<ClientSuggestionProvider> var1, Suggestions var2) {
   }

   @Inject(method = "updateCommandInfo", at = @At("RETURN"))
   private void night$updateCommandInfo(CallbackInfo var1) {
      String var2 = this.input.getValue();
      String var3 = Night.commandManager.getPrefix();
      if (var2.startsWith(var3)) {
         StringReader var4 = new StringReader(var2);
         var4.setCursor(var3.length());
         CommandDispatcher var5 = Night.commandManager.getDispatcher();
         ParseResults var6 = var5.parse(var4, Night.commandManager);
         this.currentParse = var6;
         if (this.suggestions == null || !this.keepSuggestions) {
            CompletableFuture var7 = var5.getCompletionSuggestions(var6, this.input.getCursorPosition());
            this.pendingSuggestions = var7;
            var7.thenRun(() -> {
               if (var7.isDone()) {
                  this.updateUsageInfo(this.currentParse, (Suggestions)var7.join());
               }
            });
         }
      }
   }
}
