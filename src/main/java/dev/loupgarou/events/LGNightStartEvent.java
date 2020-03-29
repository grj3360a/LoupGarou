package dev.loupgarou.events;

import dev.loupgarou.classes.LGGame;
import lombok.NonNull;

/**
 * Called when a day ended, after a people vote.
 * This is just before each role has onNightTurn().
 * Ambiance is already set to night time and nobody has day chat activated
 */
public class LGNightStartEvent extends LGEvent {
	public LGNightStartEvent(@NonNull LGGame game) {
		super(game);
	}
}