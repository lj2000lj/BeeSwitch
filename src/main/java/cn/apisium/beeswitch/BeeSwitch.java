package cn.apisium.beeswitch;

import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = BeeSwitch.MODID, version = BeeSwitch.VERSION)
public class BeeSwitch {
	public static final String MODID = "beeswitch";
	public static final String VERSION = "1.2";
	public static FMLEventChannel channel;
	public Minecraft mc = Minecraft.getMinecraft();
	private Logger logger = null;

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent evt) {
		channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("BeeSwitch");
		channel.register(this);
		logger = evt.getModLog();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent evt) {

		logger.info("BeeSwitch加载完毕", new Object[0]);
	}

	String ip = null;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPlayerJoin(FMLNetworkEvent.ClientCustomPacketEvent event) {
		ByteBuf buffer = event.getPacket().payload();
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);
		ip = new String(bytes, StandardCharsets.UTF_8);
		logger.info("收到服务器发来的切换请求: " + ip);
		if (ip != null) {
			logger.info("开始切换", new Object[0]);
			logger.info("Ip:" + ip, new Object[0]);
			this.mc.world.sendQuittingDisconnectingPacket();
			this.mc.displayGuiScreen(new GuiMainMenu());
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		if (ip == null) {
			return;
		}
		this.mc.addScheduledTask(new Runnable() {
			@Override
			public void run() {
				final ServerData serverData = new ServerData(ip, ip, false);
				ip = null;
				logger.info("正在连接至" + serverData.serverIP);
				FMLClientHandler.instance().setupServerList();
				FMLClientHandler.instance().connectToServer(new GuiMainMenu(), serverData);
			}
		});
	}
}