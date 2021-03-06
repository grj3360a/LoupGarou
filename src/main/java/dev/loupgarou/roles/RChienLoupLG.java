package dev.loupgarou.roles;

import org.bukkit.potion.PotionEffectType;

import dev.loupgarou.MainLg;
import dev.loupgarou.classes.LGCustomItems;
import dev.loupgarou.classes.LGGame;
import dev.loupgarou.classes.LGPlayer;
import dev.loupgarou.roles.utils.Role;
import dev.loupgarou.roles.utils.RoleType;
import dev.loupgarou.roles.utils.RoleWinType;

public class RChienLoupLG extends Role{
	public RChienLoupLG(LGGame game) {
		super(game);
	}
	@Override
	public int getMaxNb() {
		return Integer.MAX_VALUE;
	}
	@Override
	public String getColor() {
		for(LGPlayer lgp : getPlayers())
			if(lgp.getPlayer() != null && lgp.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY))
				return "§c";
		return (getPlayers().size() > 0 ? "§a" : "§c");
	}
	@Override
	public String getName() {
		return getColor() + "§lChien-Loup";
	}

	@Override
	public String getFriendlyName() {
		return "du "+getName();
	}

	@Override
	public String getShortDescription() {
		return "Tu gagnes avec le §a§lVillage";
	}

	@Override
	public String getDescription() {
		return "Tu gagnes avec le §a§lVillage§f. Au début de la première nuit, tu peux choisir entre rester fidèle aux §a§lVillageois§f ou alors rejoindre le clan des §c§lLoups-Garous§f.";
	}

	@Override
	public String getTask() {
		return "Souhaites-tu devenir un §c§lLoup-Garou§6 ?";
	}

	@Override
	public String getBroadcastedTask() {
		return "Le "+getName()+"§9 pourrait trouver de nouveaux amis...";
	}
	@Override
	public RoleType getType() {
		return RoleType.LOUP_GAROU;
	}
	@Override
	public RoleWinType getWinType() {
		return RoleWinType.LOUP_GAROU;
	}

	@Override
	public int getTimeout() {
		return -1;
	}
	
	@Override
	public void join(LGPlayer player, boolean sendMessage, boolean leavePrecedentRole) {
		super.join(player, sendMessage, leavePrecedentRole);
		MainLg.debug(getGame().getKey(), player.getName()+" a rejoint les LG (Chien Loup)");
		player.setRole(this);
		LGCustomItems.updateItem(player);
		for(Role role : getGame().getRoles())
			if(role instanceof RLoupGarou) {
				role.join(player, false, false);
				MainLg.debug(getGame().getKey(), player.getName()+" -> Chien LG -> Camp trouvé & join");
				for(LGPlayer lgp : role.getPlayers())
					if(lgp != player)
						lgp.sendMessage("§7§l"+player.getName()+"§6 a rejoint les §c§lLoups-Garous§6.");
			}
	}
}
