package com.example;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MassiveTest {
    private final Object monitor = new Object();
    @Test
    public void testExchangeProcessing() throws InterruptedException {
        int numClients = 3200;
        int numThreads = 4;


        // Создаем клиентов и инициализируем их балансы
        for (int i = 1; i <= numClients; i++) {
            Exchange.createClient(i);
            try {
                Exchange.addBalanceToClient(i, Valuta.USD, 100000.0);
                Exchange.addBalanceToClient(i, Valuta.RUB, 100000.0);
                Exchange.addBalanceToClient(i, Valuta.EUR, 100000.0);
                Exchange.addBalanceToClient(i, Valuta.BYN, 100000.0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // Создаем и запускаем потоки для создания заявок
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(() -> {

                    for (int j = 1; j <= numClients / 3; j++) {
                        try {
                            createRandomOrder(j);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

            });
            threads.add(thread);
            thread.start();
        }

        // Ждем завершения потоков
        for (Thread thread : threads) {
            thread.join();
        }

        // Обрабатываем заявки
        try {
            System.out.println("Заявки до обработки");
            System.out.println(Exchange.getOrdersString());

            int cnt = 1;
            while (cnt > 0) {
                Exchange.processOrders();
                cnt--;
            }

            System.out.println("Заявки после обработки");
            System.out.println(Exchange.getOrdersString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Проверяем, что балансы клиентов изменились
        for (int i = 1; i <= numClients; i++) {
            System.out.println(Exchange.getClientBalance(i));
        }

        System.out.println("Обработано заявок: " + Exchange.getProcessedOrdersCounter());
        //System.out.println("match counter: " + Exchange.getMatchCounter());

    }

    private void createRandomOrder(int clientId) throws Exception {
        Random random = new Random();

        // Создаем список возможных комбинаций валют
        List<CurrencyPair> possiblePairs = new ArrayList<>(Exchange.currencyPairs);

        if (possiblePairs.isEmpty()) {
            // Нет доступных валютных пар
            return;
        }

        // Выбираем случайную валютную пару из списка
        CurrencyPair currencyPair = possiblePairs.get(random.nextInt(possiblePairs.size()));

        Valuta baseCurrency = currencyPair.getBaseCurrency();
        Valuta quoteCurrency = currencyPair.getQuoteCurrency();

        Type type = random.nextBoolean() ? Type.BUY : Type.SELL;

        // Используем фиксированный шаг цены
        double priceStep = 0.1; // Можете настроить под свои требования
        double price = currencyPair.getExchangeRate() + random.nextDouble() * priceStep;

        double volume = (random.nextDouble()+ 1) * 10;

        synchronized (monitor) {
            Exchange.createOrder(clientId, baseCurrency, quoteCurrency, type, price, volume);
        }
    }

}