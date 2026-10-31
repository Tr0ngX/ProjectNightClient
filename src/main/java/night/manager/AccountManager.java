package night.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Generated;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import night.Night;
import night.accounts.Account;
import night.accounts.AccountType;
import night.accounts.MicrosoftAuth;
import night.mixins.accessors.MinecraftClientAccessor;
import night.util.MinecraftInstance;

public class AccountManager implements MinecraftInstance {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final List<Account> accounts = new CopyOnWriteArrayList<>();
   private Account currentAccount;

   public AccountManager() {
      this.loadAccounts();
   }

   public void loadAccounts() {
      try {
         Path var1 = this.e();
         LinkOption[] var2 = new LinkOption[0];
         if (!Files.exists(var1, var2)) {
            return;
         }

         try (BufferedReader var3 = Files.newBufferedReader(var1, StandardCharsets.UTF_8)) {
            Type var4 = (new TypeToken<List<Account>>() {}).getType();
            BufferedReader var5 = var3;
            Type var6 = var4;
            List var7 = (List)GSON.fromJson(var5, var6);
            if (var7 != null) {
               this.accounts.clear();
               List var8 = this.accounts;
               List var9 = var7;
               var8.addAll(var9);
            }
         }
      } catch (Exception var17) {
         Night.LOGGER.error("Failed to load accounts.json", var17);
      }
   }

   public void saveAccounts() {
      try {
         Path var1 = this.e();
         if (var1.getParent() != null) {
            Path var2 = var1.getParent();
            LinkOption[] var3 = new LinkOption[0];
            if (!Files.exists(var2, var3)) {
               Path var4 = var1.getParent();
               FileAttribute[] var5 = new FileAttribute[0];
               Files.createDirectories(var4, var5);
            }
         }

         Path var6 = var1;
         OpenOption[] var7 = new OpenOption[0];

         try (BufferedWriter var8 = Files.newBufferedWriter(var6, StandardCharsets.UTF_8, var7)) {
            List var9 = this.accounts;
            BufferedWriter var10 = var8;
            GSON.toJson(var9, var10);
         }
      } catch (Exception var18) {
         Night.LOGGER.error("Failed to save accounts.json", var18);
      }
   }

   private Path e() {
      String[] var1 = new String[]{"accounts.json"};
      return Paths.get("Night", var1);
   }

   private void a_void_User_String(User var1, String var2) {
      MinecraftClientAccessor var3 = (MinecraftClientAccessor)mc;
      var3.setUser(var1);

      try {
         if (var2 != null && !var2.isEmpty()) {
            UserApiService var4 = new YggdrasilAuthenticationService(Proxy.NO_PROXY).createUserApiService(var2);
            MinecraftClientAccessor var5 = var3;
            UserApiService var6 = var4;
            var5.setUserApiService(var6);
            UserApiService var7 = var4;
            Path var8 = mc.gameDirectory.toPath();
            ProfileKeyPairManager var9 = ProfileKeyPairManager.create(var7, var1, var8);
            MinecraftClientAccessor var10 = var3;
            ProfileKeyPairManager var11 = var9;
            var10.setProfileKeyPairManager(var11);
            var9.prepareKeyPair();
         } else {
            var3.setUserApiService(UserApiService.OFFLINE);
            var3.setProfileKeyPairManager(ProfileKeyPairManager.EMPTY_KEY_MANAGER);
         }
      } catch (Exception var13) {
         Night.LOGGER.warn("Failed to initialize ProfileKeyPairManager for account: " + var1.getName(), var13);
      }
   }

   public boolean login(Account var1) {
      if (var1 == null) {
         return false;
      }

      try {
         if (var1.getType() == AccountType.OFFLINE) {
            String var2 = var1.getName();
            UUID var3 = var1.getProfileId();
            User var4 = new User(var2, var3, "", Optional.empty(), Optional.empty());
            this.a_void_User_String(var4, "");
            this.currentAccount = var1;
            long var5 = System.currentTimeMillis();
            var1.setLastUsed(var5);
            this.saveAccounts();
            return true;
         }

         if (var1.getType() == AccountType.MICROSOFT) {
            if (var1.getRefreshToken() != null && !var1.getRefreshToken().isEmpty()) {
               MicrosoftAuth.AuthResult var7 = MicrosoftAuth.refreshAccount(var1.getRefreshToken());
               if (var7.success()) {
                  String var8 = var7.username();
                  var1.setName(var8);
                  String var9 = var7.uuid();
                  var1.setUuid(var9);
                  String var10 = var7.accessToken();
                  var1.setAccessToken(var10);
                  if (var7.refreshToken() != null && !var7.refreshToken().isEmpty()) {
                     String var11 = var7.refreshToken();
                     var1.setRefreshToken(var11);
                  }

                  String var12 = var1.getName();
                  UUID var13 = var1.getProfileId();
                  String var14 = var1.getAccessToken();
                  User var15 = new User(var12, var13, var14, Optional.empty(), Optional.empty());
                  String var16 = var1.getAccessToken();
                  this.a_void_User_String(var15, var16);
                  this.currentAccount = var1;
                  long var17 = System.currentTimeMillis();
                  var1.setLastUsed(var17);
                  this.saveAccounts();
                  return true;
               }
            }

            if (var1.getAccessToken() != null && !var1.getAccessToken().isEmpty()) {
               String var26 = var1.getName();
               UUID var27 = var1.getProfileId();
               String var28 = var1.getAccessToken();
               User var29 = new User(var26, var27, var28, Optional.empty(), Optional.empty());
               String var30 = var1.getAccessToken();
               this.a_void_User_String(var29, var30);
               this.currentAccount = var1;
               long var31 = System.currentTimeMillis();
               var1.setLastUsed(var31);
               this.saveAccounts();
               return true;
            } else {
               return false;
            }
         } else if (var1.getType() == AccountType.SESSION) {
            String var19 = var1.getName();
            UUID var20 = var1.getProfileId();
            String var21;
            if (var1.getAccessToken() != null) {
               var21 = var1.getAccessToken();
            } else {
               var21 = "";
            }

            User var22 = new User(var19, var20, var21, Optional.empty(), Optional.empty());
            String var23 = var1.getAccessToken();
            this.a_void_User_String(var22, var23);
            this.currentAccount = var1;
            long var24 = System.currentTimeMillis();
            var1.setLastUsed(var24);
            this.saveAccounts();
            return true;
         } else {
            return false;
         }
      } catch (Exception var34) {
         Night.LOGGER.error("Failed to login to account: " + var1.getName(), var34);
         return false;
      }
   }

   public Account loginOffline(String var1) {
      if (var1 != null && !var1.trim().isEmpty()) {
         String var2 = var1.trim();
         Account var3 = null;

         for (Account var5 : (Iterable<Account>)(Object)this.accounts) {
            if (var5.getType() == AccountType.OFFLINE) {
               String var6 = var5.getName();
               String var7 = var2;
               if (var6.equalsIgnoreCase(var7)) {
                  var3 = var5;
                  break;
               }
            }
         }

         if (var3 == null) {
            UUID var8 = UUID.nameUUIDFromBytes(("OfflinePlayer:" + var2).getBytes(StandardCharsets.UTF_8));
            String var9 = var2;
            String var10 = var8.toString();
            var3 = new Account(var9, var10, AccountType.OFFLINE);
            List var11 = this.accounts;
            Account var12 = var3;
            var11.add(var12);
         }

         Account var13 = var3;
         this.login(var13);
         return var3;
      } else {
         return null;
      }
   }

   public void addAccount(Account var1) {
      if (var1 != null) {
         this.accounts.removeIf(var1x -> {
            String var2 = var1x.getName();
            String var3 = var1.getName();
            if (var2.equalsIgnoreCase(var3)) {
               AccountType var4 = var1x.getType();
               AccountType var5 = var1.getType();
               if (var4 == var5) {
                  return true;
               }
            }

            return false;
         });
         this.accounts.add(var1);
         this.saveAccounts();
      }
   }

   public void removeAccount(Account var1) {
      if (var1 != null) {
         this.accounts.remove(var1);
         if (this.currentAccount == var1) {
            this.currentAccount = null;
         }

         this.saveAccounts();
      }
   }

   public String getCurrentUsername() {
      if (mc.getUser() != null) {
         return mc.getUser().getName();
      }

      String var1;
      if (this.currentAccount != null) {
         var1 = this.currentAccount.getName();
      } else {
         var1 = "Unknown";
      }

      return var1;
   }

   @Generated
   public List<Account> getAccounts() {
      return this.accounts;
   }
}
