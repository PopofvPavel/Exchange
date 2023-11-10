package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {

    @BeforeAll
    public static void setup() throws Exception {

        //1)создание клиента;
        Exchange.createClient(1);
        Exchange.createClient(2);
        Exchange.createClient(20);
        Exchange.createClient(21);
        Exchange.addBalanceToClient(20, Valuta.RUB, 2000);
        Exchange.addBalanceToClient(21, Valuta.USD, 50);
    }


    @Test
    public void testMainWhenAddingAndReducingBalancesAndCreatingAndExecutingOrdersThenStateIsCorrect() throws Exception {
        // 2)ввод-вывод средств клиента;
        Exchange.addBalanceToClient(1, Valuta.RUB, 10050);//reduce balance test
        Exchange.addBalanceToClient(2, Valuta.RUB, 150);//reduce balance test
        Exchange.addBalanceToClient(2, Valuta.RUB, 150);//reduce balance test
        Exchange.reduceBalanceToClient(1, Valuta.RUB, 50);
        Exchange.reduceBalanceToClient(2, Valuta.RUB, 300);
        //Exchange.createOrder(1, Valuta.USD, Valuta.RUB, Type.BUY, 85, 10);

        //3)создание заявки на куплю/продажу;
        Exchange.createOrder(20, Valuta.USD,Valuta.RUB, Type.BUY, 88, 10);
        Exchange.createOrder(21, Valuta.USD,Valuta.RUB, Type.SELL, 99, 15);
        System.out.println("До закупки");
        System.out.println("Client 20 balance " + Exchange.getClientBalance(20));
        System.out.println("Client 21 balance " + Exchange.getClientBalance(21));

        // Act
        Exchange.processOrders();
        assertEquals(5.0, Exchange.getOrderFromList(21).getVolume());//заявка должна остаться, но быть сокращена до 5
        //5)запрос состояния клиента (сколько денег в каких валютах у него есть на данный момент).
        System.out.println("После закупки");
        System.out.println("Client 20 balance " + Exchange.getClientBalance(20));
        System.out.println("Client 21 balance " + Exchange.getClientBalance(21));
        // Assert

        //processOrder
        assertEquals(1010.0 , Exchange.getClientBalanceInValute(20,Valuta.RUB));
        assertEquals(10.0, Exchange.getClientBalanceInValute(20,Valuta.USD));
        assertEquals(990.0 , Exchange.getClientBalanceInValute(21,Valuta.RUB));
        assertEquals(40.0, Exchange.getClientBalanceInValute(21,Valuta.USD));

        //reduce
        assertEquals(10000.0 , Exchange.getClientBalanceInValute(1,Valuta.RUB));
        assertEquals(0.0, Exchange.getClientBalanceInValute(2,Valuta.RUB));

        //4)запрос открытых на данный момент заявок;
        System.out.println(Exchange.getOrdersString());
    }
}