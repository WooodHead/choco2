package choco.cp.solver.constraints.global.geost.geometricPrim;

import choco.kernel.model.variables.geost.ShiftedBox;

import java.util.Vector;

/**
 * This is the class that represents a Shape. Each shape has a shape Id and a set of shifted boxes.
 */
public class Shape {
	
	private int shapeId;
	
	private Vector<ShiftedBox> sb;
	
	public Shape()
	{
		this.sb = new Vector<ShiftedBox>();
		
	}
	
	public Shape(int id)
	{
		this.shapeId = id;
		this.sb = new Vector<ShiftedBox>();
	}

	public Vector<ShiftedBox> getShiftedBoxes() {
		return this.sb;
	}

	public void setShiftedBoxes(Vector<ShiftedBox> sb) {
		this.sb = sb;
	}

	public void addShiftedBox(ShiftedBox sb)
	{
		this.sb.add(sb);
	}
	
	public ShiftedBox getShiftedBox(int index)
	{
		return this.sb.elementAt(index);
	}
	
	public void removeShiftedBox(int index)
	{
		this.sb.removeElementAt(index);
	}
	
	public void removeShiftedBox(ShiftedBox sb)
	{
		this.sb.removeElement(sb);
		
	}
	public int getShapeId() {
		return this.shapeId;
	}

	public void setShapeId(int shapeId) {
		this.shapeId = shapeId;
	}
	
	

}