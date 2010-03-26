package choco.cp.solver.constraints.global.geost.geometricPrim;

import choco.kernel.model.variables.geost.ShiftedBox;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the class that represents a Shape. Each shape has a shape Id and a set of shifted boxes.
 */
public final class Shape {
	
	private int shapeId;
	
	private List<ShiftedBox> sb;
	
	public Shape()
	{
		this.sb = new ArrayList<ShiftedBox>();
		
	}
	
	public Shape(int id)
	{
		this.shapeId = id;
		this.sb = new ArrayList<ShiftedBox>();
	}

	public List<ShiftedBox> getShiftedBoxes() {
		return this.sb;
	}

	public void setShiftedBoxes(List<ShiftedBox> sb) {
		this.sb = sb;
	}

	public void addShiftedBox(ShiftedBox sb)
	{
		this.sb.add(sb);
	}
	
	public ShiftedBox getShiftedBox(int index)
	{
		return this.sb.get(index);
	}
	
	public void removeShiftedBox(int index)
	{
		this.sb.remove(index);
	}
	
	public void removeShiftedBox(ShiftedBox sb)
	{
		this.sb.remove(sb);
		
	}
	public int getShapeId() {
		return this.shapeId;
	}

	public void setShapeId(int shapeId) {
		this.shapeId = shapeId;
	}
	
	

}
