package com.aksulightning.vibecode.betasinglechat.command;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ArgumentsTest {
    @Test
    public void resolvesAbsoluteAndRelativeCoordinates() throws CommandException {
        assertEquals(12.5, Arguments.coordinate("12.5", 100, "X"), 0);
        assertEquals(-4.5, Arguments.coordinate("~", -4.5, "Y"), 0);
        assertEquals(5.5, Arguments.coordinate("~-2.5", 8, "Z"), 0);
        assertEquals(10, Arguments.coordinate("~2", 8, "X"), 0);
    }

    @Test
    public void rejectsNonFiniteAndMalformedCoordinates() {
        for (String value : new String[] {"NaN", "Infinity", "-Infinity", "1e999", "~NaN", "~~2", "^1", ""}) {
            assertThrows(value, CommandException.class, () -> Arguments.coordinate(value, 0, "X"));
        }
        assertThrows(CommandException.class,
                () -> Arguments.coordinate("~1.7976931348623157E308", Double.MAX_VALUE, "X"));
    }

    @Test
    public void acceptsIntegerLimitsAndRejectsOverflowAndFractions() throws CommandException {
        assertEquals(1, Arguments.integer("1", 1, 32, "Amount"));
        assertEquals(32, Arguments.integer("32", 1, 32, "Amount"));
        for (String value : new String[] {"0", "-1", "33", "2147483648", "1.5", "NaN"}) {
            assertThrows(value, CommandException.class, () -> Arguments.integer(value, 1, 32, "Amount"));
        }
    }

    @Test
    public void rejectsMissingAndExcessArguments() throws CommandException {
        Arguments.count(new String[] {"pig"}, 1, 2, "/spawnmob <mob> [amount]");
        assertThrows(CommandException.class, () -> Arguments.count(new String[0], 1, 2, "/spawnmob"));
        assertThrows(CommandException.class,
                () -> Arguments.count(new String[] {"pig", "2", "extra"}, 1, 2, "/spawnmob"));
    }
}
