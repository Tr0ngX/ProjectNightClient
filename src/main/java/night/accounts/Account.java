package night.accounts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Account {
   @Expose
   @SerializedName("name")
   private String name;
   @Expose
   @SerializedName("uuid")
   private String uuid;
   @Expose
   @SerializedName("type")
   private AccountType type;
   @Expose
   @SerializedName("refreshToken")
   private String refreshToken;
   @Expose
   @SerializedName("accessToken")
   private String accessToken;
   @Expose
   @SerializedName("lastUsed")
   private long lastUsed;

   public Account() {
   }

   public Account(String var1, String var2, AccountType var3) {
      this.name = var1;
      this.uuid = var2;
      this.type = var3;
      long var4 = System.currentTimeMillis();
      this.lastUsed = var4;
   }

   public Account(String var1, String var2, AccountType var3, String var4, String var5) {
      this.name = var1;
      this.uuid = var2;
      this.type = var3;
      this.refreshToken = var4;
      this.accessToken = var5;
      long var6 = System.currentTimeMillis();
      this.lastUsed = var6;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String var1) {
      this.name = var1;
   }

   public String getUuid() {
      return this.uuid;
   }

   public void setUuid(String var1) {
      this.uuid = var1;
   }

   public AccountType getType() {
      return this.type;
   }

   public void setType(AccountType var1) {
      this.type = var1;
   }

   public String getRefreshToken() {
      return this.refreshToken;
   }

   public void setRefreshToken(String var1) {
      this.refreshToken = var1;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public void setAccessToken(String var1) {
      this.accessToken = var1;
   }

   public long getLastUsed() {
      return this.lastUsed;
   }

   public void setLastUsed(long var1) {
      long var3 = var1;
      this.lastUsed = var3;
   }

   public UUID getProfileId() {
      try {
         if (this.uuid != null && !this.uuid.isEmpty()) {
            if (this.uuid.contains("-")) {
               return UUID.fromString(this.uuid);
            }

            return UUID.fromString(
               this.uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})", "$1-$2-$3-$4-$5")
            );
         }
      } catch (Exception var4) {
      }

      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.name).getBytes(StandardCharsets.UTF_8));
   }
}
