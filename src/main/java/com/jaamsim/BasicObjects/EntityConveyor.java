/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package com.jaamsim.BasicObjects;

import java.util.ArrayList;

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.input.ColourInput;
import com.jaamsim.input.Input;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.KeywordIndex;
import com.jaamsim.input.ValueInput;
import com.jaamsim.input.Vec3dListInput;
import com.jaamsim.math.Vec3d;
import com.jaamsim.render.HasScreenPoints;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.DistanceUnit;
import com.jaamsim.units.TimeUnit;

/**
 * Moves one or more Entities along a path at a constant speed.
 */
public class EntityConveyor extends LinkedService implements HasScreenPoints {

	@Keyword(description = "The travel time for the conveyor.",
	         exampleList = {"10.0 s"})
	private final ValueInput travelTimeInput;

    @Keyword(description = "A list of points in { x, y, z } coordinates defining the line segments that" +
            "make up the arrow.  When two coordinates are given it is assumed that z = 0." ,
             exampleList = {"{ 6.7 2.2 m } { 4.9 2.2 m } { 4.9 3.4 m }"})
	private final Vec3dListInput pointsInput;

	@Keyword(description = "The width of the Arrow line segments in pixels.",
	         exampleList = {"1"})
	private final ValueInput widthInput;

	@Keyword(description = "The colour of the arrow, defined using a colour keyword or RGB values.",
	         exampleList = {"red"})
	private final ColourInput colorInput;

	private final ArrayList<DisplayEntity> entityList;  // List of the entities being conveyed
	private final ArrayList<Double> startTimeList;  // List of times at which the entities entered the conveyor
	private double totalLength;  // Graphical length of the conveyor
	private final ArrayList<Double> lengthList;  // Length of each segment of the conveyor
	private final ArrayList<Double> cumLengthList;  // Total length to the end of each segment

	private Object screenPointLock = new Object();
	private HasScreenPoints.PointsInfo[] cachedPointInfo;

	{
		operatingThresholdList.setHidden(true);
		waitQueue.setHidden(true);
		match.setHidden(true);
		processPosition.setHidden(true);

		travelTimeInput = new ValueInput("TravelTime", "Key Inputs", 0.0d);
		travelTimeInput.setValidRange(0.0, Double.POSITIVE_INFINITY);
		travelTimeInput.setUnitType(TimeUnit.class);
		this.addInput(travelTimeInput);

		ArrayList<Vec3d> defPoints =  new ArrayList<>();
		defPoints.add(new Vec3d(0.0d, 0.0d, 0.0d));
		defPoints.add(new Vec3d(1.0d, 0.0d, 0.0d));
		pointsInput = new Vec3dListInput("Points", "Key Inputs", defPoints);
		pointsInput.setValidCountRange(2, Integer.MAX_VALUE );
		pointsInput.setUnitType(DistanceUnit.class);
		this.addInput(pointsInput);

		widthInput = new ValueInput("Width", "Key Inputs", 1.0d);
		widthInput.setUnitType(DimensionlessUnit.class);
		widthInput.setValidRange(1.0d, Double.POSITIVE_INFINITY);
		this.addInput(widthInput);

		colorInput = new ColourInput("Color", "Key Inputs", ColourInput.BLACK);
		this.addInput(colorInput);
		this.addSynonym(colorInput, "Colour");
	}

	public EntityConveyor() {
		entityList = new ArrayList<>();
		startTimeList = new ArrayList<>();
		lengthList = new ArrayList<>();
		cumLengthList = new ArrayList<>();
	}

	@Override
	public void earlyInit() {
		super.earlyInit();

		entityList.clear();
		startTimeList.clear();

	    // Initialize the segment length data
		lengthList.clear();
		cumLengthList.clear();
		totalLength = 0.0;
		for (int i = 1; i < pointsInput.getValue().size(); i++) {
			// Get length between points
			Vec3d vec = new Vec3d();
			vec.sub3(pointsInput.getValue().get(i), pointsInput.getValue().get(i-1));
			double length = vec.mag3();

			lengthList.add(length);
			totalLength += length;
			cumLengthList.add(totalLength);
		}
	}

	@Override
	public void addEntity(DisplayEntity ent ) {
		super.addEntity(ent);

		// Add the entity to the conveyor
		entityList.add(ent);
		startTimeList.add(this.getSimTime());

		// If necessary, wake up the conveyor
		if (!this.isBusy()) {
			this.setBusy(true);
			this.setPresentState();
			this.startAction();
		}
	}

	@Override
	public void startAction() {

		// Schedule the next entity to reach the end of the conveyor
		double dt = startTimeList.get(0) + travelTimeInput.getValue() - this.getSimTime();
		dt = Math.max(dt, 0);  // Round-off to the nearest tick can cause a negative value
		this.scheduleProcess(dt, 5, endActionTarget);
	}

	@Override
	public void endAction() {

		// Remove the entity from the conveyor
		DisplayEntity ent = entityList.remove(0);
		startTimeList.remove(0);

		// Send the entity to the next component
		this.sendToNextComponent(ent);

		// Stop if the conveyor is empty
		if (entityList.isEmpty()) {
			this.setBusy(false);
			this.setPresentState();
			return;
		}

		// Schedule the next entity to reach the end of the conveyor
		this.startAction();
	}

	/**
	 * Return the position coordinates for a given distance along the conveyor.
	 * @param dist = distance along the conveyor.
	 * @return position coordinates
	 */
	private Vec3d getPositionForDistance(double dist) {

		// Find the present segment
		int seg = 0;
		for (int i = 0; i < cumLengthList.size(); i++) {
			if (dist <= cumLengthList.get(i)) {
				seg = i;
				break;
			}
		}

		// Interpolate between the start and end of the segment
		double frac = 0.0;
		if (seg == 0) {
			frac = dist / lengthList.get(0);
		}
		else {
			frac = ( dist - cumLengthList.get(seg-1) ) / lengthList.get(seg);
		}
		if (frac < 0.0)  frac = 0.0;
		else if (frac > 1.0)  frac = 1.0;

		Vec3d vec = new Vec3d();
		vec.interpolate3(pointsInput.getValue().get(seg), pointsInput.getValue().get(seg+1), frac);
		return vec;
	}

	@Override
	public void updateForInput(Input<?> in) {
		super.updateForInput(in);

		// If Points were input, then use them to set the start and end coordinates
		if (in == pointsInput || in == colorInput || in == widthInput) {
			synchronized(screenPointLock) {
				cachedPointInfo = null;
			}
			return;
		}
	}

	@Override
	public void updateGraphics(double simTime) {

		// Loop through the entities on the conveyor
		for (int i = 0; i < entityList.size(); i++) {
			DisplayEntity each = entityList.get(i);

			// Calculate the distance travelled by this entity
			double dist = (simTime - startTimeList.get(i)) / travelTimeInput.getValue() * totalLength;

			// Set the position for the entity
			each.setGlobalPosition(this.getPositionForDistance( dist));
		}
	}

	@Override
	public HasScreenPoints.PointsInfo[] getScreenPoints() {
		synchronized(screenPointLock) {
			if (cachedPointInfo == null) {
				cachedPointInfo = new HasScreenPoints.PointsInfo[1];
				HasScreenPoints.PointsInfo pi = new HasScreenPoints.PointsInfo();
				cachedPointInfo[0] = pi;

				pi.points = pointsInput.getValue();
				pi.color = colorInput.getValue();
				pi.width = widthInput.getValue().intValue();
				if (pi.width < 1) pi.width = 1;
			}
			return cachedPointInfo;
		}
	}

	@Override
	public boolean selectable() {
		return true;
	}

	/**
	 *  Inform simulation and editBox of new positions.
	 */
	@Override
	public void dragged(Vec3d dist) {
		KeywordIndex kw = InputAgent.formatPointsInputs(pointsInput.getKeyword(), pointsInput.getValue(), dist);
		InputAgent.apply(this, kw);
		super.dragged(dist);
	}

}
