package dev.loupgarou.events;

import org.bukkit.event.Cancellable;

import dev.loupgarou.classes.LGGame;
import dev.loupgarou.classes.LGPlayer;
import lombok.Getter;
import lombok.Setter;

public class LGPyromaneGasoilEvent extends LGEvent implements Cancellable{
	public LGPyromaneGasoilEvent(LGGame game, LGPlayer player) {
		super(game);
		this.player = player;
	}
	
	@Getter @Setter private boolean cancelled;
	@Getter @Setter private LGPlayer player;
}