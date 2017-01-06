package io.datanerds.avropatch;

import avro.shaded.com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import io.datanerds.avropatch.operation.*;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.datanerds.avropatch.operation.matcher.OperationMatchers.hasItem;
import static io.datanerds.avropatch.operation.matcher.PatchMatcher.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class PatchTest {

    @Test
    public void serializesAdd() throws IOException {
        Patch patch = patchOf(new Add<>(Path.of("person", "name"), "John Doe"));
        byte[] bytes = patch.toBytes();

        List<Operation> operations = Patch.of(bytes).getOperations();
        assertThat(operations, hasSize(1));
        assertThat(operations, hasItem(new Add<>(Path.of("person", "name"), "John Doe")));
    }

    @Test
    public void serializesCopy() throws IOException {
        Patch patch = patchOf(new Copy(Path.parse("/person/firstName"), Path.parse("/person/lastName")));
        byte[] bytes = patch.toBytes();

        List<Operation> operations = Patch.of(bytes).getOperations();
        assertThat(operations, hasSize(1));
        assertThat(operations, hasItem(new Copy(Path.parse("/person/firstName"), Path.parse("/person/lastName"))));
    }

    @Test
    public void serializesMove() throws IOException {
        Patch patch = patchOf(new Move(Path.parse("/person/firstName"), Path.parse("/person/lastName")));
        byte[] bytes = patch.toBytes();

        List<Operation> operations = Patch.of(bytes).getOperations();
        assertThat(operations, hasSize(1));
        assertThat(operations, hasItem(new Move(Path.parse("/person/firstName"), Path.parse("/person/lastName"))));
    }

    @Test
    public void serializesRemove() throws IOException {
        Patch patch = patchOf(new Remove(Path.parse("/person/name")));
        byte[] bytes = patch.toBytes();

        List<Operation> operations = Patch.of(bytes).getOperations();
        assertThat(operations, hasSize(1));
        assertThat(operations, hasItem(new Remove(Path.parse("/person/name"))));
    }

    @Test
    public void serializesReplace() throws IOException {
        Patch patch = patchOf(new Replace(Path.parse("/person/number"), 42));
        byte[] bytes = patch.toBytes();

        List<Operation> operations = Patch.of(bytes).getOperations();
        assertThat(operations, hasItem(new Replace(Path.parse("/person/number"), 42)));
    }

    @Test
    public void serializesTest() throws IOException {
        Patch patch = patchOf(new io.datanerds.avropatch.operation.Test(Path.parse("/person/number"), 42L));
        byte[] bytes = patch.toBytes();

        List<Operation> operations = Patch.of(bytes).getOperations();
        assertThat(operations, hasSize(1));
        assertThat(operations, hasItem(new io.datanerds.avropatch.operation.Test(Path.parse("/person/number"), 42L)));
    }

    @Test
    public void serializesBunchOfOperations() throws IOException {
        Patch patch = new Patch(ImmutableList.of(
                new Add<>(Path.of("person", "name"), "John Doe"),
                new Copy(Path.parse("/person/firstName"), Path.parse("/person/lastName")),
                new Move(Path.parse("/person/firstName"), Path.parse("/person/lastName")),
                new Remove(Path.parse("/person/name")),
                new Replace(Path.parse("/person/number"), 42),
                new io.datanerds.avropatch.operation.Test(Path.parse("/person/number"), 42L)));

        byte[] bytes = patch.toBytes();
        assertThat(patch, is(equalTo(Patch.of(bytes))));
    }

    @Test
    public void serializesDefaultValueTypes() throws IOException {
        Date date = new Date();
        UUID uuid = UUID.randomUUID();
        Patch patch = new Patch(ImmutableList.of(
                new Add<>(Path.of("some", "value"), "John Doe"),
                new Add<>(Path.of("some", "value"), 42),
                new Add<>(Path.of("some", "value"), 42L),
                new Add<>(Path.of("some", "value"), uuid),
                new Add<>(Path.of("some", "value"), new BigDecimal("128976548936549275.9674592348654789")),
                new Add<>(Path.of("some", "value"), new BigInteger("90374692364523789623490569234562347895")),
                new Add<>(Path.of("some", "value"), true),
                new Add<>(Path.of("some", "value"), date),
                new Add<>(Path.of("some", "value"), 4234.2345)));

        byte[] bytes = patch.toBytes();
        assertThat(patch, is(equalTo(Patch.of(bytes))));
    }

    @Test
    public void serializesArbitraryHeadersWithoutOperations() throws IOException {
        Patch patch = new Patch(Collections.EMPTY_LIST,
                ImmutableMap.of(
                        "header 1", UUID.randomUUID(),
                        "header 2", new Date(),
                        "header 3", 1234L,
                        "header 4", new BigDecimal("3214123453.123512345")));
        byte[] bytes = patch.toBytes();
        assertThat(patch, is(equalTo(Patch.of(bytes))));
    }

    @Test
    public void serializesArbitraryHeadersWithOperations() throws IOException {
        Patch patch = new Patch(ImmutableList.of(
                    new Copy(Path.of("from", "here"), Path.of("to", "there")),
                    new Move(Path.of("from", "here"), Path.of("to", "there"))),
                ImmutableMap.of(
                        "header 1", UUID.randomUUID(),
                        "header 2", new Date(),
                        "header 3", 1234L,
                        "header 4", new BigDecimal("3214123453.123512345")));
        byte[] bytes = patch.toBytes();
        assertThat(patch, is(equalTo(Patch.of(bytes))));
    }

    private <T extends Operation> Patch patchOf(T operation) {
        return new Patch(ImmutableList.of(operation));
    }
}