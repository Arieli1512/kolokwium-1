package edu.iis.mto.coffee;

import edu.iis.mto.coffee.machine.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoffeeMachineTest {
    @Mock
    CoffeeGrinder coffeeGrinder;
    @Mock
    MilkProvider milkProvider;
    @Mock
    CoffeeReceipes coffeeReceipes;
    CoffeeMachine coffeeMachine;
    Map<CoffeeSize, Integer> map = new HashMap<>();
    @BeforeEach
    void before()
    {
        coffeeMachine = new CoffeeMachine(coffeeGrinder,milkProvider,coffeeReceipes);
        map.put(CoffeeSize.SMALL, 100);
        map.put(CoffeeSize.STANDARD, 120);
        map.put(CoffeeSize.DOUBLE, 200);

    }
    @Test
    void shouldReturnCoffeeWithoutMilk() throws GrinderException {
        when(coffeeReceipes.getReceipe(any(CoffeeType.class))).thenReturn(CoffeeReceipe.builder().withMilkAmount(0).withWaterAmounts(map).build());
        when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(true);
        CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.SMALL).withType(CoffeeType.LATTE).build();
        Coffee coffee = coffeeMachine.make(coffeeOrder);
        assertEquals(Status.READY,coffee.getStatus());
        assertEquals(100,coffee.getWaterAmount());
        assertEquals(0,coffee.getMilkAmout());
    }

    @Test
    void shouldSetStatusErrorCauseNoRecipe()
    {
        CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.ESPRESSO).build();
        Coffee coffee = coffeeMachine.make(coffeeOrder);
        assertEquals(Status.ERROR,coffee.getStatus());
    }
    @Test
    void shouldReturnCoffeeWithMilk() throws GrinderException {
        when(coffeeReceipes.getReceipe(any(CoffeeType.class))).thenReturn(CoffeeReceipe.builder().withMilkAmount(10).withWaterAmounts(map).build());
        when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(true);
        when(milkProvider.pour(any(int.class))).thenReturn(10);
        CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.ESPRESSO).build();
        Coffee coffee = coffeeMachine.make(coffeeOrder);
        assertEquals(Status.READY,coffee.getStatus());
        assertEquals(10,coffee.getMilkAmout());
    }

    @Test
    void shouldFailedCauseHeaterException() throws GrinderException, HeaterException {
        doThrow(new HeaterException()).when(milkProvider).heat();
        when(coffeeReceipes.getReceipe(any(CoffeeType.class))).thenReturn(CoffeeReceipe.builder().withMilkAmount(10).withWaterAmounts(map).build());
        when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(true);
        CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.ESPRESSO).build();
        Coffee coffee = coffeeMachine.make(coffeeOrder);
        assertEquals(Status.ERROR,coffee.getStatus());
    }

    @Test
        void shouldFailedCauseThereAreNoBeans() throws GrinderException {
        when(coffeeReceipes.getReceipe(any(CoffeeType.class))).thenReturn(CoffeeReceipe.builder().withMilkAmount(10).withWaterAmounts(map).build());
        when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(false);
        CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.ESPRESSO).build();
        Coffee coffee = coffeeMachine.make(coffeeOrder);
        assertEquals(Status.ERROR,coffee.getStatus());
    }
    @Test
    void shouldCallMethodGrindOneTime() throws GrinderException {
        when(coffeeReceipes.getReceipe(any(CoffeeType.class))).thenReturn(CoffeeReceipe.builder().withMilkAmount(10).withWaterAmounts(map).build());
        when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(true);
        when(milkProvider.pour(any(int.class))).thenReturn(10);
        CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.ESPRESSO).build();
        Coffee coffee = coffeeMachine.make(coffeeOrder);
        assertEquals(Status.READY,coffee.getStatus());
        assertEquals(10,coffee.getMilkAmout());
        verify(coffeeGrinder,times(1)).grind(any());
    }

    @Test
    void shouldCallMethodPourOneTime() throws GrinderException {
        when(coffeeReceipes.getReceipe(any(CoffeeType.class))).thenReturn(CoffeeReceipe.builder().withMilkAmount(10).withWaterAmounts(map).build());
        when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(true);
        when(milkProvider.pour(any(int.class))).thenReturn(10);
        CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.ESPRESSO).build();
        Coffee coffee = coffeeMachine.make(coffeeOrder);
        assertEquals(Status.READY,coffee.getStatus());
        verify(milkProvider,times(1)).pour(any(int.class));
    }

    @Test
    void thereIsDifferenceInCoffeeSize() throws GrinderException {
        when(coffeeReceipes.getReceipe(any(CoffeeType.class))).thenReturn(CoffeeReceipe.builder().withMilkAmount(5).withWaterAmounts(map).build());
        when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(true);
        when(milkProvider.pour(any(int.class))).thenReturn(5);

        CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.LATTE).build();
        Coffee coffee = coffeeMachine.make(coffeeOrder);
        assertEquals(Status.READY,coffee.getStatus());
        assertEquals(5,coffee.getMilkAmout());

        CoffeeOrder coffeeOrder1 = CoffeeOrder.builder().withSize(CoffeeSize.SMALL).withType(CoffeeType.LATTE).build();
        Coffee coffee1 = coffeeMachine.make(coffeeOrder1);
        assertEquals(Status.READY,coffee1.getStatus());
        assertEquals(5,coffee1.getMilkAmout());

        assertNotEquals(coffee1.getWaterAmount(),coffee.getWaterAmount());
    }
}
