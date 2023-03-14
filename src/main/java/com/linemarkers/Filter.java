package com.linemarkers;

enum Filter
{
	ALL,
	REGION,
	VISIBLE,
	INVISIBLE;

	private static final Filter[] FILTERS = values();

	public Filter next()
	{
		return FILTERS[(this.ordinal() + 1) % FILTERS.length];
	}
}
