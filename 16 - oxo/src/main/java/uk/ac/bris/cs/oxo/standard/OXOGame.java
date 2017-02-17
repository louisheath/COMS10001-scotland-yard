package uk.ac.bris.cs.oxo.standard;

import uk.ac.bris.cs.oxo.Spectator;

/**
 * An OXO game model
 */
public interface OXOGame extends OXOView {

	/**
	 * Registers a spectator for the game
	 * 
	 * @param spectators the spectators to register; not null but could be an
	 *        empty array
	 */
	void registerSpectators(Spectator... spectators);

	/**
	 * Unregisters a spectator from the game
	 * 
	 * @param spectators the spectators to unregister; not null and must be
	 *        registered with {@link #registerSpectators(Spectator...)} prior to
	 *        this call, could be an empty array
	 */
	void unregisterSpectators(Spectator... spectators);

	/**
	 * Start the game
	 */
	void start();

}
