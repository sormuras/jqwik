package net.jqwik.properties;

import net.jqwik.*;
import net.jqwik.api.*;
import net.jqwik.properties.arbitraries.*;
import org.assertj.core.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Group
class ArbitraryTests {

	private Random random = SourceOfRandomness.current();

	@Example
	void fixGenSize() {
		int[] injectedGenSize = {0};

		Arbitrary<Integer> arbitrary = genSize -> {
			injectedGenSize[0] = genSize;
			return ignore -> Shrinkable.unshrinkable(0);
		};

		RandomGenerator<Integer> notUsed = arbitrary.fixGenSize(42).generator(1000);
		Assertions.assertThat(injectedGenSize[0]).isEqualTo(42);
	}


	@Group
	class GeneratingAndShrinking {
		@Example
		void generateInteger() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			RandomGenerator<Integer> generator = arbitrary.generator(10);

			assertThat(generator.next(random).value()).isEqualTo(1);
			assertThat(generator.next(random).value()).isEqualTo(2);
			assertThat(generator.next(random).value()).isEqualTo(3);
			assertThat(generator.next(random).value()).isEqualTo(4);
			assertThat(generator.next(random).value()).isEqualTo(5);
			assertThat(generator.next(random).value()).isEqualTo(1);
		}

		@Example
		void shrinkInteger() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			RandomGenerator<Integer> generator = arbitrary.generator(10);

			Shrinkable<Integer> value5 = generateNth(generator, 5);
			assertThat(value5.value()).isEqualTo(5);

			Set<ShrinkResult<Shrinkable<Integer>>> shrunkValues = value5.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(1);
			ShrinkResult<Shrinkable<Integer>> shrunkValue = shrunkValues.iterator().next();
			assertThat(shrunkValue.shrunkValue().value()).isEqualTo(4);
			assertThat(shrunkValue.shrunkValue().distance()).isEqualTo(3);
		}

		@Example
		void generateList() {
			Arbitrary<List<Integer>> arbitrary = new ListArbitraryForTests(5);
			RandomGenerator<List<Integer>> generator = arbitrary.generator(10);

			assertThat(generator.next(random).value()).isEmpty();
			assertThat(generator.next(random).value()).containsExactly(1);
			assertThat(generator.next(random).value()).containsExactly(1, 2);
			assertThat(generator.next(random).value()).containsExactly(1, 2, 3);
			assertThat(generator.next(random).value()).containsExactly(1, 2, 3, 4);
			assertThat(generator.next(random).value()).containsExactly(1, 2, 3, 4, 5);
			assertThat(generator.next(random).value()).isEmpty();
			assertThat(generator.next(random).value()).containsExactly(1);
		}

		@Example
		void shrinkList() {
			Arbitrary<List<Integer>> arbitrary = new ListArbitraryForTests(5);
			RandomGenerator<List<Integer>> generator = arbitrary.generator(10);

			Shrinkable<List<Integer>> value5 = generateNth(generator, 6);
			assertThat(value5.value()).containsExactly(1, 2, 3, 4, 5);

			Set<ShrinkResult<Shrinkable<List<Integer>>>> shrunkValues = value5.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(2);
			shrunkValues.forEach(shrunkValue -> {
				assertThat(shrunkValue.shrunkValue().value()).hasSize(4);
				assertThat(shrunkValue.shrunkValue().distance()).isEqualTo(4);
			});
		}

		@Example
		void samplesArePrependedToGeneration() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2);
			Arbitrary<Integer> arbitraryWithSamples = arbitrary.withSamples(-1, -2);
			RandomGenerator<Integer> generator = arbitraryWithSamples.generator(10);

			assertThat(generator.next(random).value()).isEqualTo(-1);
			assertThat(generator.next(random).value()).isEqualTo(-2);
			assertThat(generator.next(random).value()).isEqualTo(1);
			assertThat(generator.next(random).value()).isEqualTo(2);
			assertThat(generator.next(random).value()).isEqualTo(1);
		}

	}

	@Group
	class Filtering {
		@Example
		void filterInteger() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<Integer> filtered = arbitrary.filter(anInt -> anInt % 2 != 0);
			RandomGenerator<Integer> generator = filtered.generator(10);

			assertThat(generator.next(random).value()).isEqualTo(1);
			assertThat(generator.next(random).value()).isEqualTo(3);
			assertThat(generator.next(random).value()).isEqualTo(5);
			assertThat(generator.next(random).value()).isEqualTo(1);
		}

		@Example
		void shrinkFilteredInteger() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<Integer> filtered = arbitrary.filter(anInt -> anInt % 2 != 0);
			RandomGenerator<Integer> generator = filtered.generator(10);

			Shrinkable<Integer> value5 = generateNth(generator, 3);
			assertThat(value5.value()).isEqualTo(5);
			Set<ShrinkResult<Shrinkable<Integer>>> shrunkValues = value5.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(1);

			ShrinkResult<Shrinkable<Integer>> shrunkValue = shrunkValues.iterator().next();
			assertThat(shrunkValue.shrunkValue().value()).isEqualTo(3);
			assertThat(shrunkValue.shrunkValue().distance()).isEqualTo(2);
		}

		@Example
		void filterList() {
			Arbitrary<List<Integer>> arbitrary = new ListArbitraryForTests(5);
			Arbitrary<List<Integer>> filtered = arbitrary.filter(aList -> aList.size() % 2 != 0);
			RandomGenerator<List<Integer>> generator = filtered.generator(10);

			assertThat(generator.next(random).value()).containsExactly(1);
			assertThat(generator.next(random).value()).containsExactly(1, 2, 3);
			assertThat(generator.next(random).value()).containsExactly(1, 2, 3, 4, 5);
			assertThat(generator.next(random).value()).containsExactly(1);
		}

		@Example
		void shrinkFilteredList() {
			Arbitrary<List<Integer>> arbitrary = new ListArbitraryForTests(5);
			Arbitrary<List<Integer>> filtered = arbitrary.filter(aList -> aList.size() % 2 != 0);
			RandomGenerator<List<Integer>> generator = filtered.generator(10);

			Shrinkable<List<Integer>> value5 = generateNth(generator, 3);
			assertThat(value5.value()).containsExactly(1, 2, 3, 4, 5);

			Set<ShrinkResult<Shrinkable<List<Integer>>>> shrunkValues = value5.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(3); // [1,2,3] [2,3,4] [3,4,5]
			shrunkValues.forEach(shrunkValue -> {
				assertThat(shrunkValue.shrunkValue().value()).hasSize(3);
				assertThat(shrunkValue.shrunkValue().distance()).isEqualTo(3);
			});
		}

		@Example
		void failIfFilterWillDiscard10000ValuesInARow() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<Integer> filtered = arbitrary.filter(anInt -> false);
			RandomGenerator<Integer> generator = filtered.generator(10);

			assertThatThrownBy(() -> generator.next(random).value()).isInstanceOf(JqwikException.class);
		}


	}

	@Group
	class Mapping {

		@Example
		void mapIntegerToString() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<String> mapped = arbitrary.map(anInt -> "value=" + anInt);
			RandomGenerator<String> generator = mapped.generator(10);

			assertThat(generator.next(random).value()).isEqualTo("value=1");
			assertThat(generator.next(random).value()).isEqualTo("value=2");
			assertThat(generator.next(random).value()).isEqualTo("value=3");
			assertThat(generator.next(random).value()).isEqualTo("value=4");
			assertThat(generator.next(random).value()).isEqualTo("value=5");
			assertThat(generator.next(random).value()).isEqualTo("value=1");
		}

		@Example
		void shrinkIntegerMappedToString() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<String> mapped = arbitrary.map(anInt -> "value=" + anInt);
			RandomGenerator<String> generator = mapped.generator(10);

			Shrinkable<String> value5 = generateNth(generator, 5);
			assertThat(value5.value()).isEqualTo("value=5");
			Set<ShrinkResult<Shrinkable<String>>> shrunkValues = value5.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(1);

			ShrinkResult<Shrinkable<String>> shrunkValue = shrunkValues.iterator().next();
			assertThat(shrunkValue.shrunkValue().value()).isEqualTo("value=4");
			assertThat(shrunkValue.shrunkValue().distance()).isEqualTo(3);
		}

		@Example
		void shrinkFilteredIntegerMappedToString() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<Integer> filtered = arbitrary.filter(anInt -> anInt % 2 != 0);
			Arbitrary<String> mapped = filtered.map(anInt -> "value=" + anInt);
			RandomGenerator<String> generator = mapped.generator(10);

			Shrinkable<String> value5 = generateNth(generator, 3);
			assertThat(value5.value()).isEqualTo("value=5");
			Set<ShrinkResult<Shrinkable<String>>> shrunkValues = value5.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(1);

			ShrinkResult<Shrinkable<String>> shrunkValue = shrunkValues.iterator().next();
			assertThat(shrunkValue.shrunkValue().value()).isEqualTo("value=3");
			assertThat(shrunkValue.shrunkValue().distance()).isEqualTo(2);
		}
	}

	@Group
	class FlatMapping {

		@Example
		void flatMapIntegerToString() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<String> mapped = arbitrary.flatMap(anInt -> Arbitraries.strings() //
					.withCharRange('a', 'e') //
					.ofMinLength(anInt).ofMaxLength(anInt));

			RandomGenerator<String> generator = mapped.generator(10);

			assertThat(generator.next(random).value()).hasSize(1);
			assertThat(generator.next(random).value()).hasSize(2);
			assertThat(generator.next(random).value()).hasSize(3);
			assertThat(generator.next(random).value()).hasSize(4);
			assertThat(generator.next(random).value()).hasSize(5);

			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, s -> s.startsWith("a"));
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, s -> s.startsWith("b"));
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, s -> s.startsWith("c"));
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, s -> s.startsWith("d"));
			ArbitraryTestHelper.assertAtLeastOneGenerated(generator, s -> s.startsWith("e"));
		}

		@Property(tries = 50)
		void shrinkIntegerFlatMappedToString() {
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<String> mapped = arbitrary
					.flatMap(anInt -> Arbitraries.strings().withCharRange('a', 'b').ofLength(anInt * 2));
			RandomGenerator<String> generator = mapped.generator(10);

			Shrinkable<String> value5 = generateNth(generator, 5);
			assertThat(value5.value()).hasSize(10);
			assertThat(value5.value()).matches("[ab]+");

			Set<ShrinkResult<Shrinkable<String>>> shrunkValues = value5.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(1);

			ShrinkResult<Shrinkable<String>> shrunkValue = shrunkValues.iterator().next();
			assertThat(shrunkValue.shrunkValue().value()).hasSize(8);
			assertThat(shrunkValue.shrunkValue().value()).matches("[ab]+");

			// distance of underlying * 100 + distance of embedded
			assertThat(shrunkValue.shrunkValue().distance()).isGreaterThanOrEqualTo(3 * 100 + 8);
			assertThat(shrunkValue.shrunkValue().distance()).isLessThanOrEqualTo(3 * 100 + 16);
		}

		@Example
		void shrinkAlsoEmbeddedValueWhenFlatMapped() {
			Arbitrary<Integer> inner = random -> RandomGenerators.integers(1, 10);
			Arbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			Arbitrary<Integer> mapped = arbitrary.flatMap(anInt -> inner.map(i -> i * anInt));
			RandomGenerator<Integer> generator = mapped.generator(1);

			Shrinkable<Integer> value5 = generateNth(generator, 5);
			assertThat(value5.value()).isGreaterThanOrEqualTo(5);
			assertThat(value5.value()).isLessThanOrEqualTo(100);

			ShrinkResult<Shrinkable<Integer>> result = new ValueShrinker<>(value5, ignore -> {}, ShrinkingMode.FULL).shrink(MockFalsifier.falsifyAll(), null);
			assertThat(result.shrunkValue().value()).isEqualTo(1);

		}

	}

	@Group
	class Combination {

		@Example
		void generateCombination() {
			Arbitrary<Integer> a1 = new ArbitraryWheelForTests<>(1, 2, 3);
			Arbitrary<Integer> a2 = new ArbitraryWheelForTests<>(4, 5, 6);
			Arbitrary<String> combined = Combinators.combine(a1, a2).as((i1, i2) -> i1 + ":" + i2);
			RandomGenerator<String> generator = combined.generator(10);

			assertThat(generator.next(random).value()).isEqualTo("1:4");
			assertThat(generator.next(random).value()).isEqualTo("2:5");
			assertThat(generator.next(random).value()).isEqualTo("3:6");
			assertThat(generator.next(random).value()).isEqualTo("1:4");
		}

		@Example
		void shrinkCombination() {
			Arbitrary<Integer> a1 = new ArbitraryWheelForTests<>(1, 2, 3);
			Arbitrary<Integer> a2 = new ArbitraryWheelForTests<>(4, 5, 6);
			Arbitrary<String> combined = Combinators.combine(a1, a2).as((i1, i2) -> i1 + ":" + i2);
			RandomGenerator<String> generator = combined.generator(10);

			Shrinkable<String> value3to6 = generateNth(generator, 3);
			assertThat(value3to6.value()).isEqualTo("3:6");

			Set<ShrinkResult<Shrinkable<String>>> shrunkValues = value3to6.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(2); // 2:6 3:5
			shrunkValues.forEach(shrunkValue -> {
				assertThat(shrunkValue.shrunkValue().value()).isIn("2:6", "3:5");
				assertThat(shrunkValue.shrunkValue().distance()).isEqualTo(3); // sum of single distances
			});
		}

		@Example
		void shrinkListCombinedWithInteger() {
			Arbitrary<List<Integer>> lists = new ListArbitraryForTests(2);
			Arbitrary<Integer> integers = new ArbitraryWheelForTests<>(0, 1, 2);
			Arbitrary<String> combined = Combinators.combine(lists, integers).as((l, i) -> l.toString() + ":" + i);

			RandomGenerator<String> generator = combined.generator(10);

			Shrinkable<String> combinedString = generateNth(generator, 3);
			assertThat(combinedString.value()).isEqualTo("[1, 2]:2");

			Set<ShrinkResult<Shrinkable<String>>> shrunkValues = combinedString.shrinkNext(MockFalsifier.falsifyAll());
			assertThat(shrunkValues).hasSize(3);
			shrunkValues.forEach(shrunkValue -> {
				assertThat(shrunkValue.shrunkValue().value()).isIn("[1]:2", "[2]:2", "[1, 2]:1");
				assertThat(shrunkValue.shrunkValue().distance()).isEqualTo(3); // sum of single distances
			});
		}

	}

	private <T> Shrinkable<T> generateNth(RandomGenerator<T> generator, int n) {
		Shrinkable<T> generated = null;
		for (int i = 0; i < n; i++) {
			generated = generator.next(random);
		}
		return generated;
	}

}
