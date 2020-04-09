package dev.loupgarou.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.loupgarou.MainLg;
import dev.loupgarou.classes.LGMaps.LGMap;
import dev.loupgarou.roles.utils.Role;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor @ToString
public class LGGameConfig {
	
	@Getter private final Map<Class<? extends Role>, Integer> roles = new HashMap<Class<? extends Role>, Integer>();
	@Getter @Setter private boolean hideRole = false;
	@Getter @Setter private boolean hideVote = false;
	@Getter @Setter private boolean hideVoteExtra = false;
	@Getter @Setter private int timerDayPerPlayer = 15;
	@Getter @Setter @NonNull private CommunicationType com = CommunicationType.TEXTUEL;
	
	@Getter @NonNull private final LGMap map;
	@Getter private final boolean privateGame;
	@Getter private final List<String> banned = new ArrayList<String>();
	
	{//Init map
		for(Class<? extends Role> roleClazz : MainLg.getInstance().getRoles().keySet())
			this.roles.put(roleClazz, 0);
	}
	
	/*
	 * Methods
	 */
	
	public int getTotalConfiguredRoles(){
		int total = 0;
		for(Entry<Class<? extends Role>, Integer> entry : roles.entrySet())
			total += entry.getValue();
		return total;
	}
	
	/*
	 * Custom Types
	 */
	
	public enum CommunicationType {
		TEXTUEL,
		DISCORD;
	}
}
