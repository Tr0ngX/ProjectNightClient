package night.module.impl.misc;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;
import night.Night;
import night.events.EventTarget;
import night.events.impl.BreakBlockEvent;
import night.events.impl.ChatSendEvent;
import night.events.impl.ConsumeItemEvent;
import night.events.impl.PacketSendEvent;
import night.events.impl.TickEvent;
import night.module.Module;
import night.module.ModuleInfo;
import night.setting.impl.BooleanSetting;
import night.setting.impl.ModeSetting;
import night.setting.impl.NumberSetting;
import night.setting.impl.StringSetting;
import night.util.math.MathUtil;
import night.util.math.TimerUtil;
import night.util.world.PacketAndRenderUtil;

@ModuleInfo(name = "Announcer", description = "Announces your actions in chat.", category = Module.Category.MISCELLANEOUS)
public class Announcer extends Module {
   public StringSetting watermark = new StringSetting("Watermark", "The client name that will be used in the announcer.", "Night");
   public ModeSetting language;
   public NumberSetting delay;
   public BooleanSetting clientside;
   public BooleanSetting greenText;
   public BooleanSetting distance;
   public BooleanSetting blocksMined;
   public BooleanSetting blocksPlaced;
   public BooleanSetting eating;
   private final ConcurrentLinkedQueue<String> n;
   private final TimerUtil o;
   private final TimerUtil p;
   private Vec3 q;
   private int r;
   private int s;
   private int t;

   public Announcer() {
      String[] var1 = new String[]{
         "English",
         "German",
         "French",
         "Japanese",
         "Finnish",
         "Russian",
         "Spanish",
         "Swedish",
         "Turkish",
         "Dutch",
         "Greek",
         "Chinese",
         "Italian",
         "Norwegian",
         "Romanian",
         "Czech",
         "Portuguese",
         "Slovenian",
         "Polish",
         "Korean",
         "Lithuanian",
         "Indonesian",
         "Hungarian",
         "Random"
      };
      this.language = new ModeSetting("Language", "The language that will be used for the announcer.", "English", var1);
      Integer var2 = 5;
      Integer var3 = 0;
      Integer var4 = 30;
      this.delay = new NumberSetting("Delay", "The delay for the announcer.", var2, var3, var4);
      this.clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
      this.greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
      this.distance = new BooleanSetting("Distance", "Announces your distance travelled in chat.", true);
      this.blocksMined = new BooleanSetting("BlocksMined", "Announces blocks mined in chat.", true);
      this.blocksPlaced = new BooleanSetting("BlocksPlaced", "Announces blocks placed in chat.", true);
      this.eating = new BooleanSetting("Eating", "Announces when you eat in chat.", true);
      this.n = new ConcurrentLinkedQueue<>();
      this.o = new TimerUtil();
      this.p = new TimerUtil();
   }

   @Override
   public void onEnable() {
      this.n.clear();
      this.o.reset();
      this.p.reset();
      this.q = null;
      this.r = 0;
      this.s = 0;
      this.t = 0;
   }

   @EventTarget
   public void a(TickEvent var1) {
      if (!this.nullCheck()) {
         TimerUtil var2 = this.o;
         Integer var3 = this.delay.getValue().intValue() * 1000;
         if (var2.passedMs(var3) && !this.n.isEmpty()) {
            String var4 = this.n.poll();
            if (this.clientside.getValue()) {
               String var5 = var4;
               Night.chatManager.sendMessage(var5);
            } else {
               ClientPacketListener var6 = mc.player.connection;
               String var7;
               if (this.greenText.getValue()) {
                  var7 = "> ";
               } else {
                  var7 = "";
               }

               String var8 = var4;
               var6.sendChat(var7 + var8);
            }

            this.o.reset();
         }

         if (this.q == null) {
            double var9 = mc.player.xo;
            double var11 = mc.player.yo;
            double var13 = mc.player.zo;
            this.q = new Vec3(var9, var11, var13);
         }

         double var15 = this.q.x;
         double var17 = mc.player.xo;
         double var19 = Math.abs(var15 - var17);
         double var21 = this.q.y;
         double var23 = mc.player.yo;
         double var25 = Math.abs(var21 - var23);
         double var27 = var19 + var25;
         double var29 = this.q.z;
         double var31 = mc.player.zo;
         double var33 = Math.abs(var29 - var31);
         double var35 = var27 + var33;
         if (this.distance.getValue()) {
            double var37 = var35;
            double var39 = 1.0;
            if (var37 > var39) {
               TimerUtil var41 = this.p;
               Integer var42 = 10000;
               if (var41.passedMs(var42) && this.n.size() <= 5) {
                  ConcurrentLinkedQueue var43 = this.n;
                  String var44 = this.b(MathUtil.a_double_double_int(var35, 1) + "");
                  var43.add(var44);
                  double var45 = mc.player.xo;
                  double var47 = mc.player.yo;
                  double var49 = mc.player.zo;
                  this.q = new Vec3(var45, var47, var49);
                  this.p.reset();
               }
            }
         }
      }
   }

   @EventTarget
   public void a(BreakBlockEvent var1) {
      if (!this.nullCheck()) {
         int var2 = this.r + 1;
         this.r = var2;
         if (this.blocksMined.getValue()) {
            double var3 = this.r;
            double var5 = 6.0;
            double var7 = 1.0;
            double var9 = MathUtil.a_double_double_double(var5, var7);
            if (var3 >= var9 && this.n.size() <= 5) {
               ConcurrentLinkedQueue var11 = this.n;
               String var12 = this.c(this.r + "");
               var11.add(var12);
               this.r = 0;
            }
         }
      }
   }

   @EventTarget
   public void a(PacketSendEvent var1) {
      if (!this.nullCheck()) {
         if (var1.getPacket() instanceof ServerboundUseItemOnPacket var3) {
            LocalPlayer var4 = mc.player;
            InteractionHand var5 = PacketAndRenderUtil.a_InteractionHand_ServerboundUseItemOnPacket(var3);
            if (var4.getItemInHand(var5).getItem() instanceof BlockItem) {
               int var6 = this.s + 1;
               this.s = var6;
               if (this.blocksPlaced.getValue()) {
                  double var7 = this.s;
                  double var9 = 6.0;
                  double var11 = 1.0;
                  double var13 = MathUtil.a_double_double_double(var9, var11);
                  if (var7 >= var13 && this.n.size() <= 5) {
                     ConcurrentLinkedQueue var15 = this.n;
                     String var16 = this.d(this.s + "");
                     var15.add(var16);
                     this.s = 0;
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void a(ConsumeItemEvent var1) {
      if (!this.nullCheck() && var1.getStack().get(DataComponents.FOOD) != null) {
         int var2 = this.t + 1;
         this.t = var2;
         if (this.eating.getValue()) {
            double var3 = this.t;
            double var5 = 6.0;
            double var7 = 1.0;
            double var9 = MathUtil.a_double_double_double(var5, var7);
            if (var3 >= var9 && this.n.size() <= 5) {
               ConcurrentLinkedQueue var11 = this.n;
               int var12 = this.t;
               String var13 = var1.getStack().getHoverName().getString();
               String var14 = this.e(var12 + " " + var13);
               var11.add(var14);
               this.t = 0;
            }
         }
      }
   }

   @EventTarget
   public void a(ChatSendEvent var1) {
      if (!this.nullCheck()) {
         this.o.reset();
      }
   }

   private String b(String var1) {
      String[] var2 = new String[23];
      String var3 = this.watermark.getValue();
      var2[0] = "I just flew " + var1 + " meters thanks to " + var3 + "!";
      String var4 = this.watermark.getValue();
      var2[1] = "Ich bin gerade " + var1 + " Meter weit geflogen, dank " + var4 + "!";
      String var5 = this.watermark.getValue();
      var2[2] = "Je viens de voler " + var1 + " m\u00e8tres gr\u00e2ce \u00e0 " + var5 + "!";
      var2[3] = this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + var1 + "\u30e1\u30fc\u30c8\u30eb\u98db\u3093\u3060\u3088\uff01";
      String var6 = this.watermark.getValue();
      var2[4] = "Lensin juuri " + var1 + " metri\u00e4 " + var6 + " ansiosta!";
      String var7 = this.watermark.getValue();
      var2[5] = "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u043f\u0440\u043e\u043b\u0435\u0442\u0435\u043b "
         + var1
         + " \u043c\u0435\u0442\u0440\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f "
         + var7
         + "!";
      String var8 = this.watermark.getValue();
      var2[6] = "Acabo de volar " + var1 + " metros gracias a " + var8 + "!";
      String var9 = this.watermark.getValue();
      var2[7] = "Jag fl\u00f6g just " + var1 + " meter tack vare " + var9 + "!";
      var2[8] = this.watermark.getValue() + " sayesinde " + var1 + " metre u\u00e7tum!";
      String var10 = this.watermark.getValue();
      var2[9] = "Ik heb net " + var1 + " meter gevlogen dankzij " + var10 + "!";
      String var11 = this.watermark.getValue();
      var2[10] = "\u039c\u03cc\u03bb\u03b9\u03c2 \u03c0\u03ad\u03c4\u03b1\u03be\u03b1 "
         + var1
         + " \u03bc\u03ad\u03c4\u03c1\u03b1 \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03b7\u03bd "
         + var11
         + "!";
      String var12 = this.watermark.getValue();
      var2[11] = "\u6211\u521a\u521a\u98de\u4e86" + var1 + "\u7c73\uff0c\u591a\u4e8f\u4e86" + var12 + "!";
      String var13 = this.watermark.getValue();
      var2[12] = "Ho appena volato per " + var1 + " metri grazie ad " + var13 + "!";
      String var14 = this.watermark.getValue();
      var2[13] = "Jeg fl\u00f8y nettopp " + var1 + " meter takket v\u00e6re " + var14 + "!";
      String var15 = this.watermark.getValue();
      var2[14] = "Tocmai am zburat " + var1 + " de metri datorit\u0103 lui " + var15 + "!";
      var2[15] = "D\u00edky " + this.watermark.getValue() + " jsem pr\u00e1v\u011b ulet\u011bl " + var1 + " metr\u016f!";
      String var16 = this.watermark.getValue();
      var2[16] = "Acabei de voar " + var1 + " metros gra\u00e7as ao " + var16 + "!";
      var2[17] = "Z " + this.watermark.getValue() + " sem pravkar preletel " + var1 + " metrov!";
      String var17 = this.watermark.getValue();
      var2[18] = "W\u0142a\u015bnie przelecia\u0142em " + var1 + " metr\u00f3w dzi\u0119ki " + var17 + "!";
      var2[19] = this.watermark.getValue() + " \ub355\ubd84\uc5d0 \ubc29\uae08 " + var1 + "\ub97c \ube44\ud589\ud588\uc2b5\ub2c8\ub2e4!";
      var2[20] = this.watermark.getValue() + " d\u0117ka k\u0105 tik nuskridau " + var1 + " metrus!";
      String var18 = this.watermark.getValue();
      var2[21] = "Saya baru saja terbang sejauh " + var1 + " meter berkat " + var18 + "!";
      String var19 = this.watermark.getValue();
      var2[22] = var1 + " m\u00e9tert rep\u00fcltem az " + var19 + " k\u00f6sz\u00f6nhet\u0151en!";
      String[] var20 = var2;
      int var21 = this.v();
      return var20[var21];
   }

   private String c(String var1) {
      String[] var2 = new String[23];
      String var3 = this.watermark.getValue();
      var2[0] = "I just mined " + var1 + " blocks thanks to " + var3 + "!";
      String var4 = this.watermark.getValue();
      var2[1] = "Ich habe gerade " + var1 + " Bl\u00f6cke abgebaut, dank " + var4 + "!";
      String var5 = this.watermark.getValue();
      var2[2] = "Je viens d'extraire " + var1 + " blocs gr\u00e2ce \u00e0 " + var5 + "!";
      var2[3] = this.watermark.getValue()
         + "\u306e\u304a\u304b\u3052\u3067"
         + var1
         + "\u30d6\u30ed\u30c3\u30af\u63a1\u6398\u3057\u305f\u3068\u3053\u308d\u3067\u3059\uff01";
      String var6 = this.watermark.getValue();
      var2[4] = "Louhin juuri " + var1 + " lohkoa " + var6 + " ansiosta!";
      String var7 = this.watermark.getValue();
      var2[5] = "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0434\u043e\u0431\u044b\u043b "
         + var1
         + " \u0431\u043b\u043e\u043a\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f "
         + var7
         + "!";
      String var8 = this.watermark.getValue();
      var2[6] = "\u00a1Acabo de minar " + var1 + " bloques gracias a " + var8 + "!";
      String var9 = this.watermark.getValue();
      var2[7] = "Jag har precis tagit fram " + var1 + " block tack vare " + var9 + "!";
      var2[8] = this.watermark.getValue() + " sayesinde az \u00f6nce " + var1 + " blok kazd\u0131m!";
      String var10 = this.watermark.getValue();
      var2[9] = "Ik heb net " + var1 + " blokken gedolven dankzij " + var10 + "!";
      String var11 = this.watermark.getValue();
      var2[10] = "\u039c\u03cc\u03bb\u03b9\u03c2 \u03b5\u03be\u03cc\u03c1\u03c5\u03be\u03b1 "
         + var1
         + " \u03bc\u03c0\u03bb\u03bf\u03ba \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03b7\u03bd "
         + var11
         + "!";
      String var12 = this.watermark.getValue();
      var2[11] = "\u6211\u521a\u521a\u5f00\u91c7\u4e86" + var1 + "\u4e2a\u533a\u5757\uff0c\u611f\u8c22" + var12 + "!";
      String var13 = this.watermark.getValue();
      var2[12] = "Ho appena estratto " + var1 + " blocchi grazie ad " + var13 + "!";
      String var14 = this.watermark.getValue();
      var2[13] = "Jeg har nettopp utvunnet " + var1 + " blokker takket v\u00e6re " + var14 + "!";
      String var15 = this.watermark.getValue();
      var2[14] = "Tocmai am minat " + var1 + " de blocuri datorit\u0103 lui " + var15 + "!";
      String var16 = this.watermark.getValue();
      var2[15] = "Pr\u00e1v\u011b jsem vyt\u011b\u017eil " + var1 + " blok\u016f d\u00edky " + var16 + "!";
      String var17 = this.watermark.getValue();
      var2[16] = "Acabei de extrair " + var1 + " blocos gra\u00e7as ao " + var17 + "!";
      var2[17] = "Zahvaljujo\u010d " + this.watermark.getValue() + " sem pravkar izkopal " + var1 + " blokov!";
      String var18 = this.watermark.getValue();
      var2[18] = "W\u0142a\u015bnie wydoby\u0142em " + var1 + " blok\u00f3w dzi\u0119ki " + var18 + "!";
      var2[19] = "\ubc29\uae08 " + this.watermark.getValue() + " \ub355\ubd84\uc5d0 " + var1 + " \ube14\ub85d\uc744 \ucc44\uad74\ud588\uc2b5\ub2c8\ub2e4!";
      var2[20] = this.watermark.getValue() + " d\u0117ka k\u0105 tik i\u0161kasiau " + var1 + " blokus!";
      String var19 = this.watermark.getValue();
      var2[21] = "Saya baru saja menambang " + var1 + " blok berkat " + var19 + "!";
      String var20 = this.watermark.getValue();
      var2[22] = "Most b\u00e1ny\u00e1sztam " + var1 + " blokkot az " + var20 + " k\u00f6sz\u00f6nhet\u0151en!";
      String[] var21 = var2;
      int var22 = this.v();
      return var21[var22];
   }

   private String d(String var1) {
      String[] var2 = new String[23];
      String var3 = this.watermark.getValue();
      var2[0] = "I just placed " + var1 + " blocks thanks to " + var3 + "!";
      String var4 = this.watermark.getValue();
      var2[1] = "Ich habe gerade " + var1 + " Bl\u00f6cke dank " + var4 + " platziert!";
      String var5 = this.watermark.getValue();
      var2[2] = "Je viens de placer " + var1 + " blocs gr\u00e2ce \u00e0 " + var5 + "!";
      var2[3] = this.watermark.getValue()
         + "\u306e\u304a\u304b\u3052\u3067"
         + var1
         + "\u500b\u306e\u30d6\u30ed\u30c3\u30af\u3092\u7f6e\u3044\u305f\u3068\u3053\u308d\u3060!";
      String var6 = this.watermark.getValue();
      var2[4] = "Sijoitin juuri " + var1 + " lohkoa " + var6 + " ansiosta!";
      String var7 = this.watermark.getValue();
      var2[5] = "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0440\u0430\u0437\u043c\u0435\u0441\u0442\u0438\u043b "
         + var1
         + " \u0431\u043b\u043e\u043a\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f "
         + var7
         + "!";
      String var8 = this.watermark.getValue();
      var2[6] = "\u00a1Acabo de colocar " + var1 + " bloques gracias a " + var8 + "!";
      String var9 = this.watermark.getValue();
      var2[7] = "Jag har precis placerat " + var1 + " block tack vare " + var9 + "!";
      var2[8] = "Az \u00f6nce " + this.watermark.getValue() + " sayesinde " + var1 + " blok yerle\u015ftirdim!";
      String var10 = this.watermark.getValue();
      var2[9] = "Ik heb net " + var1 + " blokken geplaatst dankzij " + var10 + "!";
      String var11 = this.watermark.getValue();
      var2[10] = "\u039c\u03cc\u03bb\u03b9\u03c2 \u03c4\u03bf\u03c0\u03bf\u03b8\u03ad\u03c4\u03b7\u03c3\u03b1 "
         + var1
         + " \u03bc\u03c0\u03bb\u03bf\u03ba \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03bf "
         + var11
         + "!";
      var2[11] = "\u591a\u4e8f\u4e86 " + this.watermark.getValue() + "\uff0c\u6211\u521a\u521a\u653e\u4e86 " + var1 + " \u5757!";
      String var12 = this.watermark.getValue();
      var2[12] = "Ho appena piazzato " + var1 + " blocchi grazie a " + var12 + "!";
      String var13 = this.watermark.getValue();
      var2[13] = "Jeg har nettopp plassert " + var1 + " blokker takket v\u00e6re " + var13 + "!";
      String var14 = this.watermark.getValue();
      var2[14] = "Tocmai am plasat " + var1 + " blocuri datorit\u0103 lui " + var14 + "!";
      String var15 = this.watermark.getValue();
      var2[15] = "Pr\u00e1v\u011b jsem um\u00edstil " + var1 + " blok\u016f d\u00edky " + var15 + "!";
      String var16 = this.watermark.getValue();
      var2[16] = "Acabei de colocar " + var1 + " blocos gra\u00e7as ao " + var16 + "!";
      String var17 = this.watermark.getValue();
      var2[17] = "Pravkar sem postavil " + var1 + " blokov zahvaljujo\u010d " + var17 + "!";
      String var18 = this.watermark.getValue();
      var2[18] = "W\u0142a\u015bnie umie\u015bci\u0142em " + var1 + " blok\u00f3w dzi\u0119ki " + var18 + "!";
      var2[19] = this.watermark.getValue() + " \ub355\ubd84\uc5d0 \ubc29\uae08 XXX \ube14\ub85d\uc744 \ubc30\uce58\ud588\uc2b5\ub2c8\ub2e4!";
      String var19 = this.watermark.getValue();
      var2[20] = "K\u0105 tik \u012fd\u0117jau " + var1 + " blok\u0173 d\u0117ka " + var19 + "!";
      String var20 = this.watermark.getValue();
      var2[21] = "Saya baru saja menempatkan blok " + var1 + " berkat " + var20 + "!";
      String var21 = this.watermark.getValue();
      var2[22] = "Most helyeztem el " + var1 + " blokkot a " + var21 + "-nek k\u00f6sz\u00f6nhet\u0151en!";
      String[] var22 = var2;
      int var23 = this.v();
      return var22[var23];
   }

   private String e(String var1) {
      String[] var2 = new String[22];
      String var3 = this.watermark.getValue();
      var2[0] = "I just ate " + var1 + " thanks to " + var3 + "!";
      String var4 = this.watermark.getValue();
      var2[1] = "Ich habe gerade " + var1 + " gegessen, dank " + var4 + "!";
      String var5 = this.watermark.getValue();
      var2[2] = "Je viens de manger " + var1 + " gr\u00e2ce \u00e0 " + var5 + " !";
      var2[3] = this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + var1 + "\u3092\u98df\u3079\u305f\u3088!";
      String var6 = this.watermark.getValue();
      var2[4] = "S\u00f6in juuri " + var1 + " kiitos " + var6 + "!";
      String var7 = this.watermark.getValue();
      var2[5] = "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0441\u044a\u0435\u043b "
         + var1
         + " \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f "
         + var7
         + "!";
      String var8 = this.watermark.getValue();
      var2[6] = "\u00a1Acabo de comer " + var1 + " gracias a " + var8 + "!";
      String var9 = this.watermark.getValue();
      var2[7] = "Jag \u00e5t just " + var1 + " tack vare " + var9 + "!";
      var2[8] = "Az \u00f6nce " + this.watermark.getValue() + " sayesinde " + var1 + " yedim!";
      String var10 = this.watermark.getValue();
      var2[9] = "Ik heb net " + var1 + " gegeten dankzij " + var10 + "!";
      String var11 = this.watermark.getValue();
      var2[10] = "\u039c\u03cc\u03bb\u03b9\u03c2 \u03ad\u03c6\u03b1\u03b3\u03b1 " + var1 + " \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03bf " + var11 + "!";
      String var12 = this.watermark.getValue();
      var2[11] = "\u6211\u521a\u5403\u4e86 " + var1 + "\uff0c\u591a\u4e8f\u4e86 " + var12 + "!";
      String var13 = this.watermark.getValue();
      var2[12] = "Ho appena mangiato " + var1 + " grazie a " + var13 + "!";
      String var14 = this.watermark.getValue();
      var2[13] = "Jeg spiste nettopp " + var1 + " takket v\u00e6re " + var14 + "!";
      String var15 = this.watermark.getValue();
      var2[14] = "Pr\u00e1v\u011b jsem sn\u011bdl " + var1 + " d\u00edky " + var15 + "!";
      String var16 = this.watermark.getValue();
      var2[15] = "Acabei de comer " + var1 + " gra\u00e7as a " + var16 + "!";
      String var17 = this.watermark.getValue();
      var2[16] = "Pravkar sem pojedel " + var1 + " zaradi " + var17 + "!";
      String var18 = this.watermark.getValue();
      var2[17] = "W\u0142a\u015bnie zjad\u0142em " + var1 + " dzi\u0119ki " + var18 + "!";
      var2[18] = "\ubc29\uae08 " + this.watermark.getValue() + " \ub355\ubd84\uc5d0 " + var1 + "\ub97c \uba39\uc5c8\uc5b4\uc694!";
      String var19 = this.watermark.getValue();
      var2[19] = "K\u0105 tik suvalgiau " + var1 + " d\u0117l " + var19 + "!";
      String var20 = this.watermark.getValue();
      var2[20] = "Saya baru saja makan " + var1 + " berkat " + var20 + "!";
      String var21 = this.watermark.getValue();
      var2[21] = "Most ettem " + var1 + "-t, h\u00e1la " + var21 + "-nek!";
      String[] var22 = var2;
      int var23 = this.v();
      return var22[var23];
   }

   private int v() {
      return switch (this.language.getValue()) {
         case "German" -> 1;
         case "French" -> 2;
         case "Japanese" -> 3;
         case "Finnish" -> 4;
         case "Russian" -> 5;
         case "Spanish" -> 6;
         case "Swedish" -> 7;
         case "Turkish" -> 8;
         case "Dutch" -> 9;
         case "Greek" -> 10;
         case "Chinese" -> 11;
         case "Italian" -> 12;
         case "Norwegian" -> 13;
         case "Romanian" -> 14;
         case "Czech" -> 15;
         case "Portuguese" -> 16;
         case "Slovenian" -> 17;
         case "Polish" -> 18;
         case "Korean" -> 19;
         case "Lithuanian" -> 20;
         case "Indonesian" -> 21;
         case "Hungarian" -> 22;
         case "Random" -> {
            double var4 = 22.0;
            double var6 = 0.0;
            yield (int)MathUtil.a_double_double_double(var4, var6);
         }
         default -> 0;
      };
   }
}
