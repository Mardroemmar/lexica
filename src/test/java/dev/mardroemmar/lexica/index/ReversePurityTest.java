package dev.mardroemmar.lexica.index;

import static com.google.common.truth.Truth.assertThat;

import dev.mardroemmar.lexica.Index;
import org.junit.jupiter.api.Test;

class ReversePurityTest {
  @Test
  void reversePurity() {
    final var original = Index.keyToValue(String::toLowerCase, "A", "B", "C");
    final var reversed = original.reversed();
    assertThat(original.reversed()).isSameInstanceAs(reversed);
    assertThat(original.reversed().reversed()).isSameInstanceAs(original);
    assertThat(reversed.reversed()).isSameInstanceAs(original);
    assertThat(original.reversed().reversed().reversed()).isSameInstanceAs(reversed);
    assertThat(reversed.reversed().reversed()).isSameInstanceAs(reversed);
  }
}
