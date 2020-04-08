package dev.loupgarou.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import dev.loupgarou.MainLg;
import dev.loupgarou.classes.LGChat.LGChatCallback;
import dev.loupgarou.packetwrapper.WrapperPlayServerChat;
import dev.loupgarou.packetwrapper.WrapperPlayServerPlayerInfo;
import dev.loupgarou.packetwrapper.WrapperPlayServerRespawn;
import dev.loupgarou.packetwrapper.WrapperPlayServerScoreboardTeam;
import dev.loupgarou.packetwrapper.WrapperPlayServerTitle;
import dev.loupgarou.roles.utils.Role;
import dev.loupgarou.roles.utils.RoleType;
import dev.loupgarou.roles.utils.RoleWinType;
import dev.loupgarou.scoreboard.CustomScoreboard;
import dev.loupgarou.utils.SoundUtils;
import dev.loupgarou.utils.SoundUtils.LGSound;
import dev.loupgarou.utils.VariableCache;
import dev.loupgarou.utils.VariableCache.CacheType;
import dev.loupgarou.utils.VariousUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class LGPlayer {
	private static HashMap<Player, LGPlayer> cachedPlayers = new HashMap<Player, LGPlayer>();
	public static Collection<LGPlayer> all(){
		return cachedPlayers.values();
	}
	
	public static LGPlayer thePlayer(Player player) {
		LGPlayer lgp = cachedPlayers.get(player);
		if(lgp == null) {
			lgp = new LGPlayer(player);
			cachedPlayers.put(player, lgp);
		}
		return lgp;
	}
	
	public static LGPlayer removePlayer(Player player) {
		return cachedPlayers.remove(player);
	}
	
	public static void reset() {
		cachedPlayers.clear();
	}
	
	@Getter @Setter private int place;
	@Getter private Player player;
	@Getter @Setter private boolean dead;
	@Getter @Setter private Role role;
	private LGChooseCallback chooseCallback;
	private List<LGPlayer> blacklistedChoice = new ArrayList<>(0);
	@Getter private VariableCache cache = new VariableCache();
	@Getter @Setter private LGGame game;
	@Getter @Setter private String latestObjective;
	@Getter @Setter private String connectingHostname;
	@Getter private CustomScoreboard scoreboard;
	public LGPlayer(@NonNull Player player) {
		this.player = player;
		this.name = player.getDisplayName();
	}
	@Deprecated
	public LGPlayer(String name) {
		this.name = name;
	}
	
	public void setScoreboard(CustomScoreboard scoreboard) {
		if(player != null) {
			if(this.scoreboard != null)
				this.scoreboard.hide();
			
			this.scoreboard = scoreboard;
			
			if(scoreboard != null)
				scoreboard.show();
		}
	}
	
	public void sendActionBarMessage(String msg) {
		if(this.player != null) {
			WrapperPlayServerChat chat = new WrapperPlayServerChat();
			chat.setChatType(ChatType.GAME_INFO);
			chat.setMessage(WrappedChatComponent.fromText(msg));
			chat.sendPacket(getPlayer());
		}
	}
	public void sendMessage(String msg) {
		if(this.player != null)
			getPlayer().sendMessage(msg);
	}
	public void sendTitle(String title, String subTitle, int stay) {
		if(this.player != null) {
			WrapperPlayServerTitle titlePacket = new WrapperPlayServerTitle();
			titlePacket.setAction(TitleAction.TIMES);
			titlePacket.setFadeIn(10);
			titlePacket.setStay(stay);
			titlePacket.setFadeOut(10);
			titlePacket.sendPacket(player);
			
			titlePacket = new WrapperPlayServerTitle();
			titlePacket.setAction(TitleAction.TITLE);
			titlePacket.setTitle(WrappedChatComponent.fromText(title));
			titlePacket.sendPacket(player);
			
			titlePacket = new WrapperPlayServerTitle();
			titlePacket.setAction(TitleAction.SUBTITLE);
			titlePacket.setTitle(WrappedChatComponent.fromText(subTitle));
			titlePacket.sendPacket(player);
		}
	}
	public void remove() {
		this.player = null;
	}
	@NonNull private String name;
	public String getName() {
		return player != null ? name = getPlayer().getDisplayName() : name;
	}

	public void choose(LGChooseCallback callback, LGPlayer... blacklisted) {
		this.blacklistedChoice = blacklisted == null ? new ArrayList<LGPlayer>(0) : Arrays.asList(blacklisted);
		this.chooseCallback = callback;
	}
	public void stopChoosing() {
		this.blacklistedChoice = null;
		this.chooseCallback = null;
	}
	
	public static interface LGChooseCallback{
		public void callback(LGPlayer choosen);
	}

	public void showView() {
		if(getGame() != null && player != null)
			for(LGPlayer lgp : getGame().getAlive())
				if(!lgp.isDead()) {
					if(lgp != this && lgp.getPlayer() != null)
						showPlayer(lgp);
					else{
						WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
						team.setMode(2);
						team.setName(lgp.getName());
						team.setPrefix(WrappedChatComponent.fromText(""));
						team.setPlayers(Arrays.asList(lgp.getName()));
						team.sendPacket(getPlayer());
						
						WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
						ArrayList<PlayerInfoData> infos = new ArrayList<PlayerInfoData>();
						info.setAction(PlayerInfoAction.ADD_PLAYER);
						infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName()), 0, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(getName())));
						info.setData(infos);
						info.sendPacket(getPlayer());
					}
				}

		getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
		getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 2, false, false));
	}
	
	public void hideView() {
		if(getGame() != null && player != null) {
			WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
			ArrayList<PlayerInfoData> infos = new ArrayList<PlayerInfoData>();
			info.setAction(PlayerInfoAction.ADD_PLAYER);
			for(LGPlayer lgp : getGame().getAlive())
				if(lgp != this && lgp.getPlayer() != null) {
					if(!lgp.isDead())
						infos.add(new PlayerInfoData(new WrappedGameProfile(lgp.getPlayer().getUniqueId(), lgp.getName()), 0, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(lgp.getName())));
					hidePlayer(lgp);
				}
			info.setData(infos);
			info.sendPacket(getPlayer());
		}

		getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
		getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999, 1, false, false));
	}
	
	public void updatePrefix() {
		if(getGame() != null && !isDead() && player != null) {
			List<String> meList = Arrays.asList(getName());
			for(LGPlayer lgp : getGame().getInGame()) {
				WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
				ArrayList<PlayerInfoData> infos = new ArrayList<PlayerInfoData>();
				info.setAction(PlayerInfoAction.ADD_PLAYER);
				infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName()), 0, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(getName())));
				info.setData(infos);
				info.sendPacket(lgp.getPlayer());

				WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
				team.setMode(2);
				team.setName(getName());
				team.setPrefix(WrappedChatComponent.fromText(""));
				team.setPlayers(meList);
				team.sendPacket(lgp.getPlayer());
			}
		}
	}
	
	public void updateSkin() {
		if(getGame() != null && player != null) {
			for(LGPlayer lgp : getGame().getInGame()) {
				if(lgp == this) {
					WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
					ArrayList<PlayerInfoData> infos = new ArrayList<PlayerInfoData>();
					info.setAction(PlayerInfoAction.ADD_PLAYER);
					infos.add(new PlayerInfoData(new WrappedGameProfile(getPlayer().getUniqueId(), getName()), 0, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(getName())));
					info.setData(infos);
					info.sendPacket(getPlayer());
				}else if(!isDead() && lgp.getPlayer() != null){
					lgp.hidePlayer(this);
					lgp.showPlayer(this);
				}
			}
		}
	}
	public void updateOwnSkin() {
		if(player != null) {
			//On change son skin avec un packet de PlayerInfo (dans le tab)
			WrapperPlayServerPlayerInfo infos = new WrapperPlayServerPlayerInfo();
			infos.setAction(PlayerInfoAction.ADD_PLAYER);
			WrappedGameProfile gameProfile = new WrappedGameProfile(getPlayer().getUniqueId(), getPlayer().getName());
			infos.setData(Arrays.asList(new PlayerInfoData(gameProfile, 10, NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(getPlayer().getName()))));
			infos.sendPacket(getPlayer());
			//Pour qu'il voit son skin changer (sa main et en f5), on lui dit qu'il respawn (alors qu'il n'est pas mort mais ça marche quand même mdr)
			WrapperPlayServerRespawn respawn = new WrapperPlayServerRespawn();
			respawn.setDimension(0);	
			respawn.setLevelType(WorldType.NORMAL);
			respawn.setGamemode(NativeGameMode.ADVENTURE);
			respawn.setHashedSeed(0L);
			respawn.sendPacket(getPlayer());
			//Enfin, on le téléporte à sa potion actuelle car sinon il se verra dans le vide
			getPlayer().teleport(getPlayer().getLocation());
			float speed = getPlayer().getWalkSpeed();
			getPlayer().setWalkSpeed(0.2f);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					getPlayer().updateInventory();
					getPlayer().setWalkSpeed(speed);
				}
			}.runTaskLater(MainLg.getInstance(), 5);
			//Et c'est bon, le joueur se voit avec un nouveau skin avec quasiment aucun problème visible à l'écran :D
		}
	}
	public boolean canSelectDead;
	public LGPlayer getPlayerOnCursor(List<LGPlayer> list) {
		Location pointedLoc = getPlayer().getLocation();
		if(pointedLoc.getPitch() > 80 || pointedLoc.getPitch() < -60)
			if(blacklistedChoice.contains(this))
				return null;
			else
				return this;
		for(int i = 0;i<50;i++) {
			pointedLoc.add(pointedLoc.getDirection());
			for(LGPlayer targetedPlayer : list) {
				if(targetedPlayer != this && !blacklistedChoice.contains(targetedPlayer) && (!targetedPlayer.isDead() || canSelectDead) && 
					VariousUtils.distanceSquaredXZ(pointedLoc, targetedPlayer.getPlayer().getLocation()) < 0.35 && Math.abs(pointedLoc.getY()-targetedPlayer.getPlayer().getLocation().getY()) < 1.3) {
					return targetedPlayer;
				}
			}
		}
		return null;
	}
	
	public RoleType getRoleType() {
		return this.getCache().getBoolean(CacheType.INFECTED) ? RoleType.LOUP_GAROU : getRole().getType(this);
	}
	public RoleWinType getRoleWinType() {
		return this.getCache().getBoolean(CacheType.INFECTED) ? RoleWinType.LOUP_GAROU : getRole().getWinType(this);
	}
	public boolean isRoleActive() {
		return true;//TODO Old Vampire
	}
	
	@Getter
	boolean muted;
	
	public void setMuted() {
		if(player != null)
			for(LGPlayer lgp : getGame().getInGame())
				if(lgp != this && lgp.getPlayer() != null)
					lgp.hidePlayer(this);
		muted = true;
	}
	public void resetMuted() {
		muted = false;
	}
	
	@Getter private LGChat chat;
	
	public void joinChat(LGChat chat, LGChatCallback callback) {
		joinChat(chat, callback, false);
	}
	public void joinChat(LGChat chat) {
		joinChat(chat, null, false);
	}
	public void joinChat(LGChat chat, boolean muted) {
		joinChat(chat, null, muted);
	}
	public void joinChat(LGChat chat, LGChatCallback callback, boolean muted) {
		if(this.chat != null && !muted)
			this.chat.leave(this);
		
		if(!muted)
			this.chat = chat;
		
		if(chat != null && player != null)
			chat.join(this, callback == null ? chat.getDefaultCallback() : callback);
	}
	
	
	public void leaveChat() {
		joinChat(new LGChat(null, null) {
			public void sendMessage(LGPlayer sender, String message) {}
			public void join(LGPlayer player, LGChatCallback callback) {}
			public void leave(LGPlayer player) {}
		}, null);
	}
	
	public void onChat(String message) {
		if(chat != null) {
			chat.sendMessage(this, message);
		}
	}
	
	
	public void playAudio(LGSound sound, float volume) {
		SoundUtils.sendSound(getPlayer(), sound, volume);
	}
	public void stopAudio(LGSound sound) {
		if(player != null)
			getPlayer().stopSound(sound.getSound());
	}
	
	public void hidePlayer(LGPlayer lgp) {
		if(player == null || lgp.getPlayer() == null) return;
		player.hidePlayer(MainLg.getInstance(), lgp.getPlayer());
	}
	
	public void showPlayer(LGPlayer lgp) {
		if(player == null || lgp.getPlayer() == null) return;
		player.showPlayer(MainLg.getInstance(), lgp.getPlayer());
	}
	
	private long lastChooseClick;
	public void chooseAction() {
		long now = System.currentTimeMillis();
		if(lastChooseClick+200 < now) {
			if(chooseCallback != null)
				chooseCallback.callback(getPlayerOnCursor(getGame().getInGame()));
			lastChooseClick = now;
		}
	}
	
	@Override
	public String toString() {
		return super.toString()+" ("+getName()+")";
	}
}
