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

import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.Samples.SampleExpInput;
import com.jaamsim.input.EntityListInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.units.DimensionlessUnit;

public class Branch extends LinkedComponent {

	@Keyword(description = "The list of possible next objects to which the processed DisplayEntity can be passed.",
	         exampleList = {"Queue1 Queue2"})
	protected final EntityListInput<LinkedComponent> nextComponentList;

	@Keyword(description = "A number that determines the choice of next component:\n" +
			"     1 = first branch, 2 = second branch, etc.\n" +
			"A constant value, a distribution to be sampled, or a time series can be entered.",
	         exampleList = {"2", "DiscreteDistribution1", "'indexOfMin([Queue1].QueueLength, [Queue2].QueueLength)'"})
	private final SampleExpInput choice;

	{
		nextComponent.setHidden(true);

		nextComponentList = new EntityListInput<>(LinkedComponent.class, "NextComponentList", "Key Inputs", null);
		nextComponentList.setRequired(true);
		this.addInput(nextComponentList);

		choice = new SampleExpInput("Choice", "Key Inputs", null);
		choice.setUnitType(DimensionlessUnit.class );
		choice.setEntity(this);
		choice.setValidRange(1, Double.POSITIVE_INFINITY);
		choice.setRequired(true);
		this.addInput(choice);
	}

	@Override
	public void addEntity( DisplayEntity ent ) {
		super.addEntity(ent);

		// Choose the next component for this entity
		int i = (int) choice.getValue().getNextSample(this.getSimTime());
		if (i<1 || i>nextComponentList.getValue().size())
			error("Chosen index i=%s is out of range for NextComponentList: %s.",
			      i, nextComponentList.getValue());

		// Set the standard outputs for a LinkedComponent
		this.sendToNextComponent(ent);

		// Pass the entity to the selected next component
		nextComponentList.getValue().get(i-1).addEntity(ent);
	}

}
