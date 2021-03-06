/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2009-2013 Ausenco Engineering Canada Inc.
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

import com.jaamsim.input.EntityInput;
import com.jaamsim.input.Input;
import com.jaamsim.input.InputAgent;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.OutputHandle;
import com.jaamsim.input.OutputInput;
import com.jaamsim.input.StringInput;
import com.jaamsim.units.Unit;

/**
 * The "Text" object displays written text within the 3D model universe.  Both fixed and variable text can be displayed.
 * @author Harry King
 *
 */
public class Text extends TextBasics {

	@Keyword(description = "The fixed and variable text to be displayed.  If spaces are included, enclose the text in single quotes.  " +
			"If variable text is to be displayed using the OutputName keyword, include the appropriate Java format in the text, " +
			"e.g. %s, %.6f, %.6g",
	         exampleList = {"'Present speed = %.3f m/s'"})
	protected final StringInput formatText;

	@Keyword(description = "The output value chain that returns the variable text to be displayed. " +
			"If more than one output value is given, all outputs but the last should point to an entity output to query" +
			" for the next output. The example returns the name of the product in a tank",
	         exampleList = {"Tank1 Product Name"})
	protected final OutputInput<Object> outputName;

	@Keyword(description = "The unit in which to express the output value",
	         exampleList = {"m/s"})
	protected final EntityInput<Unit> unit;

	@Keyword(description = "The text to display if there is any failure while formatting" +
	                       "the dynamic text, or while reading the output's value.",
	         exampleList = {"'Input Error'"})
	private final StringInput failText;

	protected String renderText = "";

	{
		formatText = new StringInput("Format", "Key Inputs", "");
		this.addInput(formatText);

		outputName = new OutputInput<>(Object.class, "OutputName", "Key Inputs", null);
		this.addInput(outputName);

		unit = new EntityInput<>( Unit.class, "Unit", "Key Inputs", null);
		this.addInput(unit);

		failText = new StringInput("FailText", "Key Inputs", "Input Error");
		this.addInput(failText);
	}

	public Text() {}

	@Override
	public void updateForInput(Input<?> in) {
		super.updateForInput(in);

		if (in == formatText) {
			setSavedText(formatText.getValue());
			return;
		}

		if (in == outputName) {
			OutputHandle h = outputName.getOutputHandle(0.0);
			if (h != null)
				unit.setSubClass(h.getUnitType());
			return;
		}
	}

	@Override
	public void setInputsForDragAndDrop() {
		super.setInputsForDragAndDrop();

		// Set the displayed text to the entity's name
		InputAgent.applyArgs(this, "Format", this.getName());
	}

	@Override
	public void acceptEdits() {
		super.acceptEdits();
		InputAgent.applyArgs(this, "Format", getEditText());
	}

	public String getRenderText(double simTime) {

		// If the object is selected, show the editable text
		if (isEditMode())
			return getEditText();

		if( outputName.getValue() == null )
			return formatText.getValue();

		try {
			OutputHandle out = outputName.getOutputHandle(simTime);
			if( out == null )
				return failText.getValue();

			if (out.isNumericValue()) {
				double d = out.getValueAsDouble(simTime, 0.0d, unit.getValue());
				return String.format(formatText.getValue(), d);
			}
			else {
				Object o = out.getValue(simTime, out.getReturnType());
				return String.format(formatText.getValue(), o);
			}
		}
		catch (Throwable e) {
			return failText.getValue();
		}
	}

	@Override
	public void updateGraphics(double simTime) {
		super.updateGraphics(simTime);

		// This text is cached because reflection is used to get it, so who knows how long it will take
		String newRenderText = getRenderText(simTime);
		if (newRenderText.equals(renderText)) {
			// Nothing important has changed
			return;
		}

		// The text has been updated
		renderText = newRenderText;
	}

	@Override
	public String getCachedText() {
		return renderText;
	}

}
