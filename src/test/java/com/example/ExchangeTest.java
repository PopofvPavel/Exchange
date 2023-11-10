package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExchangeTest {

    @BeforeAll
    public static void setUp() throws Exception {

        Exchange.createClient(1);
        try {
            Exchange.addBalanceToClient(1, Valuta.RUB, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Exchange.createClient(2);
        try {
            Exchange.addBalanceToClient(2, Valuta.EUR, 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Exchange.createClient(3);
        try {
            Exchange.addBalanceToClient(3, Valuta.RUB, 150);
            Exchange.addBalanceToClient(3, Valuta.USD, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Exchange.createClient(4);
        Exchange.addBalanceToClient(4,Valuta.RUB,10050);
        Exchange.addBalanceToClient(4,Valuta.USD,10050);

        //match test
        Exchange.createClient(10);
        Exchange.createClient(11);
        Exchange.addBalanceToClient(10, Valuta.RUB, 1000);
        Exchange.addBalanceToClient(11, Valuta.USD, 10);

        //process order test
        Exchange.createClient(20);
        Exchange.createClient(21);
        Exchange.addBalanceToClient(20, Valuta.RUB, 2000);
        Exchange.addBalanceToClient(21, Valuta.USD, 50);

        Exchange.createClient(22);
        Exchange.createClient(23);

        Exchange.createClient(55);
        Exchange.addBalanceToClient(55, Valuta.USD, 1);
    }


    @Test
    public void testCreateClientWhenNewClientThenAddedToList() {
        int newClientId = 99;

        Exchange.createClient(newClientId);

        Client client = Exchange.getClientFromList(newClientId);
        assertNotNull(client, "Client should not be null");
        assertEquals(newClientId, client.getId(), "Client id should be the same as the one provided");
    }

    @Test
    public void testCreateClientWhenClientWithSameIdExistsThenThrowException() {

        int existingClientId = 1;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Exchange.createClient(existingClientId);
        });

        String expectedMessage = "Такой id же существует";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testGetClientBalanceWhenClientExistsThenReturnBalance() {
        int existingClientId = 1;

        String balance = Exchange.getClientBalance(existingClientId);

        assertNotNull(balance, "Balance should not be null");
        assertTrue(balance.contains("Баланс клиента  " + existingClientId), "Balance should contain client id");
    }

    @Test
    public void testGetClientBalanceWhenClientDoesNotExistThenReturnErrorMessage() {

        int nonExistingClientId = 1337;


        String balance = Exchange.getClientBalance(nonExistingClientId);


        assertNotNull(balance, "Balance should not be null");
        assertTrue(balance.contains("Данный клиент не найден: " + nonExistingClientId), "Balance should contain error message");
    }

    @Test
    public void testAddBalanceToClientWhenClientExistsThenBalanceIncreased() throws Exception {

        int existingClientId = 1;
        Valuta valuta = Valuta.USD;
        double moneyToAdd = 100.0;


        Exchange.addBalanceToClient(existingClientId, valuta, moneyToAdd);


        Client client = Exchange.getClientFromList(existingClientId);
        assertNotNull(client, "Client should not be null");
        assertEquals(moneyToAdd, client.getBalanceOfValuta(valuta), "Client balance should be increased by the added money");
    }

    @Test
    public void testAddBalanceToClientWhenClientDoesNotExistThenThrowException() {

        int nonExistingClientId = 1337;
        Valuta valuta = Valuta.USD;
        double moneyToAdd = 100.0;


        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.addBalanceToClient(nonExistingClientId, valuta, moneyToAdd);
        });

        String expectedMessage = "Данный клиент не найден";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testReduceBalanceToClientWhenClientExistsAndHasEnoughBalanceThenBalanceReduced() throws Exception {
        Exchange.reduceBalanceToClient(2, Valuta.EUR, 300);
        Client client = Exchange.getClientFromList(2);
        double actualBalance = client.getBalanceOfValuta(Valuta.EUR);
        assertEquals(200, actualBalance);
    }

    @Test
    public void testReduceBalanceToClientWhenClientDoesNotExistThenExceptionThrown() {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.reduceBalanceToClient(50, Valuta.EUR, 50);
        });
        String expectedMessage = "Данный клиент не найден";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testReduceBalanceToClientWhenClientExistsButNotEnoughBalanceThenExceptionThrown() {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.reduceBalanceToClient(2, Valuta.EUR, 5000000);
        });
        String expectedMessage = "У данного клиента недостаточно денег для списания";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testReduceBalanceToClientWhenClientExistsButNotHaveCurrentValutaThenExceptionThrown() {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.reduceBalanceToClient(2, Valuta.RUB, 5000000);
        });
        String expectedMessage = "У данного клииента нет такой валюты";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCreateOrderCurrencyPairNotExistsThenExceptionThrown() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.createOrder(1, Valuta.RUB, Valuta.USD, Type.BUY, 10, 10);
        });
        String expectedMessage = "Такая валютная пара не существует.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCreateOrderWhenClientNotExistsThenExceptionThrown() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.createOrder(33, Valuta.USD, Valuta.RUB, Type.BUY, 10, 10);
        });
        String expectedMessage = "Данный клиент не найден";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCreateOrderWhenNotEnoughMoneyToBuyThenExceptionThrown() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.createOrder(3, Valuta.USD, Valuta.RUB, Type.BUY, 95, 10);//хочет купить 10 долларов
        });
        String expectedMessage = "У клиента недостаточно денег для покупки:";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCreateOrderWhenNotEnoughMoneyToSellThenExceptionThrown() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.createOrder(55, Valuta.USD, Valuta.RUB, Type.SELL, 92, 10);//хочет продать 10 долларов
        });
        String expectedMessage = "У клиента недостаточно денег для продажи:";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCreateOrderWhenPriceHigherThanUpperLimitThenExceptionThrown() {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.createOrder(4,Valuta.USD,Valuta.RUB,Type.BUY, 110,10);
        });
        String expectedMessage = "Слишком высокая цена, выход за лимит";
        String actualMessage = exception.getMessage();
        System.out.println(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testCreateOrderWhenPriceLowerThanLowerLimitThenExceptionThrown() {
        Exception exception = assertThrows(Exception.class, () -> {
            Exchange.createOrder(4,Valuta.USD,Valuta.RUB,Type.SELL, 44,10);
        });
        String expectedMessage = "Слишком низкая цена, выход за лимит";
        String actualMessage = exception.getMessage();
        System.out.println(actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testIsMatchTrue() {
        CurrencyPair currencyPair = new CurrencyPair(Valuta.USD, Valuta.RUB,90.0);
        Order order = new Order(10, currencyPair, Type.BUY, 99, 10);
        Order order2 = new Order(11, currencyPair, Type.SELL, 90, 10);
        boolean isMatch = Exchange.isMatch(order, order2);
        assertTrue(isMatch);
    }
    @Test
    public void testIsMatchFalseTooMuchDifference() {
        CurrencyPair currencyPair = new CurrencyPair(Valuta.USD, Valuta.RUB,90.0);
        Order order = new Order(10, currencyPair, Type.BUY, 990, 10);
        Order order2 = new Order(11, currencyPair, Type.SELL, 90, 10);
        boolean isMatch = Exchange.isMatch(order, order2);
        assertFalse(isMatch);
    }
    @Test
    public void testIsMatchFalseWrongTypes() {
        CurrencyPair currencyPair = new CurrencyPair(Valuta.USD, Valuta.RUB,90.0);
        Order order = new Order(10, currencyPair, Type.BUY, 99.0, 10);
        Order order2 = new Order(11, currencyPair, Type.BUY, 90, 10);
        boolean isMatch = Exchange.isMatch(order, order2);
        assertFalse(isMatch);
    }

    @Test
    public void testIsMatchFalseWrongValuta() {
        CurrencyPair currencyPair = new CurrencyPair(Valuta.USD, Valuta.RUB,90.0);
        CurrencyPair currencyPair2 = new CurrencyPair(Valuta.RUB, Valuta.USD,90.0);
        Order order = new Order(10, currencyPair, Type.BUY, 99, 10);
        Order order2 = new Order(11, currencyPair2, Type.SELL, 90, 10);
        boolean isMatch = Exchange.isMatch(order, order2);
        assertFalse(isMatch);
    }

    @Test
    public void testProcessOrder() throws Exception {
        CurrencyPair currencyPair = new CurrencyPair(Valuta.USD, Valuta.RUB,90.0);
        CurrencyPair currencyPair2 = new CurrencyPair(Valuta.RUB, Valuta.USD,90.0);
        //Order order = new Order(20, currencyPair, Type.BUY, 88, 10);
        //Order order2 = new Order(21, currencyPair2, Type.SELL, 99, 5);
        Exchange.createOrder(20, Valuta.USD,Valuta.RUB, Type.BUY, 88, 10);
        Exchange.createOrder(21, Valuta.USD,Valuta.RUB, Type.SELL, 99, 15);
        Exchange.processOrders();
        System.out.println(Exchange.getOrdersString());

    }
}