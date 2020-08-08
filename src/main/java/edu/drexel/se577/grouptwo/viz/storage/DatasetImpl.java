package edu.drexel.se577.grouptwo.viz.storage;

import java.util.List;

import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;

public class DatasetImpl implements Dataset {
	
	protected String datasetID, name;
	protected Definition definition;
	protected List<Sample> samples;

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return datasetID;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public Definition getDefinition() {
		// TODO Auto-generated method stub
		return definition;
	}

	@Override
	public List<Sample> getSamples() {
		// TODO Auto-generated method stub
		return samples;
	}
	
	

	@Override
	public void addSample(Sample sample) {
		// TODO Auto-generated method stub
		
	}

}
