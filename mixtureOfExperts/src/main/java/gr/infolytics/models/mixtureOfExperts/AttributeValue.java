package gr.infolytics.models.mixtureOfExperts;


public class AttributeValue<E> implements Comparable<AttributeValue<E>>{
	private E index;
	private float value;

	public AttributeValue(E i, float v){
		index = i;
		value = v;
	}

	public E getIndex(){
		return index;
	}
	public float getValue(){
		return value;
	}
	
	public int compareTo(AttributeValue<E> av){
		if (value - av.getValue() < 0)
			return 1;
		else if (value - av.getValue() > 0)
			return -1;
		return 0;
	}
}
