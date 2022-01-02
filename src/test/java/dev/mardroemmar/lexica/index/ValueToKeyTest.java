package dev.mardroemmar.lexica.index;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import dev.mardroemmar.lexica.Index;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValueToKeyTest {
  @ParameterizedTest
  @MethodSource("dataDrivenIndexSource")
  void dataDrivenIndex(final String name, final DataDrivenModel model) {
    assertThat(DataDrivenModel.INDEX.value(name)).isSameInstanceAs(model);
    assertThat(DataDrivenModel.INDEX.key(model)).isSameInstanceAs(name);
  }

  static Stream<Arguments> dataDrivenIndexSource() {
    return DataDrivenModel.INDEX.values().stream()
        .map(value -> arguments(value.name, value));
  }

  private static class DataDrivenModel {
    static DataDrivenModel FIRST = new DataDrivenModel("first");
    static DataDrivenModel SECOND = new DataDrivenModel("second");
    static DataDrivenModel THIRD = new DataDrivenModel("third");
    static Index<String, DataDrivenModel> INDEX = Index.valueToKey(model -> model.name, FIRST, SECOND, THIRD);

    private final String name;

    private DataDrivenModel(final String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "DataDrivenModel{" +
          "name='" + this.name + '\'' +
          '}';
    }
  }
}
