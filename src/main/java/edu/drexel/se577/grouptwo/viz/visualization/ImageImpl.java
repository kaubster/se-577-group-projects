package edu.drexel.se577.grouptwo.viz.visualization;

class ImageImpl implements Visualization.Image {

	String type;
	byte[] dataArray;
	
	public ImageImpl(String type, byte[] data)
	{
		this.type = type;
		this.dataArray = data;
	}
	
	@Override
	public String mimeType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public byte[] data() {
		// TODO Auto-generated method stub
		return dataArray;
	}

}
