package net.jqwik.execution.providers;

import net.jqwik.properties.*;

import java.util.*;

public class SetArbitraryProvider extends AbstractCollectionArbitraryProvider {

	@Override
	protected Class<?> getProvidedType() {
		return Set.class;
	}

	@Override
	protected Arbitrary<?> create(Arbitrary<?> innerArbitrary) {
		return Arbitraries.setOf(innerArbitrary);
	}
}