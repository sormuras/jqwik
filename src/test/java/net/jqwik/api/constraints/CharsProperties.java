package net.jqwik.api.constraints;

import net.jqwik.api.*;

@Group
class CharsProperties {

	@Property
	boolean fromTo(@ForAll @Chars(from = 'a', to = 'z') String aString) {
		return aString.chars().allMatch(aChar -> aChar >= 'a' && aChar <= 'z');
	}

	@Property
	boolean charArray(@ForAll @Chars({ '1', '2', '3' }) String aString) {
		return aString.chars().allMatch(aChar -> aChar >= '1' && aChar <= '3');
	}

	@Property
	boolean listOfChars(@ForAll @Chars({ '1', '2', '3' }) @Chars(from = 'a', to = 'z') String aString) {
		return aString.chars().allMatch(aChar -> //
		(aChar >= '1' && aChar <= '3') //
				|| (aChar >= 'a' && aChar <= 'z'));
	}
}