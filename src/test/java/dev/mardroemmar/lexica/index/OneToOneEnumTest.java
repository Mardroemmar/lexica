package dev.mardroemmar.lexica.index;

import static com.google.common.truth.Truth.assertThat;

import dev.mardroemmar.lexica.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@SuppressWarnings("ConstantConditions") // Shut up? Please? It's marked @Nullable, let me provide nulls!
class OneToOneEnumTest {
  private static final Index<EnumA, EnumB> ENUM_A_TO_B = Index.enums(EnumA.class, EnumB.class);

  @ParameterizedTest
  @CsvSource({
      "A,A",
      "B,B",
      "C,C",
  })
  void oneToOneEnums(final EnumA a, final EnumB b) {
    assertThat(ENUM_A_TO_B.value(a)).isEqualTo(b);
    assertThat(ENUM_A_TO_B.key(b)).isEqualTo(a);
  }

  @Test
  void oneToOneEnumMissingValues() {
    assertThat(ENUM_A_TO_B.value(EnumA.D)).isNull();
    assertThat(ENUM_A_TO_B.key(EnumB.E)).isNull();
  }

  private enum EnumA {
    A,
    B,
    C,
    D,
  }

  private enum EnumB {
    E,
    C,
    B,
    A,
  }
}
