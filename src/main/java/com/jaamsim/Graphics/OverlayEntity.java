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
package com.jaamsim.Graphics;

import com.jaamsim.datatypes.IntegerVector;
import com.jaamsim.input.BooleanInput;
import com.jaamsim.input.IntegerListInput;
import com.jaamsim.input.Keyword;

/**
 * OverlayEntity is the superclass for DisplayEntities that have 2D overlay graphics instead of 3D graphics.  Overlay graphics
 * are those that appear relative to the View window and whose position and size are specified in pixels.
 * @author Harry King
 *
 */
public abstract class OverlayEntity extends DisplayEntity {

	@Keyword(description = "The position of the overlay, from the upper left corner of the window to the upper left corner " +
	                "of the overlay. Value is in pixels",
	         exampleList = {"20 20"})
	private final IntegerListInput screenPosition;

	@Keyword(description = "If this overlay should be aligned from the right edge of the window (instead of the left)",
	         exampleList = {"TRUE"})
	private final BooleanInput alignRight;

	@Keyword(description = "If this overlay should be aligned from the bottom edge of the window (instead of the top)",
	         exampleList = {"TRUE"})
	private final BooleanInput alignBottom;

	{
		IntegerVector defPos = new IntegerVector(2);
		defPos.add(10);
		defPos.add(10);
		screenPosition = new IntegerListInput("ScreenPosition", "Graphics", defPos);
		screenPosition.setValidCount(2);
		screenPosition.setValidRange(0, 2500);
		this.addInput(screenPosition);

		alignRight = new BooleanInput("AlignRight", "Graphics", false);
		this.addInput(alignRight);

		alignBottom = new BooleanInput("AlignBottom", "Graphics", false);
		this.addInput(alignBottom);

		getInput("Position").setHidden(true);
		getInput("Alignment").setHidden(true);
		getInput("Size").setHidden(true);
		getInput("Orientation").setHidden(true);
		getInput("Region").setHidden(true);
		getInput("RelativeEntity").setHidden(true);
		getInput("Active").setHidden(true);
		getInput("Show").setHidden(true);
		getInput("Movable").setHidden(true);
	}

	public boolean getAlignRight() {
		return alignRight.getValue();
	}

	public boolean getAlignBottom() {
		return alignBottom.getValue();
	}

	public IntegerVector getScreenPosition() {
		return screenPosition.getValue();
	}

}
