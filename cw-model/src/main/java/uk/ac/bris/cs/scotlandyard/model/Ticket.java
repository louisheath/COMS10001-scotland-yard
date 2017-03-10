package uk.ac.bris.cs.scotlandyard.model;

import java.util.Objects;

/**
 * Ticket types for the Scotland Yard game
 */
public enum Ticket {
	Bus, Taxi, Underground, Double, Secret;

	/**
	 * Finds the ticket for a given transport type
	 *
	 * @param transport the given transport, not null
	 * @return the ticket matching the transport type; never null
	 */
	public static Ticket fromTransport(Transport transport) {
		switch (Objects.requireNonNull(transport)) {
		case Taxi:
			return Taxi;
		case Bus:
			return Bus;
		case Underground:
			return Underground;
		case Boat:
			return Secret;
		default:
			return Taxi;
		}
	}

}
