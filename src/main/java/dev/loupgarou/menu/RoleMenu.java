package dev.loupgarou.menu;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import dev.loupgarou.MainLg;
import dev.loupgarou.classes.LGCustomItems;
import dev.loupgarou.classes.LGPlayer;
import dev.loupgarou.roles.utils.FakeRoles;
import dev.loupgarou.utils.InteractInventory;
import dev.loupgarou.utils.InteractInventory.InventoryCall;
import dev.loupgarou.utils.ItemBuilder;

public class RoleMenu {
	
	public static void openMenu(LGPlayer lgp) {
		if(lgp.getGame() == null) {
			lgp.sendMessage("§cVous n'êtes pas en partie...");
			return;
		}
		
		InteractInventory ii = new InteractInventory(Bukkit.createInventory(null, 4 * 9, "Sélection des rôles"));
		
		int i = 0;
		int total = 0;
		for(String roleName : MainLg.getInstance().getRoles().keySet()) {
			int nbRole = lgp.getGame().getConfig().getRoles().get(roleName);
			total += nbRole;
			ii.registerItem(
					new ItemBuilder(LGCustomItems.getItemMenu(FakeRoles.getRole(roleName)))
						.name(FakeRoles.getRole(roleName).getColor() + roleName)
						.lore(Arrays.asList(
								"§7" + nbRole,
								"",
								"§f" + optimizeLines(FakeRoles.getRole(roleName).getDescription())
								))
						.build(), 
					i, true, new InventoryCall() {
						
						@Override
						public void click(HumanEntity human, ItemStack item, ClickType clickType) {
							if(lgp.getGame().getOwner() != lgp) {
								lgp.sendMessage("§cVous n'êtes pas le propriétaire de la partie...");
								return;
							}
							
							int modif = 0;
								
							switch(clickType) {
							
							case RIGHT:
							case LEFT:
							case MIDDLE:
								modif = +1;
								break;
									
							case SHIFT_LEFT:
							case SHIFT_RIGHT:
								modif = -1;
								if(nbRole <= 0)
									modif = 0;
								break;
									
							default:
								return;
							}
								
							if(modif != 0) {
								human.sendMessage(MainLg.getPrefix()+"§6Il y aura §e" + (nbRole + modif) + " §6" + roleName);
								lgp.getGame().getConfig().getRoles().replace(roleName, nbRole + modif);
							}
								
							openMenu(lgp);
						}
			});
			i++;
		}
		
		ii.registerItem(
				new ItemBuilder(Material.GOLD_NUGGET)
					.name("§aTotal : " + total)
					.build(), 
				4*9-1, true, null);
		
		ii.openTo(lgp.getPlayer());
	}
	
	/*
	 */
	
	public static String optimizeLines(String lore) {
		int maxWord = 5;
		
		int a = 0;
		int b = 0;
		
		for(String s : lore.split(" ")){
			a += s.length();
			b++;
			if(a >= 40){
				maxWord--;
			}
			if(b >= maxWord){
				a = 0;
				b = 0;
			}
		}
		
		return optimizeLines(lore, maxWord);
	}

	public static String optimizeLines(String text, int nbWordPerLines) {
    	String result = "";
    	String[] words = text.split(" ");
    	
    	String currentLine = "";
    	int wordCountInLine = 0;
    	for (String word : words) {
    		currentLine += word + " ";
    		
    		if (wordCountInLine >= nbWordPerLines) {
    			result += currentLine + "\n";
    			char color = 'f';
    			
    			char[] charCurrentLine = currentLine.toCharArray();
    			for (int j = 0; j < charCurrentLine.length; j++)
    				if(charCurrentLine[j] == '§')
    					color = charCurrentLine[j + 1];
    			
    			currentLine = "§" + color;
    			wordCountInLine = 0;
    		}
    		wordCountInLine++;
    	}
		result += currentLine + "\n";//Add final line
    	
    	return result;
	}
	
}
