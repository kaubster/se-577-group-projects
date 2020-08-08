package edu.drexel.se577.grouptwo.viz.storage;

import java.util.Optional;

import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.visualization.Visualization;

public class EngineSingleton {

	protected static volatile EngineSingleton instance = null;
	
	protected EngineSingleton()
	{
		
	}
	public static EngineSingleton getInstance()
	{
		if(instance == null)
			instance = new EngineSingleton();
		return instance;
	}
	//@Override
	public Optional<Dataset> forId(String id) {
		// TODO Auto-generated method stub
		Optional<Dataset> dataset;
		
		return null;
	}

	//@Override
	public Dataset create(Definition definition) {
		// TODO Auto-generated method stub
		
		return null;
	}

	//@Override
	public String createViz(Visualization visualization) {
		// TODO Auto-generated method stub
		return null;
	}

}
