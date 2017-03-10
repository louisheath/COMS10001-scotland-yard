package uk.ac.bris.cs.scotlandyard.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Includes all test for the actual game model
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		ModelCreationTest.class,
		ModelGameOverTest.class,
		ModelTwoPlayerPlayOutTest.class,
		ModelSixPlayerPlayOutTest.class,
		ModelRoundTest.class,
		ModelPlayerTest.class,
		ModelValidMoveTest.class,
		ModelSpectatorTest.class })
public class ModelTest {}
