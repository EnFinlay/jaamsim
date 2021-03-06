package com.jaamsim.BasicObjects;

import com.jaamsim.DisplayModels.ShapeModel;
import com.jaamsim.Graphics.DisplayEntity;
import com.jaamsim.input.ColourInput;
import com.jaamsim.input.Keyword;
import com.jaamsim.input.Output;
import com.jaamsim.input.OutputInput;
import com.jaamsim.input.StringInput;
import com.jaamsim.units.DimensionlessUnit;

public class BooleanIndicator extends DisplayEntity {

	private final OutputInput<Boolean> boolProp;

	@Keyword(description = "The colour of the indicator when the property is true",
	         exampleList = {"green"})
	private final ColourInput trueColor;

	@Keyword(description = "The colour of the indicator when the property is false",
	         exampleList = {"red"})
	private final ColourInput falseColor;

	@Keyword(description = "The string displayed by the Text output when the property is true.",
	         exampleList = {"'True text'"})
	private final StringInput trueText;

	@Keyword(description = "The string displayed by the Text output when the property is false.",
	         exampleList = {"'False text'"})
	private final StringInput falseText;

	{
		boolProp = new OutputInput<>(Boolean.class, "OutputName", "Key Inputs", null);
		boolProp.setRequired(true);
		this.addInput(boolProp);

		trueColor = new ColourInput("TrueColour", "Graphics", ColourInput.GREEN);
		this.addInput(trueColor);
		this.addSynonym(trueColor, "TrueColor");

		falseColor = new ColourInput("FalseColour", "Graphics", ColourInput.RED);
		this.addInput(falseColor);
		this.addSynonym(falseColor, "FalseColor");

		trueText = new StringInput("TrueText", "Graphics", "TRUE");
		this.addInput(trueText);

		falseText = new StringInput("FalseText", "Graphics", "FALSE");
		this.addInput(falseText);
	}

	public BooleanIndicator() {
	}

	@Override
	public void updateGraphics( double time ) {
		if (boolProp.getValue() == null)
			return;
		Boolean b = boolProp.getOutputValue(time);
		if (b.booleanValue()) {
			setTagColour(ShapeModel.TAG_CONTENTS, trueColor.getValue());
		}
		else {
			setTagColour(ShapeModel.TAG_CONTENTS, falseColor.getValue());
		}
	}

	@Output(name = "Text",
	 description = "If the property is true, then return TrueText.  If the property is false, then return FalseText.",
	    unitType = DimensionlessUnit.class)
	public String getText(double time) {
		if (boolProp.getValue() == null)
			return "";
		Boolean b = boolProp.getOutputValue(time);
		if (b.booleanValue()) {
			return trueText.getValue();
		}
		else {
			return falseText.getValue();
		}
	}
}
