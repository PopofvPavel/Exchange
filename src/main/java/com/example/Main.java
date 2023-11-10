package com.example;

public class Main {


    public static void main(String[] args) throws Exception {
        Exchange.createClient(1);
        Exchange.createClient(2);
        System.out.println(Exchange.getClientBalance(1));
        System.out.println(Exchange.getClientBalance(2));
        Exchange.addBalanceToClient(1,Valuta.RUB,10050);
        Exchange.addBalanceToClient(2,Valuta.RUB,150);
        Exchange.addBalanceToClient(2,Valuta.RUB,150);
        System.out.println(Exchange.getClientBalance(1));
        System.out.println(Exchange.getClientBalance(2));
        Exchange.reduceBalanceToClient(1, Valuta.RUB, 50);
        Exchange.reduceBalanceToClient(2, Valuta.RUB, 300);
        System.out.println(Exchange.getClientBalance(1));
        System.out.println(Exchange.getClientBalance(2));

        Exchange.createOrder(1,Valuta.USD,Valuta.RUB,Type.BUY, 85,10);//купить доллар за рубли
        //То есть тут мы покупаем 10 доллар за 950 рублей
       // Exchange.createOrder(1,Valuta.USD,Valuta.RUB,Type.SELL, 92,10);//продать доллар и получить рубли

    }


}
