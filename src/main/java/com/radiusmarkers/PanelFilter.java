package com.radiusmarkers;

enum PanelFilter
{
	ALL,
	REGION,
	VISIBLE,
	INVISIBLE;

	private static final PanelFilter[] FILTERS = values();

	public PanelFilter next()
	{
		return FILTERS[(this.ordinal() + 1) % FILTERS.length];
	}
}
